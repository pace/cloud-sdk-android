package cloud.pace.sdk.appkit.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import cloud.pace.sdk.api.utils.RequestUtils
import cloud.pace.sdk.appkit.app.deeplink.DeepLinkManagementActivity
import cloud.pace.sdk.appkit.communication.LogoutResponse
import cloud.pace.sdk.appkit.communication.generated.model.request.GooglePayPaymentRequest
import cloud.pace.sdk.appkit.communication.generated.model.response.GooglePayAvailabilityCheckResponse
import cloud.pace.sdk.appkit.communication.generated.model.response.GooglePayPaymentResponse
import cloud.pace.sdk.appkit.model.PaymentResult
import cloud.pace.sdk.appkit.pay.GooglePayUtils
import cloud.pace.sdk.appkit.pay.GooglePayUtils.LOAD_PAYMENT_DATA_REQUEST_CODE
import cloud.pace.sdk.appkit.utils.BiometricUtils
import cloud.pace.sdk.databinding.ActivityAppBinding
import cloud.pace.sdk.idkit.IDKit
import cloud.pace.sdk.idkit.model.FailedRetrievingSessionWhileEnding
import cloud.pace.sdk.idkit.model.InternalError
import cloud.pace.sdk.idkit.model.OperationCanceled
import cloud.pace.sdk.utils.Canceled
import cloud.pace.sdk.utils.CloudSDKKoinComponent
import cloud.pace.sdk.utils.Completion
import cloud.pace.sdk.utils.ErrorLevel
import cloud.pace.sdk.utils.ErrorListener
import cloud.pace.sdk.utils.Failure
import cloud.pace.sdk.utils.Ok
import cloud.pace.sdk.utils.Success
import cloud.pace.sdk.utils.applyInsets
import cloud.pace.sdk.utils.getResultFor
import cloud.pace.sdk.utils.viewBinding
import com.google.android.gms.wallet.AutoResolveHelper
import com.google.android.gms.wallet.IsReadyToPayRequest
import com.google.android.gms.wallet.PaymentData
import com.google.android.gms.wallet.PaymentDataRequest
import com.google.android.gms.wallet.PaymentsClient
import com.google.gson.Gson
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

class AppActivity : AppCompatActivity(), CloudSDKKoinComponent {

    private val binding: ActivityAppBinding by viewBinding(ActivityAppBinding::inflate)
    private val viewModel: AppActivityViewModel by viewModel()
    private val gson by lazy { Gson() }

    private lateinit var paymentsClient: PaymentsClient
    private var backToFinish = true
    private var completableDeferred: CompletableDeferred<PaymentResult>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContentView(binding.root)
        binding.root.applyInsets()

        paymentsClient = GooglePayUtils.createPaymentsClient(this)

        val url = intent?.extras?.getString(APP_URL)
        if (url == null) {
            Timber.e("The start URL cannot be null. Finish AppActivity.")
            finish()
            return
        }

        backToFinish = intent.extras?.getBoolean(BACK_TO_FINISH, true) ?: true

        viewModel.closeEvent.observe(this) {
            it.getContentIfNotHandled()?.let {
                finish()
            }
        }

        viewModel.openUrlInNewTab.observe(this) {
            it.getContentIfNotHandled()?.let { openURLInNewTabRequest ->
                lifecycleScope.launch(Dispatchers.Main) {
                    val intent = Intent(this@AppActivity, DeepLinkManagementActivity::class.java)
                        .putExtra(DeepLinkManagementActivity.URL, openURLInNewTabRequest.url)
                        .putExtra(DeepLinkManagementActivity.INTEGRATED, openURLInNewTabRequest.integrated)

                    ErrorListener.reportBreadcrumb(TAG, "Created intent to start DeepLinkManagementActivity", mapOf("openURLInNewTabRequest" to openURLInNewTabRequest))

                    when (val result = getResultFor(intent)) {
                        is Ok -> {
                            val redirectUri = result.data?.data?.toString()
                            if (redirectUri != null) {
                                ErrorListener.reportBreadcrumb(TAG, "OpenURLInNewTabRequest finished successfully. Load redirect URL in original WebView.", mapOf("Redirect URL" to redirectUri))
                                binding.appWebView.loadUrl(redirectUri)
                            } else {
                                ErrorListener.reportError(
                                    NullPointerException("OpenURLInNewTabRequest cannot succeed without redirect URL. Load cancelUrl in original WebView: ${openURLInNewTabRequest.cancelUrl}")
                                )
                                binding.appWebView.loadUrl(openURLInNewTabRequest.cancelUrl)
                            }
                        }

                        is Canceled -> {
                            ErrorListener.reportBreadcrumb(
                                TAG,
                                "OpenURLInNewTabRequest was canceled. Load cancelUrl in original WebView",
                                mapOf("Cancel URL" to openURLInNewTabRequest.cancelUrl),
                                ErrorLevel.WARNING
                            )
                            binding.appWebView.loadUrl(openURLInNewTabRequest.cancelUrl)
                        }
                    }
                }
            }
        }

        viewModel.biometricRequest.observe(this) {
            it.getContentIfNotHandled()?.let { callback ->
                BiometricUtils.requestAuthentication(activity = this, title = getString(callback.title), onSuccess = callback.onSuccess, onFailure = callback.onFailure)
            }
        }

