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
import cloud.pace.sdk.PACECloudSDK
import cloud.pace.sdk.R
import cloud.pace.sdk.api.utils.InterceptorUtils
import cloud.pace.sdk.appkit.communication.AppEventManager
import cloud.pace.sdk.appkit.communication.AppModel
import cloud.pace.sdk.appkit.communication.InvalidTokenReason
import cloud.pace.sdk.appkit.communication.LogoutResponse
import cloud.pace.sdk.appkit.communication.generated.Communication
import cloud.pace.sdk.appkit.communication.generated.Metadata
import cloud.pace.sdk.appkit.communication.generated.model.request.*
import cloud.pace.sdk.appkit.communication.generated.model.response.*
import cloud.pace.sdk.appkit.pay.PayAuthenticationManager
import cloud.pace.sdk.appkit.persistence.SharedPreferencesImpl.Companion.getDisableTimePreferenceKey
import cloud.pace.sdk.appkit.persistence.SharedPreferencesImpl.Companion.getSecureDataPreferenceKey
import cloud.pace.sdk.appkit.persistence.SharedPreferencesModel
import cloud.pace.sdk.appkit.persistence.TotpSecret
import cloud.pace.sdk.appkit.utils.EncryptionUtils
import cloud.pace.sdk.idkit.IDKit
import cloud.pace.sdk.utils.*
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout
import timber.log.Timber
import java.util.*

abstract class AppWebViewModel : ViewModel(), AppWebViewClient.WebClientCallback, Communication {

    abstract val currentUrl: MutableLiveData<String?>
    abstract val loadUrl: LiveData<Event<String>>
    abstract val isInErrorState: LiveData<Event<Boolean>>
    abstract val showLoadingIndicator: LiveData<Event<Boolean>>
    abstract val biometricRequest: LiveData<Event<BiometricRequest>>
    abstract val goBack: LiveData<Event<Unit>>

    abstract fun init(url: String)
    abstract fun closeApp()

    class BiometricRequest(@StringRes val title: Int, val onSuccess: () -> Unit, val onFailure: (errorCode: Int, errString: CharSequence) -> Unit)

    enum class BiometryMethod(val value: String) {
        FINGERPRINT("fingerprint"),
        FACE("face"),
        OTHER("other")
    }
}

