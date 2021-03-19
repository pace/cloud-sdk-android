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
import com.google.gson.reflect.TypeToken
import dev.turingcomplete.kotlinonetimepassword.HmacAlgorithm
import dev.turingcomplete.kotlinonetimepassword.TimeBasedOneTimePasswordConfig
import dev.turingcomplete.kotlinonetimepassword.TimeBasedOneTimePasswordGenerator
import org.apache.commons.codec.binary.Base32
import java.util.*
import java.util.concurrent.TimeUnit

abstract class AppWebViewModel : ViewModel(), AppWebViewClient.WebClientCallback {

    class MessageBundle<T>(val id: String, val message: T)
    class ResponseEvent<T>(id: String, content: T) : Event<MessageBundle<T>>(MessageBundle(id, content))

    abstract val url: LiveData<Event<String>>
    abstract val isInErrorState: LiveData<Event<Boolean>>
    abstract val showLoadingIndicator: LiveData<Event<Boolean>>
    abstract val biometricRequest: LiveData<Event<BiometricRequest>>
    abstract val newToken: LiveData<ResponseEvent<String>>
    abstract val verifyLocationResponse: LiveData<ResponseEvent<String>>
    abstract val isBiometricAvailable: LiveData<ResponseEvent<Boolean>>
    abstract val statusCode: LiveData<ResponseEvent<StatusCodeResponse>>
    abstract val totpResponse: LiveData<ResponseEvent<TOTPResponse>>
    abstract val secureData: LiveData<ResponseEvent<Map<String, String>>>
    abstract val appInterceptableLink: LiveData<ResponseEvent<AppInterceptableLinkResponse>>

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
    override val newToken = MutableLiveData<ResponseEvent<String>>()
    override val verifyLocationResponse = MutableLiveData<ResponseEvent<String>>()
    override val isBiometricAvailable = MutableLiveData<ResponseEvent<Boolean>>()
    override val statusCode = MutableLiveData<ResponseEvent<StatusCodeResponse>>()
    override val totpResponse = MutableLiveData<ResponseEvent<TOTPResponse>>()
    override val secureData = MutableLiveData<ResponseEvent<Map<String, String>>>()
    override val appInterceptableLink = MutableLiveData<ResponseEvent<AppInterceptableLinkResponse>>()

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

    private inline fun <reified T> Gson.fromJson(json: String) = fromJson<T>(json, object : TypeToken<T>() {}.type)

    override fun handleInvalidToken(message: String) {
        val requestBundle = gson.fromJson<MessageBundle<Any>>(message)
        appModel.onTokenInvalid { token ->
            if (TokenValidator.isTokenValid(token)) {
                newToken.value = ResponseEvent(requestBundle.id, token)
            } else {
                handleInvalidToken(message)
            }
        }
    }

    override fun handleImageData(message: String) {
        val messageBundle = gson.fromJson<MessageBundle<String>>(message)
        try {
            val decodedString = Base64.decode(messageBundle.message, Base64.DEFAULT)
            val bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
            appModel.onImageDataReceived(bitmap)
        } catch (e: IllegalArgumentException) {
            Log.e(e, "Could not decode the following Base64 image string: $message")
        }
    }

    override fun handleVerifyLocation(message: String) {
        val messageBundle = gson.fromJson<MessageBundle<Any>>(message)
        try {
            val verifyLocationRequest = gson.fromJson<MessageBundle<VerifyLocationRequest>>(message)

            appLocationManager.start { result ->
                val targetLocation = Location("").apply {
                    latitude = verifyLocationRequest.message.lat
                    longitude = verifyLocationRequest.message.lon
                }
                val value = when (result.getOrNull()?.distanceTo(targetLocation)?.let { it <= verifyLocationRequest.message.threshold }) {
                    true -> VerifyLocationResponse.TRUE
                    false -> VerifyLocationResponse.FALSE
                    else -> VerifyLocationResponse.UNKNOWN
                }
                verifyLocationResponse.value = ResponseEvent(verifyLocationRequest.id, value.value)
            }
        } catch (e: JsonSyntaxException) {
            Log.e(e, "The verifyLocation JSON $message could not be deserialized.")
            verifyLocationResponse.value = ResponseEvent(messageBundle.id, VerifyLocationResponse.UNKNOWN.value)
        }
    }

