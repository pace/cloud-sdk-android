package cloud.pace.sdk.appkit.app

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import cloud.pace.sdk.api.utils.RequestUtils
import cloud.pace.sdk.appkit.app.deeplink.DeepLinkManagementActivity
import cloud.pace.sdk.appkit.communication.LogoutResponse
import cloud.pace.sdk.appkit.utils.BiometricUtils
import cloud.pace.sdk.databinding.ActivityAppBinding
import cloud.pace.sdk.idkit.IDKit
import cloud.pace.sdk.idkit.model.FailedRetrievingSessionWhileEnding
import cloud.pace.sdk.utils.Canceled
import cloud.pace.sdk.utils.CloudSDKKoinComponent
import cloud.pace.sdk.utils.ErrorLevel
import cloud.pace.sdk.utils.ErrorListener
import cloud.pace.sdk.utils.Failure
import cloud.pace.sdk.utils.Ok
import cloud.pace.sdk.utils.Success
import cloud.pace.sdk.utils.getResultFor
import cloud.pace.sdk.utils.viewBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

class AppActivity : AppCompatActivity(), CloudSDKKoinComponent {

    private val binding: ActivityAppBinding by viewBinding(ActivityAppBinding::inflate)
    private val viewModel: AppActivityViewModel by viewModel()
    private var backToFinish = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

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

        binding.appWebView.init(RequestUtils.getUrlWithQueryParams(url))
    }

    override fun onBackPressed() {
        if (backToFinish) {
            finish()
        } else {
            binding.appWebView.onBackPressed()
        }
    }

    override fun onDestroy() {
        binding.appWebView.onDestroy()
        super.onDestroy()
    }

    companion object {
        private const val TAG = "AppActivity"
        const val APP_URL = "APP_URL"
        const val BACK_TO_FINISH = "BACK_TO_FINISH"
    }
}
