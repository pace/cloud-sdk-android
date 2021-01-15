package cloud.pace.sdk.appkit.app.webview

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.location.Location
import android.util.Base64
import androidx.annotation.StringRes
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import cloud.pace.sdk.PACECloudSDK
import cloud.pace.sdk.R
import cloud.pace.sdk.appkit.app.api.UriManager
import cloud.pace.sdk.appkit.app.webview.AppWebViewClient.Companion.BIOMETRIC_METHOD
import cloud.pace.sdk.appkit.app.webview.AppWebViewClient.Companion.STATE
import cloud.pace.sdk.appkit.app.webview.AppWebViewClient.Companion.STATUS_CODE
import cloud.pace.sdk.appkit.app.webview.AppWebViewClient.Companion.TOTP
import cloud.pace.sdk.appkit.app.webview.AppWebViewClient.Companion.VALUE
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
import dev.turingcomplete.kotlinonetimepassword.HmacAlgorithm
import dev.turingcomplete.kotlinonetimepassword.TimeBasedOneTimePasswordConfig
import dev.turingcomplete.kotlinonetimepassword.TimeBasedOneTimePasswordGenerator
import org.apache.commons.codec.binary.Base32
import java.util.*
import java.util.concurrent.TimeUnit

abstract class AppWebViewModel : ViewModel(), AppWebViewClient.WebClientCallback {

    abstract val touchEnable: LiveData<Boolean>
    abstract val url: LiveData<Event<String>>
    abstract val isInErrorState: LiveData<Event<Boolean>>
    abstract val broadcastIntent: LiveData<Event<Intent>>
    abstract val showLoadingIndicator: LiveData<Event<Boolean>>
    abstract val biometricRequest: LiveData<Event<BiometricRequest>>
    abstract val newToken: LiveData<Event<String>>
    abstract val verifyLocationResponse: LiveData<Event<VerifyLocationResponse>>

    abstract fun init(url: String)
    abstract fun handleInvalidToken(message: String)
    abstract fun handleImageData(message: String)
    abstract fun handleVerifyLocation(latitude: Double, longitude: Double, threshold: Double)

    class BiometricRequest(@StringRes val title: Int, val onSuccess: () -> Unit, val onFailure: () -> Unit)

    enum class VerifyLocationResponse(val value: String) {
        TRUE("true"),
        FALSE("false"),
        UNKNOWN("unknown")
    }
}