class AppWebViewModelImpl(
    private val context: Context,
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
    override val goBack = MutableLiveData<Event<Unit>>()

    override fun init(url: String) {
        this.loadUrl.value = Event(url)
    }

    override fun closeApp() {
        appModel.close()
    }

    override fun onClose() {
        closeApp()
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

    override suspend fun introspect(timeout: Long): IntrospectResult {
        return try {
            withTimeout(timeout) {
                IntrospectResult(IntrospectResult.Success(IntrospectResponse(Metadata.version, Metadata.operations)))
            }
        } catch (e: TimeoutCancellationException) {
            Timber.w(e, "Timeout for introspect")
            IntrospectResult(IntrospectResult.Failure(IntrospectResult.Failure.StatusCode.RequestTimeout, IntrospectError(e.message)))
        }
    }

    override suspend fun close(timeout: Long): CloseResult {
        return try {
            withTimeout(timeout) {
                closeApp()
                CloseResult(CloseResult.Success())
            }
        } catch (e: TimeoutCancellationException) {
            Timber.w(e, "Timeout for close")
            CloseResult(CloseResult.Failure(CloseResult.Failure.StatusCode.RequestTimeout, CloseError(e.message)))
        }
    }

    override suspend fun logout(timeout: Long): LogoutResult {
        return try {
            suspendCoroutineWithTimeout(timeout) { continuation ->
                appModel.onLogout {
                    when (it) {
                        LogoutResponse.SUCCESSFUL -> continuation.resumeIfActive(LogoutResult(LogoutResult.Success()))
                        LogoutResponse.UNAUTHORIZED -> continuation.resumeIfActive(
                            LogoutResult(
                                LogoutResult.Failure(
                                    LogoutResult.Failure.StatusCode.NotFound,
                                    LogoutError("User was not even logged in")
                                )
                            )
                        )
                        LogoutResponse.OTHER -> continuation.resumeIfActive(LogoutResult(LogoutResult.Failure(LogoutResult.Failure.StatusCode.InternalServerError, LogoutError())))
                    }
                }
            }
        } catch (e: TimeoutCancellationException) {
            Timber.w(e, "Timeout for logout")
            LogoutResult(LogoutResult.Failure(LogoutResult.Failure.StatusCode.RequestTimeout, LogoutError(e.message)))
        }
    }

    override suspend fun getBiometricStatus(timeout: Long): GetBiometricStatusResult {
        return try {
            withTimeout(timeout) {
                GetBiometricStatusResult(GetBiometricStatusResult.Success(GetBiometricStatusResponse(payAuthenticationManager.isFingerprintAvailable())))
            }
        } catch (e: TimeoutCancellationException) {
            Timber.w(e, "Timeout for getBiometricStatus")
            GetBiometricStatusResult(GetBiometricStatusResult.Failure(GetBiometricStatusResult.Failure.StatusCode.RequestTimeout, GetBiometricStatusError(e.message)))
        }
    }

    override suspend fun setTOTP(timeout: Long, setTOTPRequest: SetTOTPRequest): SetTOTPResult {
        return try {
            val algorithm = EncryptionUtils.stringToAlgorithm(setTOTPRequest.algorithm)
            if (algorithm != null) {
                val host = getHost()
                if (host != null) {
                    try {
                        val encryptedSecret = EncryptionUtils.encrypt(setTOTPRequest.secret)
                        sharedPreferencesModel.setTotpSecret(host, setTOTPRequest.key, TotpSecret(encryptedSecret, setTOTPRequest.digits, setTOTPRequest.period, setTOTPRequest.algorithm))
                        SetTOTPResult(SetTOTPResult.Success())
                    } catch (e: Exception) {
                        SetTOTPResult(SetTOTPResult.Failure(SetTOTPResult.Failure.StatusCode.InternalServerError, SetTOTPError("Could not encrypt the TOTP secret.")))
                    }
                } else {
                    SetTOTPResult(SetTOTPResult.Failure(SetTOTPResult.Failure.StatusCode.InternalServerError, SetTOTPError("The host is null.")))
                }
            } else {
                SetTOTPResult(SetTOTPResult.Failure(SetTOTPResult.Failure.StatusCode.InternalServerError, SetTOTPError("Invalid HMAC algorithm: ${setTOTPRequest.algorithm}")))
            }
        } catch (e: TimeoutCancellationException) {
            Timber.w(e, "Timeout for setTOTP")
            SetTOTPResult(SetTOTPResult.Failure(SetTOTPResult.Failure.StatusCode.RequestTimeout, SetTOTPError(e.message)))
        }
    }

    override suspend fun getTOTP(timeout: Long, getTOTPRequest: GetTOTPRequest): GetTOTPResult {
        return try {
            suspendCoroutineWithTimeout(timeout) { continuation ->
                if (payAuthenticationManager.isFingerprintAvailable()) {
                    val host = getHost()
                    if (host != null) {
                        val totpSecret = sharedPreferencesModel.getTotpSecret(host, getTOTPRequest.key) ?: run {
                            if (isDomainInACL(host)) {
                                // Get master TOTP secret data
                                sharedPreferencesModel.getTotpSecret()
                            } else {
                                null
                            }
                        }

                        if (totpSecret != null) {
                            biometricRequest.postValue(Event(BiometricRequest(
                                R.string.biometric_totp_title,
                                onSuccess = {
                                    try {
                                        val decryptedSecret = EncryptionUtils.decrypt(totpSecret.encryptedSecret)
                                        val otp = EncryptionUtils.generateOTP(
                                            decryptedSecret,
                                            totpSecret.digits,
                                            totpSecret.period,
                                            totpSecret.algorithm,
                                            Date((getTOTPRequest.serverTime * 1000.0).toLong())
                                        )
                                        continuation.resumeIfActive(GetTOTPResult(GetTOTPResult.Success(GetTOTPResponse(otp, BiometryMethod.OTHER.value))))
                                    } catch (e: Exception) {
                                        continuation.resumeIfActive(
                                            GetTOTPResult(
                                                GetTOTPResult.Failure(
                                                    GetTOTPResult.Failure.StatusCode.InternalServerError,
                                                    GetTOTPError("Could not decrypt the encrypted TOTP secret.")
                                                )
                                            )
                                        )
                                    }
                                },
                                onFailure = { errorCode, errString ->
                                    continuation.resumeIfActive(
                                        GetTOTPResult(
                                            GetTOTPResult.Failure(
                                                GetTOTPResult.Failure.StatusCode.Unauthorized,
                                                GetTOTPError("Biometric authentication failed: errorCode was $errorCode, errString was $errString")
                                            )
                                        )
                                    )
                                }
                            )))
                        } else {
                            continuation.resumeIfActive(
                                GetTOTPResult(
                                    GetTOTPResult.Failure(
                                        GetTOTPResult.Failure.StatusCode.NotFound,
                                        GetTOTPError("No biometric data found for host $host was found in the SharedPreferences.")
                                    )
                                )
                            )
                        }
                    } else {
                        continuation.resumeIfActive(GetTOTPResult(GetTOTPResult.Failure(GetTOTPResult.Failure.StatusCode.InternalServerError, GetTOTPError("The host is null.."))))
                    }
                } else {
                    continuation.resumeIfActive(
                        GetTOTPResult(
                            GetTOTPResult.Failure(
                                GetTOTPResult.Failure.StatusCode.MethodNotAllowed,
                                GetTOTPError("No biometric authentication is available or none has been set.")
                            )
                        )
                    )
                }
            }
        } catch (e: TimeoutCancellationException) {
            Timber.w(e, "Timeout for getTOTP")
            GetTOTPResult(GetTOTPResult.Failure(GetTOTPResult.Failure.StatusCode.RequestTimeout, GetTOTPError(e.message)))
        }
    }

    override suspend fun setSecureData(timeout: Long, setSecureDataRequest: SetSecureDataRequest): SetSecureDataResult {
        return try {
            withTimeout(timeout) {
                val host = getHost()
                if (host != null) {
                    val preferenceKey = getSecureDataPreferenceKey(host, setSecureDataRequest.key)
                    val encryptedValue = EncryptionUtils.encrypt(setSecureDataRequest.value)
                    sharedPreferencesModel.putString(preferenceKey, encryptedValue)
                    SetSecureDataResult(SetSecureDataResult.Success())
                } else {
                    SetSecureDataResult(SetSecureDataResult.Failure(SetSecureDataResult.Failure.StatusCode.InternalServerError, SetSecureDataError("The host is null.")))
                }
            }
        } catch (e: TimeoutCancellationException) {
            Timber.w(e, "Timeout for setSecureData")
            SetSecureDataResult(SetSecureDataResult.Failure(SetSecureDataResult.Failure.StatusCode.InternalServerError, SetSecureDataError(e.message)))
        }
    }

    override suspend fun getSecureData(timeout: Long, getSecureDataRequest: GetSecureDataRequest): GetSecureDataResult {
        return try {
            suspendCoroutineWithTimeout(timeout) { continuation ->
                if (payAuthenticationManager.isFingerprintAvailable()) {
                    val host = getHost()
                    if (host != null) {
                        val preferenceKey = getSecureDataPreferenceKey(host, getSecureDataRequest.key)
                        val encryptedValue = sharedPreferencesModel.getString(preferenceKey)
                        if (encryptedValue != null) {
                            biometricRequest.postValue(Event(BiometricRequest(
                                R.string.biometric_secure_data_title,
                                onSuccess = {
                                    try {
                                        val value = EncryptionUtils.decrypt(encryptedValue)
                                        continuation.resumeIfActive(GetSecureDataResult(GetSecureDataResult.Success(GetSecureDataResponse(value))))
                                    } catch (e: Exception) {
                                        continuation.resumeIfActive(
                                            GetSecureDataResult(
                                                GetSecureDataResult.Failure(
                                                    GetSecureDataResult.Failure.StatusCode.InternalServerError,
                                                    GetSecureDataError("Could not decrypt the encrypted secure data value.")
                                                )
                                            )
                                        )
                                    }
                                },
                                onFailure = { errorCode, errString ->
                                    continuation.resumeIfActive(
                                        GetSecureDataResult(
                                            GetSecureDataResult.Failure(
                                                GetSecureDataResult.Failure.StatusCode.Unauthorized,
                                                GetSecureDataError("Biometric authentication failed: errorCode was $errorCode, errString was $errString")
                                            )
                                        )
                                    )
                                }
                            )))
                        } else {
                            continuation.resumeIfActive(
                                GetSecureDataResult(
                                    GetSecureDataResult.Failure(
                                        GetSecureDataResult.Failure.StatusCode.NotFound,
                                        GetSecureDataError("No encrypted value with the key $preferenceKey was found in the SharedPreferences.")
                                    )
                                )
                            )
                        }
                    } else {
                        continuation.resumeIfActive(
                            GetSecureDataResult(
                                GetSecureDataResult.Failure(
                                    GetSecureDataResult.Failure.StatusCode.InternalServerError,
                                    GetSecureDataError("The host is null.")
                                )
                            )
                        )
                    }
                } else {
                    continuation.resumeIfActive(
                        GetSecureDataResult(
                            GetSecureDataResult.Failure(
                                GetSecureDataResult.Failure.StatusCode.MethodNotAllowed,
                                GetSecureDataError("No biometric authentication is available or none has been set.")
                            )
                        )
                    )
                }
            }
        } catch (e: TimeoutCancellationException) {
            Timber.w(e, "Timeout for getSecureData")
            GetSecureDataResult(GetSecureDataResult.Failure(GetSecureDataResult.Failure.StatusCode.RequestTimeout, GetSecureDataError(e.message)))
        }
    }

    override suspend fun disable(timeout: Long, disableRequest: DisableRequest): DisableResult {
        return try {
            withTimeout(timeout) {
                val host = getHost()
                if (host != null) {
                    sharedPreferencesModel.putLong(getDisableTimePreferenceKey(host), disableRequest.until.toLong())
                    eventManager.setDisabledHost(host)
                    appModel.disable(host)
                    DisableResult(DisableResult.Success())
                } else {
                    DisableResult(DisableResult.Failure(DisableResult.Failure.StatusCode.InternalServerError, DisableError("The host is null.")))
                }
            }.also { closeApp() }
        } catch (e: TimeoutCancellationException) {
            Timber.w(e, "Timeout for disable")
            DisableResult(DisableResult.Failure(DisableResult.Failure.StatusCode.RequestTimeout, DisableError(e.message)))
        }
    }

    override suspend fun openURLInNewTab(timeout: Long, openURLInNewTabRequest: OpenURLInNewTabRequest): OpenURLInNewTabResult {
        return try {
            withTimeout(timeout) {
                val redirectScheme = getRedirectScheme()
                onMainThread {
                    loadUrl.value = Event(openURLInNewTabRequest.cancelUrl)
                }

                if (!redirectScheme.isNullOrEmpty()) {
                    appModel.openUrlInNewTab(openURLInNewTabRequest.url)
                    OpenURLInNewTabResult(OpenURLInNewTabResult.Success())
                } else {
                    appModel.onCustomSchemeError(context, "${redirectScheme}://redirect")
                    OpenURLInNewTabResult(
                        OpenURLInNewTabResult.Failure(
                            OpenURLInNewTabResult.Failure.StatusCode.MethodNotAllowed,
                            OpenURLInNewTabError("Redirect scheme for deep linking has not been specified.")
                        )
                    )
                }
            }
        } catch (e: TimeoutCancellationException) {
            Timber.w(e, "Timeout for openURLInNewTab")
            OpenURLInNewTabResult(OpenURLInNewTabResult.Failure(OpenURLInNewTabResult.Failure.StatusCode.RequestTimeout, OpenURLInNewTabError(e.message)))
        }
    }

    override suspend fun verifyLocation(timeout: Long, verifyLocationRequest: VerifyLocationRequest): VerifyLocationResult {
        return try {
            withTimeout(timeout) {
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

                val targetLocation = Location("").apply { latitude = verifyLocationRequest.lat; longitude = verifyLocationRequest.lon }
                val verified = location?.let { it.distanceTo(targetLocation) <= verifyLocationRequest.threshold } ?: false
                VerifyLocationResult(VerifyLocationResult.Success(VerifyLocationResponse(verified, location?.accuracy?.toDouble())))
            }
        } catch (e: TimeoutCancellationException) {
            Timber.w(e, "Timeout for verifyLocation")
            VerifyLocationResult(VerifyLocationResult.Failure(VerifyLocationResult.Failure.StatusCode.RequestTimeout, VerifyLocationError(e.message)))
        }
    }

    override suspend fun getAccessToken(timeout: Long, getAccessTokenRequest: GetAccessTokenRequest): GetAccessTokenResult {
        return try {
            suspendCoroutineWithTimeout(timeout) { continuation ->
                val reason = getAccessTokenRequest.reason
                val invalidTokenReason = InvalidTokenReason.values().associateBy(InvalidTokenReason::value)[reason] ?: InvalidTokenReason.OTHER
                appModel.getAccessToken(invalidTokenReason, getAccessTokenRequest.oldToken) {
                    when (it) {
                        is Success -> continuation.resumeIfActive(GetAccessTokenResult(GetAccessTokenResult.Success(GetAccessTokenResponse(it.result.accessToken, it.result.isInitialToken))))
                        is Failure -> continuation.resumeIfActive(
                            GetAccessTokenResult(
                                GetAccessTokenResult.Failure(
                                    GetAccessTokenResult.Failure.StatusCode.InternalServerError,
                                    GetAccessTokenError(it.throwable.message)
                                )
                            )
                        )
                    }
                }
            }
        } catch (e: TimeoutCancellationException) {
            Timber.w(e, "Timeout for getAccessToken")
            GetAccessTokenResult(GetAccessTokenResult.Failure(GetAccessTokenResult.Failure.StatusCode.RequestTimeout, GetAccessTokenError(e.message)))
        }
    }

    override suspend fun imageData(timeout: Long, imageDataRequest: ImageDataRequest): ImageDataResult {
        return try {
            withTimeout(timeout) {
                val decodedString = Base64.decode(imageDataRequest.image, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
                appModel.onImageDataReceived(bitmap)
                ImageDataResult(ImageDataResult.Success())
            }
        } catch (e: TimeoutCancellationException) {
            Timber.w(e, "Timeout for imageData")
            ImageDataResult(ImageDataResult.Failure(ImageDataResult.Failure.StatusCode.RequestTimeout, ImageDataError(e.message)))
        }
    }

    override suspend fun applePayAvailabilityCheck(timeout: Long, applePayAvailabilityCheckRequest: ApplePayAvailabilityCheckRequest): ApplePayAvailabilityCheckResult {
        return ApplePayAvailabilityCheckResult(
            ApplePayAvailabilityCheckResult.Failure(
                ApplePayAvailabilityCheckResult.Failure.StatusCode.InternalServerError,
                ApplePayAvailabilityCheckError("applePayAvailabilityCheck is not supported on Android")
            )
        )
    }

    override suspend fun applePayRequest(timeout: Long, applePayRequestRequest: ApplePayRequestRequest): ApplePayRequestResult {
        return ApplePayRequestResult(
            ApplePayRequestResult.Failure(
                ApplePayRequestResult.Failure.StatusCode.InternalServerError,
                ApplePayRequestError("ApplePayRequestResult is not supported on Android")
            )
        )
    }

    override suspend fun back(timeout: Long): BackResult {
        return try {
            withTimeout(timeout) {
                onMainThread {
                    goBack.value = Event(Unit)
                }
                BackResult(BackResult.Success())
            }
        } catch (e: TimeoutCancellationException) {
            Timber.w(e, "Timeout for back")
            BackResult(BackResult.Failure(BackResult.Failure.StatusCode.RequestTimeout, BackError(e.message)))
        }
    }

    override suspend fun appInterceptableLink(timeout: Long): AppInterceptableLinkResult {
        return try {
            withTimeout(timeout) {
                val redirectScheme = getRedirectScheme()
                if (!redirectScheme.isNullOrEmpty()) {
                    AppInterceptableLinkResult(AppInterceptableLinkResult.Success(AppInterceptableLinkResponse(redirectScheme)))
                } else {
                    AppInterceptableLinkResult(
                        AppInterceptableLinkResult.Failure(
                            AppInterceptableLinkResult.Failure.StatusCode.NotFound,
                            AppInterceptableLinkError("Redirect scheme for deep linking has not been specified.")
                        )
                    )
                }
            }
        } catch (e: TimeoutCancellationException) {
            Timber.w(e, "Timeout for appInterceptableLink")
            AppInterceptableLinkResult(AppInterceptableLinkResult.Failure(AppInterceptableLinkResult.Failure.StatusCode.RequestTimeout, AppInterceptableLinkError(e.message)))
        }
    }

    override suspend fun setUserProperty(timeout: Long, setUserPropertyRequest: SetUserPropertyRequest): SetUserPropertyResult {
        return try {
            withTimeout(timeout) {
                appModel.setUserProperty(setUserPropertyRequest.key, setUserPropertyRequest.value, setUserPropertyRequest.update ?: false)
                SetUserPropertyResult(SetUserPropertyResult.Success())
            }
        } catch (e: TimeoutCancellationException) {
            Timber.w(e, "Timeout for setUserProperty")
            SetUserPropertyResult(SetUserPropertyResult.Failure(SetUserPropertyResult.Failure.StatusCode.RequestTimeout, SetUserPropertyError(e.message)))
        }
    }

    override suspend fun logEvent(timeout: Long, logEventRequest: LogEventRequest): LogEventResult {
        return try {
            withTimeout(timeout) {
                appModel.logEvent(logEventRequest.key, logEventRequest.parameters ?: emptyMap())
                LogEventResult(LogEventResult.Success())
            }
        } catch (e: TimeoutCancellationException) {
            Timber.w(e, "Timeout for logEvent")
            LogEventResult(LogEventResult.Failure(LogEventResult.Failure.StatusCode.RequestTimeout, LogEventError(e.message)))
        }
    }

    override suspend fun getConfig(timeout: Long, getConfigRequest: GetConfigRequest): GetConfigResult {
        return try {
            suspendCoroutineWithTimeout(timeout) { continuation ->
                appModel.getConfig(getConfigRequest.key) { config ->
                    if (config != null) {
                        continuation.resumeIfActive(GetConfigResult(GetConfigResult.Success(GetConfigResponse(config))))
                    } else {
                        continuation.resumeIfActive(GetConfigResult(GetConfigResult.Failure(GetConfigResult.Failure.StatusCode.NotFound, GetConfigError("No config value found."))))
                    }
                }
            }
        } catch (e: TimeoutCancellationException) {
            Timber.w(e, "Timeout for getConfig")
            GetConfigResult(GetConfigResult.Failure(GetConfigResult.Failure.StatusCode.RequestTimeout, GetConfigError(e.message)))
        }
    }

    override suspend fun getTraceId(timeout: Long): GetTraceIdResult {
        return try {
            withTimeout(timeout) {
                GetTraceIdResult(GetTraceIdResult.Success(GetTraceIdResponse(InterceptorUtils.getTraceId())))
            }
        } catch (e: TimeoutCancellationException) {
            Timber.w(e, "Timeout for getTraceId")
            GetTraceIdResult(GetTraceIdResult.Failure(GetTraceIdResult.Failure.StatusCode.RequestTimeout, GetTraceIdError(e.message)))
        }
    }

    override suspend fun getLocation(timeout: Long): GetLocationResult {
        return try {
            withTimeout(timeout) {
                when (val currentLocation = locationProvider.currentLocation(true)) {
                    is Success -> {
                        if (currentLocation.result != null) {
                            val bearing = currentLocation.result.bearing.toDouble()
                            GetLocationResult(
                                GetLocationResult.Success(
                                    GetLocationResponse(
                                        currentLocation.result.latitude,
                                        currentLocation.result.longitude,
                                        currentLocation.result.accuracy.toDouble(),
                                        if (bearing > 0) bearing else -1.0
                                    )
                                )
                            )
                        } else {
                            when (val validLocation = locationProvider.firstValidLocation()) {
                                is Success -> {
                                    val bearing = validLocation.result.bearing.toDouble()
                                    GetLocationResult(
                                        GetLocationResult.Success(
                                            GetLocationResponse(
                                                validLocation.result.latitude,
                                                validLocation.result.longitude,
                                                validLocation.result.accuracy.toDouble(),
                                                if (bearing > 0) bearing else -1.0
                                            )
                                        )
                                    )
                                }
                                is Failure -> {
                                    when (validLocation.throwable) {
                                        is PermissionDenied -> GetLocationResult(
                                            GetLocationResult.Failure(
                                                GetLocationResult.Failure.StatusCode.Forbidden,
                                                GetLocationError(validLocation.throwable.message)
                                            )
                                        )
                                        is NoLocationFound -> GetLocationResult(
                                            GetLocationResult.Failure(
                                                GetLocationResult.Failure.StatusCode.NotFound,
                                                GetLocationError(validLocation.throwable.message)
                                            )
                                        )
                                        else -> GetLocationResult(
                                            GetLocationResult.Failure(
                                                GetLocationResult.Failure.StatusCode.InternalServerError,
                                                GetLocationError(validLocation.throwable.message)
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                    is Failure -> {
                        when (currentLocation.throwable) {
                            is PermissionDenied -> GetLocationResult(GetLocationResult.Failure(GetLocationResult.Failure.StatusCode.Forbidden, GetLocationError(currentLocation.throwable.message)))
                            is NoLocationFound -> GetLocationResult(GetLocationResult.Failure(GetLocationResult.Failure.StatusCode.NotFound, GetLocationError(currentLocation.throwable.message)))
                            else -> GetLocationResult(GetLocationResult.Failure(GetLocationResult.Failure.StatusCode.InternalServerError, GetLocationError(currentLocation.throwable.message)))
                        }
                    }
                }
            }
        } catch (e: TimeoutCancellationException) {
            Timber.w(e, "Timeout for getLocation")
            GetLocationResult(GetLocationResult.Failure(GetLocationResult.Failure.StatusCode.RequestTimeout, GetLocationError(e.message)))
        }
    }

    override suspend fun appRedirect(timeout: Long, appRedirectRequest: AppRedirectRequest): AppRedirectResult {
        return try {
            suspendCoroutineWithTimeout(timeout) { continuation ->
                appModel.isAppRedirectAllowed(appRedirectRequest.app) {
                    if (it) {
                        continuation.resumeIfActive(AppRedirectResult(AppRedirectResult.Success()))
                    } else {
                        continuation.resumeIfActive(
                            AppRedirectResult(
                                AppRedirectResult.Failure(
                                    AppRedirectResult.Failure.StatusCode.MethodNotAllowed,
                                    AppRedirectError("The redirect was disallowed")
                                )
                            )
                        )
                    }
                }
            }
        } catch (e: TimeoutCancellationException) {
            Timber.w(e, "Timeout for appRedirect")
            AppRedirectResult(AppRedirectResult.Failure(AppRedirectResult.Failure.StatusCode.RequestTimeout, AppRedirectError(e.message)))
        }
    }

    override suspend fun isBiometricAuthEnabled(timeout: Long): IsBiometricAuthEnabledResult {
        return try {
            withTimeout(timeout) {
                IsBiometricAuthEnabledResult(IsBiometricAuthEnabledResult.Success(IsBiometricAuthEnabledResponse(IDKit.isInitialized && IDKit.isBiometricAuthenticationEnabled())))
            }
        } catch (e: TimeoutCancellationException) {
            Timber.w(e, "Timeout for isBiometricAuthEnabled")
            IsBiometricAuthEnabledResult(IsBiometricAuthEnabledResult.Failure(IsBiometricAuthEnabledResult.Failure.StatusCode.RequestTimeout, IsBiometricAuthEnabledError(e.message)))
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
}
