package cloud.pace.sdk.appkit.communication

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.Intent.EXTRA_SUBJECT
import android.content.Intent.EXTRA_TEXT
import android.content.Intent.EXTRA_TITLE
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.content.Intent.createChooser
import android.content.pm.PackageManager
import android.graphics.Bitmap
import androidx.core.content.ContextCompat.startActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import cloud.pace.sdk.appkit.app.webview.AppWebViewModel
import cloud.pace.sdk.appkit.communication.generated.model.request.GooglePayAvailabilityCheckRequest
import cloud.pace.sdk.appkit.communication.generated.model.request.GooglePayPaymentRequest
import cloud.pace.sdk.appkit.communication.generated.model.request.OpenURLInNewTabRequest
import cloud.pace.sdk.appkit.communication.generated.model.response.GooglePayAvailabilityCheckResponse
import cloud.pace.sdk.appkit.communication.generated.model.response.GooglePayPaymentResponse
import cloud.pace.sdk.appkit.navigation.NavigationUtils
import cloud.pace.sdk.appkit.utils.TokenValidator
import cloud.pace.sdk.idkit.IDKit
import cloud.pace.sdk.idkit.model.InternalError
import cloud.pace.sdk.utils.Completion
import cloud.pace.sdk.utils.DefaultDispatcherProvider
import cloud.pace.sdk.utils.DispatcherProvider
import cloud.pace.sdk.utils.Failure
import cloud.pace.sdk.utils.Success
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import net.openid.appauth.AuthorizationException
import timber.log.Timber
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException

interface AppModel {

    var callback: AppCallbackImpl?
    val close: LiveData<Unit>
    val openUrlInNewTab: LiveData<OpenURLInNewTabRequest>
    val biometricRequest: LiveData<AppWebViewModel.BiometricRequest>
    val authorize: LiveData<Result<Completion<String?>>>
    val endSession: LiveData<Result<LogoutResponse>>
    val googlePayAvailabilityCheckRequest: LiveData<Pair<GooglePayAvailabilityCheckRequest, (Completion<GooglePayAvailabilityCheckResponse>) -> Unit>>
    val googlePayPayment: LiveData<Pair<GooglePayPaymentRequest, (Completion<GooglePayPaymentResponse>) -> Unit>>

    fun reset()
    fun setBiometricRequest(request: AppWebViewModel.BiometricRequest)
    fun authorize(onResult: (Completion<String?>) -> Unit)
    fun endSession(onResult: (LogoutResponse) -> Unit)
    fun close()
    fun openUrlInNewTab(openURLInNewTabRequest: OpenURLInNewTabRequest)
    fun disable(host: String)
    fun getAccessToken(reason: InvalidTokenReason, oldToken: String?, onResult: (Completion<GetAccessTokenResponse>) -> Unit)
    fun showShareSheet(bitmap: Bitmap)
    fun showShareSheet(text: String, title: String)
    fun startNavigation(lat: Double, lon: Double)
    fun onLogin(context: Context, result: Completion<String?>)
    fun onLogout(onResult: (LogoutResponse) -> Unit)
    fun onCustomSchemeError(context: Context?, scheme: String)
    fun onImageDataReceived(bitmap: Bitmap)
    fun onShareTextReceived(text: String, title: String)
    fun setUserProperty(key: String, value: String, update: Boolean)
    fun logEvent(key: String, parameters: Map<String, Any>)
    fun getConfig(key: String, config: (String?) -> Unit)
    fun isAppRedirectAllowed(app: String, isAllowed: (Boolean) -> Unit)
    fun isSignedIn(isSignedIn: (Boolean) -> Unit)
    fun isRemoteConfigAvailable(isAvailable: (Boolean) -> Unit)
    fun onGooglePayAvailabilityRequest(request: GooglePayAvailabilityCheckRequest, onResult: (Completion<GooglePayAvailabilityCheckResponse>) -> Unit)
    fun onGooglePayPayment(googlePayPaymentRequest: GooglePayPaymentRequest, onResult: (Completion<GooglePayPaymentResponse>) -> Unit)
    fun onNavigationRequestReceived(lat: Double, lon: Double, name: String)

    class Result<T>(val onResult: (T) -> Unit)
}

