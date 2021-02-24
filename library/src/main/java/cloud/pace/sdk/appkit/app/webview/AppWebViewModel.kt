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
import cloud.pace.sdk.R
import cloud.pace.sdk.appkit.communication.AppEventManager
import cloud.pace.sdk.appkit.communication.AppModel
import cloud.pace.sdk.appkit.location.AppLocationManager
import cloud.pace.sdk.appkit.pay.PayAuthenticationManager
import cloud.pace.sdk.appkit.persistence.SharedPreferencesImpl
import cloud.pace.sdk.appkit.persistence.SharedPreferencesModel
import cloud.pace.sdk.appkit.utils.EncryptionUtils
import cloud.pace.sdk.appkit.utils.TokenValidator
import cloud.pace.sdk.utils.Event
import cloud.pace.sdk.utils.Log
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import dev.turingcomplete.kotlinonetimepassword.HmacAlgorithm
import dev.turingcomplete.kotlinonetimepassword.TimeBasedOneTimePasswordConfig
import dev.turingcomplete.kotlinonetimepassword.TimeBasedOneTimePasswordGenerator
import org.apache.commons.codec.binary.Base32
import java.util.*
import java.util.concurrent.TimeUnit

abstract class AppWebViewModel : ViewModel(), AppWebViewClient.WebClientCallback {

    abstract val url: LiveData<Event<String>>
    abstract val isInErrorState: LiveData<Event<Boolean>>
    abstract val showLoadingIndicator: LiveData<Event<Boolean>>
    abstract val biometricRequest: LiveData<Event<BiometricRequest>>
    abstract val newToken: LiveData<Event<String>>
    abstract val verifyLocationResponse: LiveData<Event<VerifyLocationResponse>>
    abstract val isBiometricAvailable: LiveData<Event<Boolean>>
    abstract val statusCode: LiveData<Event<StatusCodeResponse>>
    abstract val totpResponse: LiveData<Event<TOTPResponse>>
    abstract val secureData: LiveData<Event<Map<String, String>>>
    abstract val appInterceptableLink: LiveData<Event<AppInterceptableLinkResponse>>

    abstract fun init(url: String)
    abstract fun handleInvalidToken(message: String)
    abstract fun handleImageData(message: String)
    abstract fun handleVerifyLocation(message: String)
    abstract fun handleClose()
    abstract fun handleGetBiometricStatus(message: String)
    abstract fun handleSetTOTPSecret(message: String)
    abstract fun handleGetTOTP(message: String)
    abstract fun handleSetSecureData(message: String)
    abstract fun handleGetSecureData(message: String)
    abstract fun handleDisable(message: String)
    abstract fun handleOpenURLInNewTab(message: String)
    abstract fun handleGetAppInterceptableLink(message: String)

    class VerifyLocationRequest(val lat: Double, val lon: Double, val threshold: Double)
    class BiometricRequest(@StringRes val title: Int, val onSuccess: () -> Unit, val onFailure: (errorCode: Int, errString: CharSequence) -> Unit)
    class SetTOTPRequest(val secret: String, val period: Int, val digits: Int, val algorithm: String, val key: String)
    class GetTOTPRequest(val serverTime: Int, val key: String)
    class SetSecureDataRequest(val key: String, val value: String)
    class GetSecureDataRequest(val key: String)
    class DisableRequest(val until: Long)
    class OpenURLInNewTabRequest(val url: String, val cancelUrl: String)
    class TOTPResponse(val totp: String, val biometryMethod: String)
    class AppInterceptableLinkResponse(val link: String)

    sealed class StatusCodeResponse(val statusCode: Int) {
        object Success : StatusCodeResponse(StatusCode.Ok.code)
        class Failure(val error: String, statusCode: Int) : StatusCodeResponse(statusCode)
    }

