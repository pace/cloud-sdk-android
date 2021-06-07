package cloud.pace.sdk.appkit.app.webview

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.location.Location
import android.net.Uri
import android.util.Base64
import androidx.annotation.StringRes
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cloud.pace.sdk.PACECloudSDK
import cloud.pace.sdk.R
import cloud.pace.sdk.api.utils.InterceptorUtils
import cloud.pace.sdk.appkit.communication.AppEventManager
import cloud.pace.sdk.appkit.communication.AppModel
import cloud.pace.sdk.appkit.communication.MessageHandler
import cloud.pace.sdk.appkit.model.InvalidTokenReason
import cloud.pace.sdk.appkit.pay.PayAuthenticationManager
import cloud.pace.sdk.appkit.persistence.SharedPreferencesImpl.Companion.getDisableTimePreferenceKey
import cloud.pace.sdk.appkit.persistence.SharedPreferencesImpl.Companion.getSecureDataPreferenceKey
import cloud.pace.sdk.appkit.persistence.SharedPreferencesModel
import cloud.pace.sdk.appkit.persistence.TotpSecret
import cloud.pace.sdk.appkit.utils.EncryptionUtils
import cloud.pace.sdk.utils.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.*
import timber.log.Timber
import java.net.HttpURLConnection
import java.util.*

abstract class AppWebViewModel : ViewModel(), AppWebViewClient.WebClientCallback {

    abstract val currentUrl: MutableLiveData<String?>
    abstract val loadUrl: LiveData<Event<String>>
    abstract val isInErrorState: LiveData<Event<Boolean>>
    abstract val showLoadingIndicator: LiveData<Event<Boolean>>
    abstract val biometricRequest: LiveData<Event<BiometricRequest>>
    abstract val newToken: LiveData<ResponseEvent<String>>
    abstract val verifyLocationResponse: LiveData<ResponseEvent<VerifyLocationResponse>>
    abstract val goBack: LiveData<Event<Unit>>
    abstract val isBiometricAvailable: LiveData<ResponseEvent<Boolean>>
    abstract val statusCode: LiveData<ResponseEvent<StatusCodeResponse>>
    abstract val totpResponse: LiveData<ResponseEvent<TOTPResponse>>
    abstract val secureData: LiveData<ResponseEvent<Map<String, String>>>
    abstract val appInterceptableLink: LiveData<ResponseEvent<AppInterceptableLinkResponse>>
    abstract val valueResponse: LiveData<ResponseEvent<ValueResponse>>

    abstract fun init(url: String)
    abstract fun close()
    abstract fun handleInvalidToken(message: String)
    abstract fun handleImageData(message: String)
    abstract fun handleVerifyLocation(message: String)
    abstract fun handleBack(message: String)
    abstract fun handleClose(message: String)
    abstract fun handleGetBiometricStatus(message: String)
    abstract fun handleSetTOTPSecret(message: String)
    abstract fun handleGetTOTP(message: String)
    abstract fun handleSetSecureData(message: String)
    abstract fun handleGetSecureData(message: String)
    abstract fun handleDisable(message: String)
    abstract fun handleOpenURLInNewTab(message: String)
    abstract fun handleGetAppInterceptableLink(message: String)
    abstract fun handleSetUserProperty(message: String)
    abstract fun handleLogEvent(message: String)
    abstract fun handleGetConfig(message: String)
    abstract fun handleGetTraceId(message: String)

    class MessageBundle<T>(val id: String?, val message: T)
    class ResponseEvent<T>(id: String?, content: T) : Event<MessageBundle<T>>(MessageBundle(id, content))

    class InvalidTokenRequest(val reason: String, val oldToken: String?)
    class VerifyLocationRequest(val lat: Double, val lon: Double, val threshold: Double)
    class BiometricRequest(@StringRes val title: Int, val onSuccess: () -> Unit, val onFailure: (errorCode: Int, errString: CharSequence) -> Unit)
    class SetTOTPRequest(val secret: String, val period: Int, val digits: Int, val algorithm: String, val key: String)
    class GetTOTPRequest(val serverTime: Int, val key: String)
    class SetSecureDataRequest(val key: String, val value: String)
    class KeyRequest(val key: String)
    class DisableRequest(val until: Long)
    class OpenURLInNewTabRequest(val url: String, val cancelUrl: String)
    class SetUserPropertyRequest(val key: String, val value: String, val update: Boolean = false)
    class LogEventRequest(val key: String, val parameters: Map<String, Any> = emptyMap())