class AppModelImpl(
    private val context: Context,
    dispatchers: DispatcherProvider = DefaultDispatcherProvider()
) : AppModel {

    override var callback: AppCallbackImpl? = null
    override var close = MutableLiveData<Unit>()
    override var openUrlInNewTab = MutableLiveData<OpenURLInNewTabRequest>()
    override var biometricRequest = MutableLiveData<AppWebViewModel.BiometricRequest>()
    override var authorize = MutableLiveData<AppModel.Result<Completion<String?>>>()
    override var endSession = MutableLiveData<AppModel.Result<LogoutResponse>>()
    override var googlePayAvailabilityCheckRequest = MutableLiveData<Pair<GooglePayAvailabilityCheckRequest, (Completion<GooglePayAvailabilityCheckResponse>) -> Unit>>()
    override var googlePayPayment = MutableLiveData<Pair<GooglePayPaymentRequest, (Completion<GooglePayPaymentResponse>) -> Unit>>()

    private val coroutineScope = CoroutineScope(dispatchers.main())

    override fun reset() {
        close = MutableLiveData()
        openUrlInNewTab = MutableLiveData()
        biometricRequest = MutableLiveData()
        authorize = MutableLiveData()
        endSession = MutableLiveData()
        googlePayAvailabilityCheckRequest = MutableLiveData()
        googlePayPayment = MutableLiveData()
    }

    override fun setBiometricRequest(request: AppWebViewModel.BiometricRequest) {
        coroutineScope.launch {
            biometricRequest.value = request
        }
    }

    override fun authorize(onResult: (Completion<String?>) -> Unit) {
        coroutineScope.launch {
            authorize.value = AppModel.Result(onResult)
        }
    }

    override fun endSession(onResult: (LogoutResponse) -> Unit) {
        coroutineScope.launch {
            endSession.value = AppModel.Result(onResult)
        }
    }

    override fun close() {
        coroutineScope.launch {
            close.value = Unit
            callback?.onClose()
        }
    }

    override fun openUrlInNewTab(openURLInNewTabRequest: OpenURLInNewTabRequest) {
        coroutineScope.launch {
            openUrlInNewTab.value = openURLInNewTabRequest
            callback?.onOpenInNewTab(openURLInNewTabRequest.url)
        }
    }

    override fun disable(host: String) {
        coroutineScope.launch {
            callback?.onDisable(host)
        }
    }

    override fun getAccessToken(reason: InvalidTokenReason, oldToken: String?, onResult: (Completion<GetAccessTokenResponse>) -> Unit) {
        if (IDKit.isInitialized) {
            if (reason == InvalidTokenReason.UNAUTHORIZED && oldToken != null && TokenValidator.isTokenValid(oldToken)) {
                Timber.wtf("Reason is UNAUTHORIZED and token is still valid -> send onSessionRenewalFailed()")
                sendOnSessionRenewalFailed(null, onResult)
            } else {
                if (IDKit.isAuthorizationValid()) {
                    IDKit.refreshToken { completion ->
                        when (completion) {
                            is Success -> completion.result?.let { token -> onResult(Success(GetAccessTokenResponse(token))) } ?: onResult(Failure(InternalError))
                            is Failure -> {
                                val code = (completion.throwable as? AuthorizationException)?.code
                                if (code != null && code >= 1000) {
                                    /**
                                     * Keycloak error --> For a full list of codes see [net.openid.appauth.AuthorizationException]
                                     */
                                    Timber.wtf("Keycloak error -> send onSessionRenewalFailed()")
                                    sendOnSessionRenewalFailed(completion.throwable, onResult)
                                } else {
                                    // General error (e.g. network error)
                                    onResult(Failure(InternalError))
                                }
                            }
                        }
                    }
                } else {
                    authorize { completion ->
                        when (completion) {
                            is Success -> {
                                val accessToken = completion.result
                                if (accessToken != null) {
                                    onResult(Success(GetAccessTokenResponse(accessToken, true)))
                                } else {
                                    onResult(Failure(InternalError))
                                }
                            }

                            is Failure -> onResult(Failure(completion.throwable))
                        }
                    }
                }
            }
        } else {
            coroutineScope.launch {
                callback?.getAccessToken(reason, oldToken) {
                    onResult(Success(it))
                }
            }
        }
    }

    override fun showShareSheet(bitmap: Bitmap) {
        try {
            // Save bitmap to cache directory
            val cachePath = File(context.cacheDir, IMAGES_DIRECTORY_NAME)
            cachePath.mkdirs()

            val receipt = File(cachePath, RECEIPT_FILENAME)
            val stream = FileOutputStream(receipt) // overwrites this image every time
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            stream.flush()
            stream.close()

            // Open share sheet
            val contentUri = FileProvider.getUriForFile(context, "${context.packageName}.pace_cloud_sdk_file_provider", receipt)
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                putExtra(Intent.EXTRA_STREAM, contentUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                type = context.contentResolver.getType(contentUri)
            }

            val chooserIntent = createChooser(shareIntent, null)
            chooserIntent.addFlags(FLAG_ACTIVITY_NEW_TASK)

            val resInfoList = context.packageManager.queryIntentActivities(chooserIntent, PackageManager.MATCH_DEFAULT_ONLY)
            resInfoList.forEach {
                val packageName = it.activityInfo.packageName
                context.grantUriPermission(packageName, contentUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            startActivity(context, chooserIntent, null)
        } catch (e: FileNotFoundException) {
            Timber.w(e, "Could not create FileOutputStream to write the receipt file")
        } catch (e: IOException) {
            Timber.w(e, "Could not create or save the receipt bitmap")
        } catch (e: IllegalArgumentException) {
            Timber.w(e, "The receipt file is outside the paths supported by the FileProvider")
        } catch (e: ActivityNotFoundException) {
            Timber.i(e, "No Activity found to execute the share intent")
        } catch (e: Exception) {
            Timber.w(e, "Could not create, save or share the receipt bitmap")
        }
    }

    override fun showShareSheet(text: String, title: String) {
        try {
            // Open share sheet
            val shareIntent: Intent = Intent().apply {
                action = Intent.ACTION_SEND
                type = "text/plain"
                putExtra(EXTRA_SUBJECT, title)
                putExtra(EXTRA_TITLE, title)
                putExtra(EXTRA_TEXT, text)
            }

            val chooserIntent = createChooser(shareIntent, title)
            chooserIntent.addFlags(FLAG_ACTIVITY_NEW_TASK)

            startActivity(context, chooserIntent, null)
        } catch (e: ActivityNotFoundException) {
            Timber.i(e, "No Activity found to execute the share intent of the text $text with title $title")
        } catch (e: Exception) {
            Timber.w(e, "Could not share the text $text with title $title")
        }
    }

    override fun startNavigation(lat: Double, lon: Double) {
        try {
            // Open navigation app chooser (Google Maps or Waze)
            val navigationIntent = NavigationUtils.getNavigationIntent(lat, lon)
            startActivity(context, navigationIntent, null)
        } catch (e: ActivityNotFoundException) {
            Timber.i(e, "No Activity found to execute the navigation intent to $lat; $lon")
        } catch (e: Exception) {
            Timber.w(e, "Could not start the navigation to $lat; $lon")
        }
    }

    private fun sendOnSessionRenewalFailed(throwable: Throwable?, onResult: (Completion<GetAccessTokenResponse>) -> Unit) {
        coroutineScope.launch {
            callback?.onSessionRenewalFailed(throwable) {
                it?.let { onResult(Success(GetAccessTokenResponse(it))) } ?: onResult(Failure(InternalError))
            }
        }
    }

    override fun onLogin(context: Context, result: Completion<String?>) {
        coroutineScope.launch {
            callback?.onLogin(context, result)
        }
    }

    override fun onLogout(onResult: (LogoutResponse) -> Unit) {
        coroutineScope.launch {
            callback?.onLogout(onResult)
        }
    }

    override fun onCustomSchemeError(context: Context?, scheme: String) {
        coroutineScope.launch {
            callback?.onCustomSchemeError(context, scheme)
        }
    }

    override fun onImageDataReceived(bitmap: Bitmap) {
        coroutineScope.launch {
            callback?.onImageDataReceived(bitmap)
        }
    }

    override fun onShareTextReceived(text: String, title: String) {
        coroutineScope.launch {
            callback?.onShareTextReceived(text, title)
        }
    }

    override fun setUserProperty(key: String, value: String, update: Boolean) {
        coroutineScope.launch {
            callback?.setUserProperty(key, value, update)
        }
    }

    override fun logEvent(key: String, parameters: Map<String, Any>) {
        coroutineScope.launch {
            callback?.logEvent(key, parameters)
        }
    }

    override fun getConfig(key: String, config: (String?) -> Unit) {
        coroutineScope.launch {
            callback?.getConfig(key, config)
        }
    }

    override fun isAppRedirectAllowed(app: String, isAllowed: (Boolean) -> Unit) {
        coroutineScope.launch {
            callback?.isAppRedirectAllowed(app, isAllowed)
        }
    }

    override fun isSignedIn(isSignedIn: (Boolean) -> Unit) {
        coroutineScope.launch {
            callback?.isSignedIn(isSignedIn)
        }
    }

    override fun isRemoteConfigAvailable(isAvailable: (Boolean) -> Unit) {
        coroutineScope.launch {
            callback?.isRemoteConfigAvailable(isAvailable)
        }
    }

    override fun onGooglePayAvailabilityRequest(request: GooglePayAvailabilityCheckRequest, onResult: (Completion<GooglePayAvailabilityCheckResponse>) -> Unit) {
        coroutineScope.launch {
            googlePayAvailabilityCheckRequest.value = Pair(request, onResult)
        }
    }

    override fun onGooglePayPayment(googlePayPaymentRequest: GooglePayPaymentRequest, onResult: (Completion<GooglePayPaymentResponse>) -> Unit) {
        coroutineScope.launch {
            googlePayPayment.value = Pair(googlePayPaymentRequest, onResult)
        }
    }

    override fun onNavigationRequestReceived(lat: Double, lon: Double, name: String) {
        coroutineScope.launch {
            callback?.onNavigationRequestReceived(lat, lon, name)
        }
    }

    companion object {
        private const val IMAGES_DIRECTORY_NAME = "images"
        private const val RECEIPT_FILENAME = "receipt.png"
    }
}