    enum class VerifyLocationResponse(val value: String) {
        TRUE("true"),
        FALSE("false"),
        UNKNOWN("unknown")
    }

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
    private val appLocationManager: AppLocationManager
) : AppWebViewModel() {

    override val url = MutableLiveData<Event<String>>()
    override val isInErrorState = MutableLiveData<Event<Boolean>>()
    override val showLoadingIndicator = MutableLiveData<Event<Boolean>>()
    override val biometricRequest = MutableLiveData<Event<BiometricRequest>>()
    override val newToken = MutableLiveData<Event<String>>()
    override val verifyLocationResponse = MutableLiveData<Event<VerifyLocationResponse>>()
    override val isBiometricAvailable = MutableLiveData<Event<Boolean>>()
    override val statusCode = MutableLiveData<Event<StatusCodeResponse>>()
    override val totpResponse = MutableLiveData<Event<TOTPResponse>>()
    override val secureData = MutableLiveData<Event<Map<String, String>>>()
    override val appInterceptableLink = MutableLiveData<Event<AppInterceptableLinkResponse>>()

    private val gson = Gson()

    override fun init(url: String) {
        this.url.value = Event(url)
    }

    override fun onSwitchErrorState(isError: Boolean, isHttpError: Boolean) {
        isInErrorState.value = Event(isError)
    }

    override fun onLoadingChanged(isLoading: Boolean) {
        showLoadingIndicator.value = Event(isLoading)
    }

    override fun handleInvalidToken(message: String) {
        appModel.onTokenInvalid { token ->
            if (TokenValidator.isTokenValid(token)) {
                newToken.value = Event(token)
            } else {
                handleInvalidToken(message)
            }
        }
    }

    override fun handleImageData(message: String) {
        try {
            val decodedString = Base64.decode(message, Base64.DEFAULT)
            val bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
            appModel.onImageDataReceived(bitmap)
        } catch (e: IllegalArgumentException) {
            Log.e(e, "Could not decode the following Base64 image string: $message")
        }
    }

    override fun handleVerifyLocation(message: String) {
        try {
            val verifyLocationRequest = gson.fromJson(message, VerifyLocationRequest::class.java)

            appLocationManager.start { result ->
                val targetLocation = Location("").apply {
                    latitude = verifyLocationRequest.lat
                    longitude = verifyLocationRequest.lon
                }
                val value = when (result.getOrNull()?.distanceTo(targetLocation)?.let { it <= verifyLocationRequest.threshold }) {
                    true -> Event(VerifyLocationResponse.TRUE)
                    false -> Event(VerifyLocationResponse.FALSE)
                    else -> Event(VerifyLocationResponse.UNKNOWN)
                }
                verifyLocationResponse.value = value
            }
        } catch (e: JsonSyntaxException) {
            Log.e(e, "The verifyLocation JSON $message could not be deserialized.")
            verifyLocationResponse.value = Event(VerifyLocationResponse.UNKNOWN)
        }
    }

    override fun handleClose() {
        appModel.close(true)
    }

    override fun handleGetBiometricStatus(message: String) {
        isBiometricAvailable.value = Event(payAuthenticationManager.isFingerprintAvailable())
    }

    override fun handleSetTOTPSecret(message: String) {
        try {
            val totpRequest = gson.fromJson(message, SetTOTPRequest::class.java)
            val hmacAlgorithm = stringToAlgorithm(totpRequest.algorithm)

            if (hmacAlgorithm == null) {
                statusCode.value = Event(StatusCodeResponse.Failure("Invalid HMAC algorithm: ${totpRequest.algorithm}", StatusCode.InternalError.code))
                return
            }

            val host = getHost()
            if (host != null) {
                sharedPreferencesModel.putInt(getTotpSecretPreferenceKey(SharedPreferencesImpl.DIGITS, host, totpRequest.key), totpRequest.digits)
                sharedPreferencesModel.putInt(getTotpSecretPreferenceKey(SharedPreferencesImpl.PERIOD, host, totpRequest.key), totpRequest.period)
                sharedPreferencesModel.putString(getTotpSecretPreferenceKey(SharedPreferencesImpl.ALGORITHM, host, totpRequest.key), totpRequest.algorithm)

                try {
                    val encryptedSecret = EncryptionUtils.encrypt(totpRequest.secret)
                    sharedPreferencesModel.putString(getTotpSecretPreferenceKey(SharedPreferencesImpl.SECRET, host, totpRequest.key), encryptedSecret)
                    statusCode.value = Event(StatusCodeResponse.Success)
                } catch (e: Exception) {
                    statusCode.value = Event(StatusCodeResponse.Failure("Could not encrypt the TOTP secret.", StatusCode.InternalError.code))
                }
            } else {
                statusCode.value = Event(StatusCodeResponse.Failure("The host is null.", StatusCode.InternalError.code))
            }
        } catch (e: JsonSyntaxException) {
            statusCode.value = Event(StatusCodeResponse.Failure("The setTOTPSecret JSON $message could not be deserialized.", StatusCode.InternalError.code))
        }
    }

    override fun handleGetTOTP(message: String) {
        if (!payAuthenticationManager.isFingerprintAvailable()) {
            statusCode.value = Event(StatusCodeResponse.Failure("No biometric authentication is available or none has been set.", StatusCode.NotAllowed.code))
            return
        }

        try {
            val getTOTPRequest = gson.fromJson(message, GetTOTPRequest::class.java)
            val host = getHost()

            if (host != null) {
                val encryptedSecret = sharedPreferencesModel.getString(getTotpSecretPreferenceKey(SharedPreferencesImpl.SECRET, host, getTOTPRequest.key))
                val digits = sharedPreferencesModel.getInt(getTotpSecretPreferenceKey(SharedPreferencesImpl.DIGITS, host, getTOTPRequest.key))
                val period = sharedPreferencesModel.getInt(getTotpSecretPreferenceKey(SharedPreferencesImpl.PERIOD, host, getTOTPRequest.key))
                val algorithm = sharedPreferencesModel.getString(getTotpSecretPreferenceKey(SharedPreferencesImpl.ALGORITHM, host, getTOTPRequest.key))

                if (encryptedSecret == null || digits == null || period == null || algorithm == null || stringToAlgorithm(algorithm) == null) {
                    statusCode.value = Event(StatusCodeResponse.Failure("No encrypted secret, digits, period or algorithm was found in the SharedPreferences.", StatusCode.NotFound.code))
                    return
                }

                biometricRequest.value = Event(BiometricRequest(
                    R.string.biometric_totp_title,
                    onSuccess = {
                        try {
                            val secret = EncryptionUtils.decrypt(encryptedSecret)
                            val config = TimeBasedOneTimePasswordConfig(
                                codeDigits = digits,
                                hmacAlgorithm = stringToAlgorithm(algorithm) ?: HmacAlgorithm.SHA1,
                                timeStep = period.toLong(),
                                timeStepUnit = TimeUnit.SECONDS
                            )
                            val otp = TimeBasedOneTimePasswordGenerator(Base32().decode(secret), config).generate(Date(getTOTPRequest.serverTime * 1000L))

                            // the biometric lib currently doesn't tell which method was used; set "other" for consistency
                            totpResponse.value = Event(TOTPResponse(otp, BiometryMethod.OTHER.value))
                        } catch (e: Exception) {
                            statusCode.value = Event(StatusCodeResponse.Failure("Could not decrypt the encrypted TOTP secret.", StatusCode.InternalError.code))
                        }
                    },
                    onFailure = { errorCode, errString ->
                        statusCode.value = Event(StatusCodeResponse.Failure("Biometric authentication failed: errorCode was $errorCode, errString was $errString", StatusCode.Unauthorized.code))

                    }
                ))
            } else {
                statusCode.value = Event(StatusCodeResponse.Failure("The host is null.", StatusCode.InternalError.code))
            }
        } catch (e: JsonSyntaxException) {
            statusCode.value = Event(StatusCodeResponse.Failure("The getTOTP JSON $message could not be deserialized.", StatusCode.InternalError.code))
        }
    }

    override fun handleSetSecureData(message: String) {
        try {
            val host = getHost()
            if (host != null) {
                val setSecureDataRequest = gson.fromJson(message, SetSecureDataRequest::class.java)
                val preferenceKey = getSecureDataPreferenceKey(host, setSecureDataRequest.key)
                val encryptedValue = EncryptionUtils.encrypt(setSecureDataRequest.value)
                sharedPreferencesModel.putString(preferenceKey, encryptedValue)
                statusCode.value = Event(StatusCodeResponse.Success)
            } else {
                statusCode.value = Event(StatusCodeResponse.Failure("The host is null.", StatusCode.InternalError.code))
            }
        } catch (e: JsonSyntaxException) {
            statusCode.value = Event(StatusCodeResponse.Failure("The setSecureData JSON $message could not be deserialized.", StatusCode.InternalError.code))
        } catch (e: Exception) {
            statusCode.value = Event(StatusCodeResponse.Failure("Could not encrypt the secure data value.", StatusCode.InternalError.code))
        }
    }

    override fun handleGetSecureData(message: String) {
        if (!payAuthenticationManager.isFingerprintAvailable()) {
            statusCode.value = Event(StatusCodeResponse.Failure("No biometric authentication is available or none has been set.", StatusCode.NotAllowed.code))
            return
        }

        try {
            val host = getHost()
            if (host != null) {
                val getSecureDataRequest = gson.fromJson(message, GetSecureDataRequest::class.java)
                val preferenceKey = getSecureDataPreferenceKey(host, getSecureDataRequest.key)
                val encryptedValue = sharedPreferencesModel.getString(preferenceKey)
                if (encryptedValue == null) {
                    statusCode.value = Event(StatusCodeResponse.Failure("No encrypted value with the key $preferenceKey was found in the SharedPreferences.", StatusCode.NotFound.code))
                    return
                }

                biometricRequest.value = Event(BiometricRequest(
                    R.string.biometric_secure_data_title,
                    onSuccess = {
                        try {
                            val value = EncryptionUtils.decrypt(encryptedValue)
                            secureData.value = Event(mapOf("value" to value))
                        } catch (e: Exception) {
                            statusCode.value = Event(StatusCodeResponse.Failure("Could not decrypt the encrypted secure data value.", StatusCode.InternalError.code))
                        }
                    },
                    onFailure = { errorCode, errString ->
                        statusCode.value = Event(StatusCodeResponse.Failure("Biometric authentication failed: errorCode was $errorCode, errString was $errString", StatusCode.Unauthorized.code))
                    }
                ))
            } else {
                statusCode.value = Event(StatusCodeResponse.Failure("The host is null.", StatusCode.InternalError.code))
            }
        } catch (e: JsonSyntaxException) {
            statusCode.value = Event(StatusCodeResponse.Failure("The getSecureData JSON $message could not be deserialized.", StatusCode.InternalError.code))
        }
    }

    override fun handleDisable(message: String) {
        try {
            val disableRequest = gson.fromJson(message, DisableRequest::class.java)
            val host = getHost()
            if (host != null) {
                sharedPreferencesModel.putLong(getDisableTimePreferenceKey(host), disableRequest.until)
                eventManager.setDisabledHost(host)
                appModel.disable(host)
                statusCode.value = Event(StatusCodeResponse.Success)
            } else {
                statusCode.value = Event(StatusCodeResponse.Failure("The host is null.", StatusCode.InternalError.code))
            }
        } catch (e: JsonSyntaxException) {
            statusCode.value = Event(StatusCodeResponse.Failure("The disable JSON $message could not be deserialized.", StatusCode.InternalError.code))
        } finally {
            handleClose()
        }
    }

    override fun handleOpenURLInNewTab(message: String) {
        try {
            val redirectScheme = getRedirectScheme()
            val openURLInNewTabRequest = gson.fromJson(message, OpenURLInNewTabRequest::class.java)
            url.value = Event(openURLInNewTabRequest.cancelUrl)

            if (!redirectScheme.isNullOrEmpty()) {
                appModel.openUrlInNewTab(openURLInNewTabRequest.url)
            } else {
                appModel.onCustomSchemeError(context, "${redirectScheme}://redirect")
            }
        } catch (e: JsonSyntaxException) {
            Log.e(e, "The openURLInNewTab JSON $message could not be deserialized.")
        }
    }

    override fun handleGetAppInterceptableLink(message: String) {
        try {
            val redirectScheme = getRedirectScheme()
            if (!redirectScheme.isNullOrEmpty()) {
                appInterceptableLink.value = Event(AppInterceptableLinkResponse(redirectScheme))
            } else {
                statusCode.value = Event(StatusCodeResponse.Failure("Could not retrieve redirect scheme", StatusCode.NotFound.code))
            }
        } catch (e: Exception) {
            statusCode.value = Event(StatusCodeResponse.Failure("Could not retrieve redirect scheme", StatusCode.NotFound.code))
        }
    }

    private fun getRedirectScheme(): String? {
        val applicationInfo = context.packageManager?.getApplicationInfo(context.packageName, PackageManager.GET_META_DATA)
        return applicationInfo?.metaData?.get("pace_redirect_scheme")?.toString()
    }

    private fun stringToAlgorithm(algorithm: String): HmacAlgorithm? {
        return when (algorithm) {
            "SHA1" -> HmacAlgorithm.SHA1
            "SHA256" -> HmacAlgorithm.SHA256
            "SHA512" -> HmacAlgorithm.SHA512
            else -> null
        }
    }

    private fun getHost() = url.value?.peekContent()?.let { Uri.parse(it).host }

    companion object {

        fun getTotpSecretPreferenceKey(which: String, host: String, key: String) = "${which}_${host}_$key"

        fun getSecureDataPreferenceKey(host: String, key: String) = "${SharedPreferencesImpl.SECURE_DATA}_${host}_$key"

        fun getDisableTimePreferenceKey(host: String) = "${SharedPreferencesImpl.DISABLE_TIME}_$host"
    }
}