        viewModel.authorize.observe(this) { event ->
            event.getContentIfNotHandled()?.let { result ->
                lifecycleScope.launch(Dispatchers.Main) {
                    val authorizationResult = IDKit.authorize(this@AppActivity)
                    result.onResult(authorizationResult)
                    viewModel.onLogin(this@AppActivity, authorizationResult)
                }
            }
        }

        viewModel.endSession.observe(this) { event ->
            event.getContentIfNotHandled()?.let { result ->
                lifecycleScope.launch(Dispatchers.Main) {
                    val endSessionResult = IDKit.endSession(this@AppActivity)
                    val logoutResponse = when {
                        endSessionResult is Success -> LogoutResponse.SUCCESSFUL
                        endSessionResult is Failure && endSessionResult.throwable is FailedRetrievingSessionWhileEnding -> LogoutResponse.UNAUTHORIZED
                        else -> LogoutResponse.OTHER
                    }
                    result.onResult(logoutResponse)
                }
            }
        }

        viewModel.googlePayAvailabilityCheck.observe(this) { event ->
            event.getContentIfNotHandled()?.let { (request, response) ->
                val readyToPayRequest = IsReadyToPayRequest.fromJson(gson.toJson(request))

                paymentsClient.isReadyToPay(readyToPayRequest)
                    .addOnSuccessListener {
                        response(Success(GooglePayAvailabilityCheckResponse(it)))
                    }
                    .addOnFailureListener {
                        response(Failure(it))
                    }
                    .addOnCanceledListener {
                        response(Failure(OperationCanceled))
                    }
            }
        }

        viewModel.googlePayPayment.observe(this) { event ->
            event.getContentIfNotHandled()?.let { (request, response) ->
                lifecycleScope.launch(Dispatchers.Main) {
                    when (val result = requestPayment(request)) {
                        is PaymentResult.Ok -> result.data?.let { response(getGooglePayPaymentResponse(it)) }
                        is PaymentResult.Canceled -> response(Failure(OperationCanceled))
                        is PaymentResult.Error -> response(Failure(InternalError))
                    }
                }
            }
        }

        val isDarkMode = intent?.extras?.getBoolean(IS_DARK_MODE)
        binding.appWebView.init(RequestUtils.getUrlWithQueryParams(url), isDarkMode)
    }

    override fun onBackPressed() {
        if (backToFinish) {
            finish()
        } else {
            binding.appWebView.onBackPressed()
        }
    }

    /**
     * Handle a resolved activity from the Google Pay payment sheet.
     * Note: registerForActivityResult can not be used due to Google Pays implementation
     *
     * @param requestCode Request code originally supplied to AutoResolveHelper in requestPayment().
     * @param resultCode Result code returned by the Google Pay API.
     * @param data Intent from the Google Pay API containing payment or error data.
     */
    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        // Value passed in AutoResolveHelper
        if (requestCode == LOAD_PAYMENT_DATA_REQUEST_CODE) {
            val result = when (resultCode) {
                RESULT_OK -> {
                    Timber.i("Google Pay payment successful")
                    val paymentData = data?.let { PaymentData.getFromIntent(it) }
                    PaymentResult.Ok(paymentData)
                }

                RESULT_CANCELED -> {
                    Timber.i("User canceled Google Pay payment")
                    PaymentResult.Canceled
                }

                AutoResolveHelper.RESULT_ERROR -> {
                    // At this stage, the user has already seen a popup informing them an error occurred. Normally, only logging is required.
                    val status = AutoResolveHelper.getStatusFromIntent(data)
                    Timber.e("Failed to request Google Pay payment. Status code: ${status?.statusCode}; status message: ${status?.statusMessage}")
                    PaymentResult.Error(status)
                }

                else -> {
                    Timber.e("Failed to request Google Pay payment due to unknown error.")
                    PaymentResult.Error(null)
                }
            }

            completableDeferred?.complete(result)
        }

        super.onActivityResult(requestCode, resultCode, data)
    }

    private suspend fun requestPayment(googlePayRequest: GooglePayPaymentRequest): PaymentResult {
        val request = PaymentDataRequest.fromJson(gson.toJson(googlePayRequest))

        return CompletableDeferred<PaymentResult>().run {
            completableDeferred = this
            AutoResolveHelper.resolveTask(paymentsClient.loadPaymentData(request), this@AppActivity, LOAD_PAYMENT_DATA_REQUEST_CODE)
            await()
        }
    }

    /**
     * PaymentData response object contains the payment information, as well as any additional
     * requested information, such as billing and shipping address.
     *
     * @param paymentData A response object returned by Google after a payer approves payment.
     */
    private fun getGooglePayPaymentResponse(paymentData: PaymentData): Completion<GooglePayPaymentResponse> {
        return try {
            val paymentResponse = gson.fromJson(paymentData.toJson(), GooglePayPaymentResponse::class.java)
            if (paymentResponse != null) {
                Success(paymentResponse)
            } else {
                Timber.e("Failed to handle Google Pay payment success: response null")
                Failure(NullPointerException("Response is null"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to handle Google Pay payment success")
            Failure(e)
        }
    }

    override fun onDestroy() {
        binding.appWebView.onDestroy()
        super.onDestroy()

        completableDeferred?.cancel()
        completableDeferred = null
    }

    companion object {
        private const val TAG = "AppActivity"
        const val APP_URL = "APP_URL"
        const val BACK_TO_FINISH = "BACK_TO_FINISH"
        const val IS_DARK_MODE = "IS_DARK_MODE"
    }
}