    class VerifyLocationResponse(val verified: Boolean?, val accuracy: Float?)
    class TOTPResponse(val totp: String, val biometryMethod: String)
    class AppInterceptableLinkResponse(val link: String)
    class ValueResponse(val value: String)

    sealed class StatusCodeResponse(val statusCode: Int) {
        class Success(statusCode: Int = HttpURLConnection.HTTP_OK) : StatusCodeResponse(statusCode)
        class Failure(val error: String, statusCode: Int) : StatusCodeResponse(statusCode)
    }

    enum class BiometryMethod(val value: String) {
        FINGERPRINT("fingerprint"),
        FACE("face"),
        OTHER("other")
    }
}

class AppWebViewModelImpl(
    private val context: Context,
    private val dispatchers: DispatcherProvider,
    private val sharedPreferencesModel: SharedPreferencesModel,
    private val eventManager: AppEventManager,
    private val payAuthenticationManager: PayAuthenticationManager,
    private val appModel: AppModel,
    private val locationProvider: LocationProvider
) : AppWebViewModel() {

    override val currentUrl = MutableLiveData<String?>()
    override val loadUrl = MutableLiveData<Event<String>>()
    override val isInErrorState = MutableLiveData<Event<Boolean>>()
    override val showLoadingIndicator = MutableLiveData<Event<Boolean>>()
    override val biometricRequest = MutableLiveData<Event<BiometricRequest>>()
    override val newToken = MutableLiveData<ResponseEvent<String>>()
    override val verifyLocationResponse = MutableLiveData<ResponseEvent<VerifyLocationResponse>>()
    override val goBack = MutableLiveData<Event<Unit>>()
    override val isBiometricAvailable = MutableLiveData<ResponseEvent<Boolean>>()
    override val statusCode = MutableLiveData<ResponseEvent<StatusCodeResponse>>()
    override val totpResponse = MutableLiveData<ResponseEvent<TOTPResponse>>()
    override val secureData = MutableLiveData<ResponseEvent<Map<String, String>>>()
    override val appInterceptableLink = MutableLiveData<ResponseEvent<AppInterceptableLinkResponse>>()
    override val valueResponse = MutableLiveData<ResponseEvent<ValueResponse>>()

    private val gson = Gson()

    override fun init(url: String) {
        this.loadUrl.value = Event(url)
    }

    override fun close() {
        appModel.close(true)
    }

    override fun onSwitchErrorState(isError: Boolean, isHttpError: Boolean) {
        isInErrorState.value = Event(isError)
    }

    override fun onLoadingChanged(isLoading: Boolean) {
        showLoadingIndicator.value = Event(isLoading)
    }

    override fun onUrlChanged(newUrl: String) {
        currentUrl.value = newUrl
    }

    override fun handleInvalidToken(message: String) {
        val messageBundle = getMessageBundle<InvalidTokenRequest>(message) ?: return
        launch {
            suspendCoroutineWithTimeout<String>(message, MessageHandler.INVALID_TOKEN.timeoutMillis) { continuation ->
                val reason = messageBundle.message.reason
                val invalidTokenReason = InvalidTokenReason.values().associateBy(InvalidTokenReason::value)[reason] ?: InvalidTokenReason.OTHER
                appModel.onTokenInvalid(invalidTokenReason, messageBundle.message.oldToken) { token ->
                    continuation.resumeIfActive(token)
                }
            }?.let {
                newToken.postValue(ResponseEvent(messageBundle.id, it))
            }
        }
    }

    override fun handleImageData(message: String) {
        val messageBundle = getMessageBundle<String>(message) ?: return
        launch {
            timeout(message, MessageHandler.IMAGE_DATA.timeoutMillis) {
                val decodedString = Base64.decode(messageBundle.message, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
                appModel.onImageDataReceived(bitmap)
                statusCode.postValue(ResponseEvent(messageBundle.id, StatusCodeResponse.Success(HttpURLConnection.HTTP_NO_CONTENT)))
            }
        }
    }

    override fun handleVerifyLocation(message: String) {
        val messageBundle = getMessageBundle<VerifyLocationRequest>(message) ?: return
        launch {
            suspendCoroutineWithTimeout<VerifyLocationResponse>(message, MessageHandler.VERIFY_LOCATION.timeoutMillis) { continuation ->
                launch {
                    val currentLocation = locationProvider.currentLocation(true)
                    val location = if (currentLocation is Success) {
                        val currentLocationResult = currentLocation.result
                        if (currentLocationResult != null) {
                            currentLocationResult
                        } else {
                            val validLocation = locationProvider.firstValidLocation()
                            if (validLocation is Success) validLocation.result else null
                        }
                    } else null

                    val targetLocation = Location("").apply { latitude = messageBundle.message.lat; longitude = messageBundle.message.lon }
                    val verified = location?.let { it.distanceTo(targetLocation) <= messageBundle.message.threshold }

                    continuation.resumeIfActive(VerifyLocationResponse(verified, location?.accuracy))
                }
            }?.let {
                verifyLocationResponse.postValue(ResponseEvent(messageBundle.id, it))
            }
        }
    }

    override fun handleBack(message: String) {
        val messageBundle = getMessageBundle<String>(message) ?: return
        launch {
            timeout(message, MessageHandler.BACK.timeoutMillis) {
                onMainThread {
                    goBack.value = Event(Unit)
                }
                statusCode.postValue(ResponseEvent(messageBundle.id, StatusCodeResponse.Success(HttpURLConnection.HTTP_NO_CONTENT)))
            }
        }
    }

    override fun handleClose(message: String) {
        val messageBundle = getMessageBundle<String>(message) ?: return
        launch {
            timeout(message, MessageHandler.CLOSE.timeoutMillis) {
                close()
                statusCode.postValue(ResponseEvent(messageBundle.id, StatusCodeResponse.Success(HttpURLConnection.HTTP_NO_CONTENT)))
            }
        }
    }

    override fun handleGetBiometricStatus(message: String) {
        val messageBundle = getMessageBundle<String>(message) ?: return
        launch {
            timeout(message, MessageHandler.GET_BIOMETRIC_STATUS.timeoutMillis) {
                isBiometricAvailable.postValue(ResponseEvent(messageBundle.id, payAuthenticationManager.isFingerprintAvailable()))
            }
        }
    }

    override fun handleSetTOTPSecret(message: String) {
        val messageBundle = getMessageBundle<SetTOTPRequest>(message, HttpURLConnection.HTTP_INTERNAL_ERROR) ?: return
        launch {
            timeout(message, MessageHandler.SET_TOTP_SECRET.timeoutMillis) {
                val request = messageBundle.message

                EncryptionUtils.stringToAlgorithm(request.algorithm) ?: run {
                    statusCode.postValue(
                        ResponseEvent(
                            messageBundle.id, StatusCodeResponse.Failure("Invalid HMAC algorithm: ${messageBundle.message.algorithm}", HttpURLConnection.HTTP_INTERNAL_ERROR)
                        )
                    )
                    return@timeout
                }

                val host = getHost() ?: run {
                    statusCode.postValue(ResponseEvent(messageBundle.id, StatusCodeResponse.Failure("The host is null.", HttpURLConnection.HTTP_INTERNAL_ERROR)))
                    return@timeout
                }

                try {
                    val encryptedSecret = EncryptionUtils.encrypt(request.secret)
                    sharedPreferencesModel.setTotpSecret(host, request.key, TotpSecret(encryptedSecret, request.digits, request.period, request.algorithm))
                    statusCode.postValue(ResponseEvent(messageBundle.id, StatusCodeResponse.Success()))
                } catch (e: Exception) {
                    statusCode.postValue(ResponseEvent(messageBundle.id, StatusCodeResponse.Failure("Could not encrypt the TOTP secret.", HttpURLConnection.HTTP_INTERNAL_ERROR)))
                }
            }
        }
    }

    override fun handleGetTOTP(message: String) {
        val messageBundle = getMessageBundle<GetTOTPRequest>(message, HttpURLConnection.HTTP_INTERNAL_ERROR) ?: return
        launch {
            suspendCoroutineWithTimeout<String?>(message, MessageHandler.GET_TOTP.timeoutMillis) { continuation ->
                val id = messageBundle.id

                if (!payAuthenticationManager.isFingerprintAvailable()) {
                    statusCode.postValue(ResponseEvent(id, StatusCodeResponse.Failure("No biometric authentication is available or none has been set.", HttpURLConnection.HTTP_BAD_METHOD)))
                    continuation.resumeIfActive(null)
                    return@suspendCoroutineWithTimeout
                }

                val host = getHost() ?: run {
                    statusCode.postValue(ResponseEvent(id, StatusCodeResponse.Failure("The host is null.", HttpURLConnection.HTTP_INTERNAL_ERROR)))
                    continuation.resumeIfActive(null)
                    return@suspendCoroutineWithTimeout
                }

                val totpSecret = sharedPreferencesModel.getTotpSecret(host, messageBundle.message.key) ?: run {
                    if (isDomainInACL(host)) {
                        // Get master TOTP secret data
                        sharedPreferencesModel.getTotpSecret() ?: run {
                            statusCode.postValue(ResponseEvent(id, StatusCodeResponse.Failure("No biometric data found in the SharedPreferences.", HttpURLConnection.HTTP_NOT_FOUND)))
                            continuation.resumeIfActive(null)
                            return@suspendCoroutineWithTimeout
                        }
                    } else {
                        statusCode.postValue(ResponseEvent(id, StatusCodeResponse.Failure("The host $host is not in the access control list.", HttpURLConnection.HTTP_INTERNAL_ERROR)))
                        continuation.resumeIfActive(null)
                        return@suspendCoroutineWithTimeout
                    }
                }

                biometricRequest.postValue(Event(BiometricRequest(
                    R.string.biometric_totp_title,
                    onSuccess = {
                        try {
                            val decryptedSecret = EncryptionUtils.decrypt(totpSecret.encryptedSecret)
                            val otp = EncryptionUtils.generateOTP(decryptedSecret, totpSecret.digits, totpSecret.period, totpSecret.algorithm, Date(messageBundle.message.serverTime * 1000L))

                            continuation.resumeIfActive(otp)
                        } catch (e: Exception) {
                            statusCode.postValue(ResponseEvent(id, StatusCodeResponse.Failure("Could not decrypt the encrypted TOTP secret.", HttpURLConnection.HTTP_INTERNAL_ERROR)))
                            continuation.resumeIfActive(null)
                        }
                    },
                    onFailure = { errorCode, errString ->
                        statusCode.postValue(
                            ResponseEvent(
                                id, StatusCodeResponse.Failure("Biometric authentication failed: errorCode was $errorCode, errString was $errString", HttpURLConnection.HTTP_UNAUTHORIZED)
                            )
                        )
                        continuation.resumeIfActive(null)
                    }
                )))
            }?.let {
                // the biometric lib currently doesn't tell which method was used; set "other" for consistency
                totpResponse.postValue(ResponseEvent(messageBundle.id, TOTPResponse(it, BiometryMethod.OTHER.value)))
            }
        }
    }

    override fun handleSetSecureData(message: String) {
        val messageBundle = getMessageBundle<SetSecureDataRequest>(message, HttpURLConnection.HTTP_INTERNAL_ERROR) ?: return
        launch {
            timeout(message, MessageHandler.SET_SECURE_DATA.timeoutMillis) {
                val host = getHost() ?: run {
                    statusCode.postValue(ResponseEvent(messageBundle.id, StatusCodeResponse.Failure("The host is null.", HttpURLConnection.HTTP_INTERNAL_ERROR)))
                    return@timeout
                }
                val preferenceKey = getSecureDataPreferenceKey(host, messageBundle.message.key)
                val encryptedValue = EncryptionUtils.encrypt(messageBundle.message.value)

                sharedPreferencesModel.putString(preferenceKey, encryptedValue)
                statusCode.postValue(ResponseEvent(messageBundle.id, StatusCodeResponse.Success()))
            }
        }
    }

    override fun handleGetSecureData(message: String) {
        val messageBundle = getMessageBundle<KeyRequest>(message, HttpURLConnection.HTTP_INTERNAL_ERROR) ?: return
        launch {
            suspendCoroutineWithTimeout<String?>(message, MessageHandler.GET_SECURE_DATA.timeoutMillis) { continuation ->
                val id = messageBundle.id

                if (!payAuthenticationManager.isFingerprintAvailable()) {
                    statusCode.postValue(ResponseEvent(id, StatusCodeResponse.Failure("No biometric authentication is available or none has been set.", HttpURLConnection.HTTP_BAD_METHOD)))
                    continuation.resumeIfActive(null)
                    return@suspendCoroutineWithTimeout
                }

                val host = getHost() ?: run {
                    statusCode.postValue(ResponseEvent(id, StatusCodeResponse.Failure("The host is null.", HttpURLConnection.HTTP_INTERNAL_ERROR)))
                    continuation.resumeIfActive(null)
                    return@suspendCoroutineWithTimeout
                }
                val preferenceKey = getSecureDataPreferenceKey(host, messageBundle.message.key)
                val encryptedValue = sharedPreferencesModel.getString(preferenceKey) ?: run {
                    statusCode.postValue(
                        ResponseEvent(id, StatusCodeResponse.Failure("No encrypted value with the key $preferenceKey was found in the SharedPreferences.", HttpURLConnection.HTTP_NOT_FOUND))
                    )
                    continuation.resumeIfActive(null)
                    return@suspendCoroutineWithTimeout
                }

                biometricRequest.postValue(Event(BiometricRequest(
                    R.string.biometric_secure_data_title,
                    onSuccess = {
                        try {
                            val value = EncryptionUtils.decrypt(encryptedValue)
                            continuation.resumeIfActive(value)
                        } catch (e: Exception) {
                            statusCode.postValue(ResponseEvent(id, StatusCodeResponse.Failure("Could not decrypt the encrypted secure data value.", HttpURLConnection.HTTP_INTERNAL_ERROR)))
                            continuation.resumeIfActive(null)
                        }
                    },
                    onFailure = { errorCode, errString ->
                        statusCode.postValue(
                            ResponseEvent(
                                id, StatusCodeResponse.Failure("Biometric authentication failed: errorCode was $errorCode, errString was $errString", HttpURLConnection.HTTP_UNAUTHORIZED)
                            )
                        )
                        continuation.resumeIfActive(null)
                    }
                )))
            }?.let {
                secureData.postValue(ResponseEvent(messageBundle.id, mapOf("value" to it)))
            }
        }
    }

    override fun handleDisable(message: String) {
        val messageBundle = getMessageBundle<DisableRequest>(message, HttpURLConnection.HTTP_INTERNAL_ERROR) ?: return
        launch {
            timeout(message, MessageHandler.DISABLE.timeoutMillis) {
                val host = getHost()
                if (host != null) {
                    sharedPreferencesModel.putLong(getDisableTimePreferenceKey(host), messageBundle.message.until)
                    eventManager.setDisabledHost(host)
                    appModel.disable(host)
                    statusCode.postValue(ResponseEvent(messageBundle.id, StatusCodeResponse.Success()))
                } else {
                    statusCode.postValue(ResponseEvent(messageBundle.id, StatusCodeResponse.Failure("The host is null.", HttpURLConnection.HTTP_INTERNAL_ERROR)))
                }
                close()
            }
        }
    }

    override fun handleOpenURLInNewTab(message: String) {
        val messageBundle = getMessageBundle<OpenURLInNewTabRequest>(message) ?: return
        launch {
            timeout(message, MessageHandler.OPEN_URL_IN_NEW_TAB.timeoutMillis) {
                val redirectScheme = getRedirectScheme()
                onMainThread {
                    loadUrl.value = Event(messageBundle.message.cancelUrl)
                }

                if (!redirectScheme.isNullOrEmpty()) {
                    appModel.openUrlInNewTab(messageBundle.message.url)
                    statusCode.postValue(ResponseEvent(messageBundle.id, StatusCodeResponse.Success(HttpURLConnection.HTTP_NO_CONTENT)))
                } else {
                    appModel.onCustomSchemeError(context, "${redirectScheme}://redirect")
                    statusCode.postValue(ResponseEvent(messageBundle.id, StatusCodeResponse.Failure("Redirect scheme for deep linking has not been specified.", HttpURLConnection.HTTP_NOT_FOUND)))
                }
            }
        }
    }

    override fun handleGetAppInterceptableLink(message: String) {
        val messageBundle = getMessageBundle<String>(message) ?: return
        launch {
            timeout(message, MessageHandler.GET_APP_INTERCEPTABLE_LINK.timeoutMillis) {
                val redirectScheme = getRedirectScheme()
                if (!redirectScheme.isNullOrEmpty()) {
                    appInterceptableLink.postValue(ResponseEvent(messageBundle.id, AppInterceptableLinkResponse(redirectScheme)))
                } else {
                    statusCode.postValue(ResponseEvent(messageBundle.id, StatusCodeResponse.Failure("Redirect scheme for deep linking has not been specified.", HttpURLConnection.HTTP_NOT_FOUND)))
                }
            }
        }
    }

    override fun handleSetUserProperty(message: String) {
        val messageBundle = getMessageBundle<SetUserPropertyRequest>(message) ?: return
        launch {
            timeout(message, MessageHandler.SET_USER_PROPERTY.timeoutMillis) {
                appModel.setUserProperty(messageBundle.message.key, messageBundle.message.value, messageBundle.message.update)
                statusCode.postValue(ResponseEvent(messageBundle.id, StatusCodeResponse.Success(HttpURLConnection.HTTP_NO_CONTENT)))
            }
        }
    }

    override fun handleLogEvent(message: String) {
        val messageBundle = getMessageBundle<LogEventRequest>(message) ?: return
        launch {
            timeout(message, MessageHandler.LOG_EVENT.timeoutMillis) {
                appModel.logEvent(messageBundle.message.key, messageBundle.message.parameters)
                statusCode.postValue(ResponseEvent(messageBundle.id, StatusCodeResponse.Success(HttpURLConnection.HTTP_NO_CONTENT)))
            }
        }
    }

    override fun handleGetConfig(message: String) {
        val messageBundle = getMessageBundle<KeyRequest>(message) ?: return
        launch {
            val config = suspendCoroutineWithTimeout<String?>(message, MessageHandler.GET_CONFIG.timeoutMillis) { continuation ->
                appModel.getConfig(messageBundle.message.key) { config ->
                    continuation.resumeIfActive(config)
                }
            }

            if (config != null) {
                valueResponse.postValue(ResponseEvent(messageBundle.id, ValueResponse(config)))
            } else {
                statusCode.postValue(ResponseEvent(messageBundle.id, StatusCodeResponse.Failure("No config value found.", HttpURLConnection.HTTP_NOT_FOUND)))
            }
        }
    }

    override fun handleGetTraceId(message: String) {
        val messageBundle = getMessageBundle<Unit>(message) ?: return
        launch {
            timeout(message, MessageHandler.GET_TRACE_ID.timeoutMillis) {
                valueResponse.postValue(ResponseEvent(messageBundle.id, ValueResponse(InterceptorUtils.getTraceId())))
            }
        }
    }

    private fun getRedirectScheme(): String? {
        val applicationInfo = context.packageManager?.getApplicationInfo(context.packageName, PackageManager.GET_META_DATA)
        return applicationInfo?.metaData?.get("pace_redirect_scheme")?.toString()
    }

    private fun getHost() = currentUrl.value?.let { Uri.parse(it).host }

    private fun isDomainInACL(domain: String): Boolean {
        return PACECloudSDK.configuration.domainACL.any {
            val aclHost = Uri.parse(it).host ?: it
            domain.endsWith(aclHost)
        }
    }

    private inline fun <reified T> Gson.fromJson(json: String): MessageBundle<T>? {
        return try {
            val type = TypeToken.getParameterized(MessageBundle::class.java, T::class.java).type
            fromJson(json, type)
        } catch (e: Exception) {
            null
        }
    }

    private fun launch(block: suspend CoroutineScope.() -> Unit) = viewModelScope.launch(context = dispatchers.default(), block = block)

    private inline fun <reified T> getMessageBundle(message: String, errorCode: Int = HttpURLConnection.HTTP_BAD_REQUEST): MessageBundle<T>? {
        val bundle = gson.fromJson<T>(message)
        return if (bundle?.id != null && bundle.message != null) {
            bundle
        } else {
            val id = gson.fromJson<MessageBundle<Any>>(message)?.id
            statusCode.postValue(ResponseEvent(id, StatusCodeResponse.Failure("Could not deserialize the following JSON message: $message", errorCode)))
            null
        }
    }

    private suspend fun <T> timeout(message: String, timeoutMillis: Long, block: suspend CoroutineScope.() -> T) =
        try {
            withTimeout(timeoutMillis, block)
        } catch (e: TimeoutCancellationException) {
            val id = gson.fromJson<MessageBundle<Any>>(message)?.id
            Timber.w(e, "Timeout for request with ID $id")
            statusCode.postValue(ResponseEvent(id, StatusCodeResponse.Failure(e.message.orEmpty(), HttpURLConnection.HTTP_CLIENT_TIMEOUT)))
            null
        }

    private suspend inline fun <T> suspendCoroutineWithTimeout(message: String, timeoutMillis: Long, crossinline block: (CancellableContinuation<T>) -> Unit): T? =
        timeout(message, timeoutMillis) {
            suspendCancellableCoroutine(block)
        }
}