class AppWebViewModelImpl(
    private val sharedPreferencesModel: SharedPreferencesModel,
    private val uriManager: UriManager,
    private val eventManager: AppEventManager,
    private val payAuthenticationManager: PayAuthenticationManager,
    private val appModel: AppModel,
    private val appLocationManager: AppLocationManager
) : AppWebViewModel() {

    override val touchEnable = MutableLiveData<Boolean>()
    override val url = MutableLiveData<Event<String>>()
    override val isInErrorState = MutableLiveData<Event<Boolean>>()
    override val broadcastIntent = MutableLiveData<Event<Intent>>()
    override val showLoadingIndicator = MutableLiveData<Event<Boolean>>()
    override val biometricRequest = MutableLiveData<Event<BiometricRequest>>()
    override val newToken = MutableLiveData<Event<String>>()
    override val verifyLocationResponse = MutableLiveData<Event<VerifyLocationResponse>>()

    private lateinit var initialUrl: String

    override fun init(url: String) {
        initialUrl = url

        // Check whether reopen url exists
        val allStates = sharedPreferencesModel.getAppStates()
        val appState = try {
            allStates.first { it.url == url }
        } catch (e: NoSuchElementException) {
            null
        }

        val baseUrl = appState?.reopenUrl ?: initialUrl

        val startUrl = when {
            appState?.state != null -> uriManager.getURI(baseUrl, mapOf(Pair("state", appState.state)))
            else -> baseUrl
        }

        this.url.value = Event(startUrl)
    }

    override fun close(reopenRequest: AppWebViewClient.WebClientCallback.ReopenRequest?) {
        if (reopenRequest?.reopenUrl != null || reopenRequest?.state != null) {
            sharedPreferencesModel.saveAppState(SharedPreferencesModel.AppState(initialUrl, reopenRequest.reopenUrl ?: initialUrl, reopenRequest.state))
        }

        if (reopenRequest == null) {
            sharedPreferencesModel.deleteAppState(initialUrl)
        }

        eventManager.onAppDrawerChanged(initialUrl, reopenRequest?.reopenTitle, reopenRequest?.reopenSubtitle)

        appModel.close(true)
    }

    override fun getBiometricStatus(redirectUri: String?, state: String?) {
        if (redirectUri == null) return

        val status = payAuthenticationManager.isFingerprintAvailable()
        respond(redirectUri, if (status) StatusCode.Ok.code else StatusCode.NotFound.code, state)
    }

    override fun saveTotpSecret(request: AppWebViewClient.WebClientCallback.TotpSecretRequest) {
        if (request.host == null || request.redirectUri == null) return

        if (request.secret == null || request.key == null || request.algorithm == null || request.digits == null || request.period == null || stringToAlgorithm(request.algorithm) == null) {
            respond(request.redirectUri, StatusCode.InternalError.code, request.state)
            return
        }

        sharedPreferencesModel.putInt(getTotpSecretPreferenceKey(SharedPreferencesImpl.DIGITS, request.host, request.key), request.digits)
        sharedPreferencesModel.putInt(getTotpSecretPreferenceKey(SharedPreferencesImpl.PERIOD, request.host, request.key), request.period)
        sharedPreferencesModel.putString(getTotpSecretPreferenceKey(SharedPreferencesImpl.ALGORITHM, request.host, request.key), request.algorithm)

        val encryptedSecret = EncryptionUtils.encrypt(request.secret)
        sharedPreferencesModel.putString(getTotpSecretPreferenceKey(SharedPreferencesImpl.SECRET, request.host, request.key), encryptedSecret)

        respond(request.redirectUri, StatusCode.Ok.code, request.state)
    }

    override fun getTotp(host: String?, key: String?, serverTime: Long?, redirectUri: String?, state: String?) {
        if (host == null || redirectUri == null) return

        if (!payAuthenticationManager.isFingerprintAvailable()) {
            respond(redirectUri, StatusCode.NotAllowed.code, state)
            return
        }

        if (serverTime == null || key == null) {
            respond(redirectUri, StatusCode.InternalError.code, state)
            return
        }

        val encryptedSecret = sharedPreferencesModel.getString(getTotpSecretPreferenceKey(SharedPreferencesImpl.SECRET, host, key))
        val digits = sharedPreferencesModel.getInt(getTotpSecretPreferenceKey(SharedPreferencesImpl.DIGITS, host, key))
        val period = sharedPreferencesModel.getInt(getTotpSecretPreferenceKey(SharedPreferencesImpl.PERIOD, host, key))
        val algorithm = sharedPreferencesModel.getString(getTotpSecretPreferenceKey(SharedPreferencesImpl.ALGORITHM, host, key))

        if (encryptedSecret == null || digits == null || period == null || algorithm == null || stringToAlgorithm(algorithm) == null) {
            respond(redirectUri, StatusCode.NotFound.code, state)
            return
        }

        biometricRequest.value = Event(
            BiometricRequest(
                R.string.biometric_totp_title,
                onSuccess = {
                    val secret = EncryptionUtils.decrypt(encryptedSecret)
                    val config = TimeBasedOneTimePasswordConfig(
                        codeDigits = digits,
                        hmacAlgorithm = stringToAlgorithm(algorithm) ?: HmacAlgorithm.SHA1,
                        timeStep = period.toLong(),
                        timeStepUnit = TimeUnit.SECONDS
                    )
                    val timeBasedOneTimePasswordGenerator = TimeBasedOneTimePasswordGenerator(Base32().decode(secret), config)
                    val otp = timeBasedOneTimePasswordGenerator.generate(Date(serverTime * 1000))

                    val uri = uriManager.getURI(
                        redirectUri, listOfNotNull(
                            TOTP to otp,
                            // the biometric lib currently doesn't tell which method was used; set "other" for consistency
                            BIOMETRIC_METHOD to "other",
                            if (state != null) STATE to state else null
                        ).toMap()
                    )
                    url.value = Event(uri)
                },
                onFailure = {
                    respond(redirectUri, StatusCode.Unauthorized.code, state)
                })
        )
    }

    override fun setSecureData(host: String?, key: String?, value: String?, redirectUri: String?, state: String?) {
        if (host == null || redirectUri == null) return

        if (key == null || value == null) {
            respond(redirectUri, StatusCode.InternalError.code, state)
            return
        }

        val preferenceKey = getSecureDataPreferenceKey(host, key)
        val encryptedValue = EncryptionUtils.encrypt(value)
        sharedPreferencesModel.putString(preferenceKey, encryptedValue)

        respond(redirectUri, StatusCode.Ok.code, state)
    }

    override fun getSecureData(host: String?, key: String?, redirectUri: String?, state: String?) {
        if (host == null || redirectUri == null) return

        if (key == null) {
            respond(redirectUri, StatusCode.InternalError.code, state)
            return
        }

        if (!payAuthenticationManager.isFingerprintAvailable()) {
            respond(redirectUri, StatusCode.NotAllowed.code, state)
            return
        }

        val encryptedValue = sharedPreferencesModel.getString(getSecureDataPreferenceKey(host, key))

        if (encryptedValue == null) {
            respond(redirectUri, StatusCode.NotFound.code, state)
            return
        }

        biometricRequest.value = Event(BiometricRequest(
            R.string.biometric_secure_data_title,
            onSuccess = {
                val value = EncryptionUtils.decrypt(encryptedValue)
                val uri = uriManager.getURI(redirectUri, listOfNotNull(VALUE to value, if (state != null) STATE to state else null).toMap())
                url.value = Event(uri)
            },
            onFailure = {
                respond(redirectUri, StatusCode.Unauthorized.code, state)
            }
        ))
    }

    override fun setDisableTime(host: String?, until: Long?) {
        if (host == null || until == null) return

        sharedPreferencesModel.putLong(getDisableTimePreferenceKey(host), until)
        eventManager.setDisabledHost(host)
        appModel.disable(host)
    }

    override fun openInNewTab(url: String, cancelUrl: String) {
        appModel.openUrlInNewTab(url)
        this.url.value = Event(cancelUrl)
    }

    override fun onCustomSchemeError(context: Context?, cancelUrl: String, scheme: String) {
        url.value = Event(cancelUrl)
        appModel.onCustomSchemeError(context, scheme)
    }

    override fun onSwitchErrorState(isError: Boolean, isHttpError: Boolean) {
        isInErrorState.value = Event(isError)
    }

    override fun onLoadingChanged(isLoading: Boolean) {
        showLoadingIndicator.value = Event(isLoading)
    }

    override fun handleInvalidToken(message: String) {
        val initialToken = PACECloudSDK.configuration.accessToken
        if (initialToken != null && TokenValidator.isTokenValid(initialToken)) {
            PACECloudSDK.resetAccessToken()
            newToken.postValue(Event(initialToken))
        } else {
            sendOnTokenInvalid()
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

    private fun sendOnTokenInvalid() {
        appModel.onTokenInvalid { token ->
            if (TokenValidator.isTokenValid(token)) {
                PACECloudSDK.setAccessToken(token)
                newToken.postValue(Event(token))
            } else {
                sendOnTokenInvalid()
            }
        }
    }

    override fun handleVerifyLocation(latitude: Double, longitude: Double, threshold: Double) {
        appLocationManager.start { result ->
            val targetLocation = Location("").apply {
                this.latitude = latitude
                this.longitude = longitude
            }
            verifyLocationResponse.value = when (result.getOrNull()?.distanceTo(targetLocation)?.let { it <= threshold }) {
                true -> Event(VerifyLocationResponse.TRUE)
                false -> Event(VerifyLocationResponse.FALSE)
                else -> Event(VerifyLocationResponse.UNKNOWN)
            }
        }
    }

    private fun respond(redirect: String, statusCode: Int, state: String? = null) {
        val uri = uriManager.getURI(redirect, listOfNotNull(STATUS_CODE to statusCode.toString(), if (state != null) STATE to state else null).toMap())
        url.value = Event(uri)
    }

    private fun stringToAlgorithm(algorithm: String): HmacAlgorithm? {
        return when (algorithm) {
            "SHA1" -> HmacAlgorithm.SHA1
            "SHA256" -> HmacAlgorithm.SHA256
            "SHA512" -> HmacAlgorithm.SHA512
            else -> null
        }
    }

    companion object {

        fun getTotpSecretPreferenceKey(which: String, host: String, key: String) = "${which}_${host}_$key"

        fun getSecureDataPreferenceKey(host: String, key: String) = "${SharedPreferencesImpl.SECURE_DATA}_${host}_$key"

        fun getDisableTimePreferenceKey(host: String) = "${SharedPreferencesImpl.DISABLE_TIME}_$host"
    }
}