    override fun handleClose() {
        appModel.close(true)
    }

    override fun handleGetBiometricStatus(message: String) {
        val messageBundle = gson.fromJson<MessageBundle<String>>(message)
        isBiometricAvailable.value = ResponseEvent(messageBundle.id, payAuthenticationManager.isFingerprintAvailable())
    }

    override fun handleSetTOTPSecret(message: String) {
        val messageBundle = gson.fromJson<MessageBundle<Any>>(message)
        try {
            val totpRequest = gson.fromJson<MessageBundle<SetTOTPRequest>>(message)
            val hmacAlgorithm = stringToAlgorithm(totpRequest.message.algorithm)

            if (hmacAlgorithm == null) {
                statusCode.value = ResponseEvent(totpRequest.id, StatusCodeResponse.Failure("Invalid HMAC algorithm: ${totpRequest.message.algorithm}", StatusCode.InternalError.code))
                return
            }

            val host = getHost()
            if (host != null) {
                sharedPreferencesModel.putInt(getTotpSecretPreferenceKey(SharedPreferencesImpl.DIGITS, host, totpRequest.message.key), totpRequest.message.digits)
                sharedPreferencesModel.putInt(getTotpSecretPreferenceKey(SharedPreferencesImpl.PERIOD, host, totpRequest.message.key), totpRequest.message.period)
                sharedPreferencesModel.putString(getTotpSecretPreferenceKey(SharedPreferencesImpl.ALGORITHM, host, totpRequest.message.key), totpRequest.message.algorithm)

                try {
                    val encryptedSecret = EncryptionUtils.encrypt(totpRequest.message.secret)
                    sharedPreferencesModel.putString(getTotpSecretPreferenceKey(SharedPreferencesImpl.SECRET, host, totpRequest.message.key), encryptedSecret)
                    statusCode.value = ResponseEvent(totpRequest.id, StatusCodeResponse.Success)
                } catch (e: Exception) {
                    statusCode.value = ResponseEvent(totpRequest.id, StatusCodeResponse.Failure("Could not encrypt the TOTP secret.", StatusCode.InternalError.code))
                }
            } else {
                statusCode.value = ResponseEvent(totpRequest.id, StatusCodeResponse.Failure("The host is null.", StatusCode.InternalError.code))
            }
        } catch (e: JsonSyntaxException) {
            statusCode.value = ResponseEvent(messageBundle.id, StatusCodeResponse.Failure("The setTOTPSecret JSON $message could not be deserialized.", StatusCode.InternalError.code))
        }
    }

    override fun handleGetTOTP(message: String) {
        val messageBundle = gson.fromJson<MessageBundle<Any>>(message)
        if (!payAuthenticationManager.isFingerprintAvailable()) {
            statusCode.value = ResponseEvent(messageBundle.id, StatusCodeResponse.Failure("No biometric authentication is available or none has been set.", StatusCode.NotAllowed.code))
            return
        }

        try {
            val getTOTPRequest = gson.fromJson<MessageBundle<GetTOTPRequest>>(message)
            val host = getHost()

            if (host != null) {
                val encryptedSecret = sharedPreferencesModel.getString(getTotpSecretPreferenceKey(SharedPreferencesImpl.SECRET, host, getTOTPRequest.message.key))
                val digits = sharedPreferencesModel.getInt(getTotpSecretPreferenceKey(SharedPreferencesImpl.DIGITS, host, getTOTPRequest.message.key))
                val period = sharedPreferencesModel.getInt(getTotpSecretPreferenceKey(SharedPreferencesImpl.PERIOD, host, getTOTPRequest.message.key))
                val algorithm = sharedPreferencesModel.getString(getTotpSecretPreferenceKey(SharedPreferencesImpl.ALGORITHM, host, getTOTPRequest.message.key))

                if (encryptedSecret == null || digits == null || period == null || algorithm == null || stringToAlgorithm(algorithm) == null) {
                    statusCode.value =
                        ResponseEvent(getTOTPRequest.id, StatusCodeResponse.Failure("No encrypted secret, digits, period or algorithm was found in the SharedPreferences.", StatusCode.NotFound.code))
                    return
                }

                biometricRequest.value = Event(
                    BiometricRequest(
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
                                val otp = TimeBasedOneTimePasswordGenerator(Base32().decode(secret), config).generate(Date(getTOTPRequest.message.serverTime * 1000L))

                                // the biometric lib currently doesn't tell which method was used; set "other" for consistency
                                totpResponse.value = ResponseEvent(getTOTPRequest.id, TOTPResponse(otp, BiometryMethod.OTHER.value))
                            } catch (e: Exception) {
                                statusCode.value = ResponseEvent(getTOTPRequest.id, StatusCodeResponse.Failure("Could not decrypt the encrypted TOTP secret.", StatusCode.InternalError.code))
                            }
                        },
                        onFailure = { errorCode, errString ->
                            statusCode.value = ResponseEvent(
                                getTOTPRequest.id,
                                StatusCodeResponse.Failure("Biometric authentication failed: errorCode was $errorCode, errString was $errString", StatusCode.Unauthorized.code)
                            )

                        }
                    ))
            } else {
                statusCode.value = ResponseEvent(getTOTPRequest.id, StatusCodeResponse.Failure("The host is null.", StatusCode.InternalError.code))
            }
        } catch (e: JsonSyntaxException) {
            statusCode.value = ResponseEvent(messageBundle.id, StatusCodeResponse.Failure("The getTOTP JSON $message could not be deserialized.", StatusCode.InternalError.code))
        }
    }

    override fun handleSetSecureData(message: String) {
        val messageBundle = gson.fromJson<MessageBundle<Any>>(message)
        try {
            val host = getHost()
            if (host != null) {
                val setSecureDataRequest = gson.fromJson<MessageBundle<SetSecureDataRequest>>(message)
                val preferenceKey = getSecureDataPreferenceKey(host, setSecureDataRequest.message.key)
                val encryptedValue = EncryptionUtils.encrypt(setSecureDataRequest.message.value)
                sharedPreferencesModel.putString(preferenceKey, encryptedValue)
                statusCode.value = ResponseEvent(setSecureDataRequest.id, StatusCodeResponse.Success)
            } else {
                statusCode.value = ResponseEvent(messageBundle.id, StatusCodeResponse.Failure("The host is null.", StatusCode.InternalError.code))
            }
        } catch (e: JsonSyntaxException) {
            statusCode.value = ResponseEvent(messageBundle.id, StatusCodeResponse.Failure("The setSecureData JSON $message could not be deserialized.", StatusCode.InternalError.code))
        } catch (e: Exception) {
            statusCode.value = ResponseEvent(messageBundle.id, StatusCodeResponse.Failure("Could not encrypt the secure data value.", StatusCode.InternalError.code))
        }
    }

    override fun handleGetSecureData(message: String) {
        val messageBundle = gson.fromJson<MessageBundle<Any>>(message)
        if (!payAuthenticationManager.isFingerprintAvailable()) {
            statusCode.value = ResponseEvent(messageBundle.id, StatusCodeResponse.Failure("No biometric authentication is available or none has been set.", StatusCode.NotAllowed.code))
            return
        }

        try {
            val host = getHost()
            if (host != null) {
                val getSecureDataRequest = gson.fromJson<MessageBundle<GetSecureDataRequest>>(message)
                val preferenceKey = getSecureDataPreferenceKey(host, getSecureDataRequest.message.key)
                val encryptedValue = sharedPreferencesModel.getString(preferenceKey)
                if (encryptedValue == null) {
                    statusCode.value = ResponseEvent(
                        getSecureDataRequest.id,
                        StatusCodeResponse.Failure("No encrypted value with the key $preferenceKey was found in the SharedPreferences.", StatusCode.NotFound.code)
                    )
                    return
                }

                biometricRequest.value = Event(
                    BiometricRequest(
                        R.string.biometric_secure_data_title,
                        onSuccess = {
                            try {
                                val value = EncryptionUtils.decrypt(encryptedValue)
                                secureData.value = ResponseEvent(getSecureDataRequest.id, mapOf("value" to value))
                            } catch (e: Exception) {
                                statusCode.value =
                                    ResponseEvent(getSecureDataRequest.id, StatusCodeResponse.Failure("Could not decrypt the encrypted secure data value.", StatusCode.InternalError.code))
                            }
                        },
                        onFailure = { errorCode, errString ->
                            statusCode.value = ResponseEvent(
                                getSecureDataRequest.id,
                                StatusCodeResponse.Failure("Biometric authentication failed: errorCode was $errorCode, errString was $errString", StatusCode.Unauthorized.code)
                            )
                        }
                    ))
            } else {
                statusCode.value = ResponseEvent(messageBundle.id, StatusCodeResponse.Failure("The host is null.", StatusCode.InternalError.code))
            }
        } catch (e: JsonSyntaxException) {
            statusCode.value = ResponseEvent(messageBundle.id, StatusCodeResponse.Failure("The getSecureData JSON $message could not be deserialized.", StatusCode.InternalError.code))
        }
    }

    override fun handleDisable(message: String) {
        val messageBundle = gson.fromJson<MessageBundle<Any>>(message)
        try {
            val disableRequest = gson.fromJson<MessageBundle<DisableRequest>>(message)
            val host = getHost()
            if (host != null) {
                sharedPreferencesModel.putLong(getDisableTimePreferenceKey(host), disableRequest.message.until)
                eventManager.setDisabledHost(host)
                appModel.disable(host)
                statusCode.value = ResponseEvent(messageBundle.id, StatusCodeResponse.Success)
            } else {
                statusCode.value = ResponseEvent(messageBundle.id, StatusCodeResponse.Failure("The host is null.", StatusCode.InternalError.code))
            }
        } catch (e: JsonSyntaxException) {
            statusCode.value = ResponseEvent(messageBundle.id, StatusCodeResponse.Failure("The disable JSON $message could not be deserialized.", StatusCode.InternalError.code))
        } finally {
            handleClose()
        }
    }

    override fun handleOpenURLInNewTab(message: String) {
        try {
            val redirectScheme = getRedirectScheme()
            val openURLInNewTabRequest = gson.fromJson<MessageBundle<OpenURLInNewTabRequest>>(message)
            url.value = Event(openURLInNewTabRequest.message.cancelUrl)

            if (!redirectScheme.isNullOrEmpty()) {
                appModel.openUrlInNewTab(openURLInNewTabRequest.message.url)
            } else {
                appModel.onCustomSchemeError(context, "${redirectScheme}://redirect")
            }
        } catch (e: JsonSyntaxException) {
            Log.e(e, "The openURLInNewTab JSON $message could not be deserialized.")
        }
    }

    override fun handleGetAppInterceptableLink(message: String) {
        val messageBundle = gson.fromJson<MessageBundle<Any>>(message)
        try {
            val redirectScheme = getRedirectScheme()
            if (!redirectScheme.isNullOrEmpty()) {
                appInterceptableLink.value = ResponseEvent(messageBundle.id, AppInterceptableLinkResponse(redirectScheme))
            } else {
                statusCode.value = ResponseEvent(messageBundle.id, StatusCodeResponse.Failure("Could not retrieve redirect scheme", StatusCode.NotFound.code))
            }
        } catch (e: Exception) {
            statusCode.value = ResponseEvent(messageBundle.id, StatusCodeResponse.Failure("Could not retrieve redirect scheme", StatusCode.NotFound.code))
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
