package cloud.pace.sdk.idkit.credentials

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import cloud.pace.sdk.api.API
import cloud.pace.sdk.api.user.UserAPI.credentials
import cloud.pace.sdk.api.user.UserAPI.totp
import cloud.pace.sdk.api.user.generated.model.CreateOTP
import cloud.pace.sdk.api.user.generated.model.DeviceTOTPBody
import cloud.pace.sdk.api.user.generated.model.PinOrPassword
import cloud.pace.sdk.api.user.generated.model.UserPINAndOTPBody
import cloud.pace.sdk.api.user.generated.request.credentials.CheckUserPINAPI.checkUserPIN
import cloud.pace.sdk.api.user.generated.request.credentials.CheckUserPasswordAPI.checkUserPassword
import cloud.pace.sdk.api.user.generated.request.credentials.CheckUserPinOrPasswordAPI.checkUserPinOrPassword
import cloud.pace.sdk.api.user.generated.request.credentials.UpdateUserPINAPI
import cloud.pace.sdk.api.user.generated.request.credentials.UpdateUserPINAPI.updateUserPIN
import cloud.pace.sdk.api.user.generated.request.totp.CreateOTPAPI.createOTP
import cloud.pace.sdk.api.user.generated.request.totp.CreateTOTPAPI
import cloud.pace.sdk.api.user.generated.request.totp.CreateTOTPAPI.createTOTP
import cloud.pace.sdk.api.user.generated.request.totp.SendmailOTPAPI.sendmailOTP
import cloud.pace.sdk.appkit.pay.PayAuthenticationManager
import cloud.pace.sdk.appkit.persistence.SharedPreferencesModel
import cloud.pace.sdk.appkit.persistence.TotpSecret
import cloud.pace.sdk.appkit.utils.BiometricUtils
import cloud.pace.sdk.appkit.utils.EncryptionUtils
import cloud.pace.sdk.appkit.utils.EncryptionUtils.generateOTP
import cloud.pace.sdk.idkit.authorization.AuthorizationManager
import cloud.pace.sdk.idkit.model.BiometricAuthenticationNotSet
import cloud.pace.sdk.idkit.model.BiometricAuthenticationNotSupported
import cloud.pace.sdk.idkit.model.InternalError
import cloud.pace.sdk.idkit.model.InvalidSession
import cloud.pace.sdk.idkit.model.PINNotSecure
import cloud.pace.sdk.poikit.utils.ApiException
import cloud.pace.sdk.utils.CloudSDKKoinComponent
import cloud.pace.sdk.utils.Completion
import cloud.pace.sdk.utils.Failure
import cloud.pace.sdk.utils.Success
import cloud.pace.sdk.utils.enqueue
import cloud.pace.sdk.utils.requestId
import retrofit2.Call
import timber.log.Timber
import java.net.HttpURLConnection
import java.util.UUID

internal class CredentialsManager(
    private val sharedPreferencesModel: SharedPreferencesModel,
    private val payAuthenticationManager: PayAuthenticationManager,
    private val authorizationManager: AuthorizationManager
) : CloudSDKKoinComponent {

    internal fun isBiometricAuthenticationEnabled() = authorizationManager.cachedToken() != null && sharedPreferencesModel.getTotpSecret() != null

    internal fun enableBiometricAuthentication(pin: String? = null, password: String? = null, otp: String? = null, completion: (Completion<Boolean>) -> Unit = {}) {
        if (setAuthorizationHeader()) {
            API.totp.createTOTP(CreateTOTPAPI.Body().apply { data = getDeviceTOTPBody(pin, password, otp) }).enqueue {
                onResponse = {
                    val body = it.body()
                    when {
                        it.isSuccessful && body != null -> {
                            val secret = body.secret
                            val digits = body.digits
                            val period = body.period
                            val algorithm = body.algorithm
                            if (secret != null && digits != null && period != null && algorithm != null) {
                                try {
                                    sharedPreferencesModel.setTotpSecret(totpSecret = TotpSecret(EncryptionUtils.encrypt(secret), digits, period, algorithm.value))
                                    completion(Success(true))
                                } catch (e: Exception) {
                                    completion(Failure(e))
                                }
                            } else {
                                completion(Failure(InternalError))
                            }
                        }
                        it.code() >= HttpURLConnection.HTTP_INTERNAL_ERROR -> completion(Failure(InternalError))
                        it.code() == HttpURLConnection.HTTP_UNAUTHORIZED -> completion(Failure(InvalidSession))
                        else -> completion(Success(false))
                    }
                }

                onFailure = {
                    completion(Failure(it ?: InternalError))
                }
            }
        } else {
            completion(Failure(InvalidSession))
        }
    }

    internal fun disableBiometricAuthentication() = sharedPreferencesModel.removeTotpSecret()

    internal fun isPINSet(completion: (Completion<Boolean>) -> Unit) {
        if (setAuthorizationHeader()) {
            API.credentials.checkUserPIN().makeBooleanRequest(completion)
        } else {
            completion(Failure(InvalidSession))
        }
    }

    internal fun isPasswordSet(completion: (Completion<Boolean>) -> Unit) {
        if (setAuthorizationHeader()) {
            API.credentials.checkUserPassword().makeBooleanRequest(completion)
        } else {
            completion(Failure(InvalidSession))
        }
    }

    internal fun isPINOrPasswordSet(completion: (Completion<PinOrPassword>) -> Unit) {
        if (setAuthorizationHeader()) {
            API.credentials.checkUserPinOrPassword().enqueue {
                onResponse = {
                    val body = it.body()
                    when {
                        it.isSuccessful && body != null -> completion(Success(body))
                        it.code() >= HttpURLConnection.HTTP_INTERNAL_ERROR -> completion(Failure(InternalError))
                        it.code() == HttpURLConnection.HTTP_UNAUTHORIZED -> completion(Failure(InvalidSession))
                        else -> completion(Failure(ApiException(it.code(), it.message(), it.requestId)))
                    }
                }

                onFailure = {
                    completion(Failure(it ?: InternalError))
                }
            }
        } else {
            completion(Failure(InvalidSession))
        }
    }

    internal fun setPINWithBiometry(
        fragment: Fragment,
        title: String,
        subTitle: String?,
        cancelText: String?,
        isDeviceCredentialsAllowed: Boolean,
        pin: String,
        completion: (Completion<Boolean>) -> Unit
    ) {
        if (payAuthenticationManager.isFingerprintAvailable()) {
            if (setAuthorizationHeader()) {
                val totpSecret = sharedPreferencesModel.getTotpSecret()
                if (totpSecret != null) {
                    BiometricUtils.requestAuthentication(
                        fragment, title, subTitle, cancelText, isDeviceCredentialsAllowed,
                        onSuccess = { updateUserPIN(pin, totpSecret, completion) },
                        onFailure = { errorCode, errString -> completion(Failure(Throwable("Error code = $errorCode error message = $errString"))) }
                    )
                } else {
                    completion(Failure(BiometricAuthenticationNotSet))
                }
            } else {
                completion(Failure(InvalidSession))
            }
        } else {
            completion(Failure(BiometricAuthenticationNotSupported))
        }
    }

    internal fun setPINWithBiometry(
        activity: FragmentActivity,
        title: String,
        subTitle: String?,
        cancelText: String?,
        isDeviceCredentialsAllowed: Boolean,
        pin: String,
        completion: (Completion<Boolean>) -> Unit
    ) {
        if (payAuthenticationManager.isFingerprintAvailable()) {
            if (setAuthorizationHeader()) {
                val totpSecret = sharedPreferencesModel.getTotpSecret()
                if (totpSecret != null) {
                    BiometricUtils.requestAuthentication(
                        activity, title, subTitle, cancelText, isDeviceCredentialsAllowed,
                        onSuccess = { updateUserPIN(pin, totpSecret, completion) },
                        onFailure = { errorCode, errString -> completion(Failure(Throwable("Error code = $errorCode error message = $errString"))) }
                    )
                } else {
                    completion(Failure(BiometricAuthenticationNotSet))
                }
            } else {
                completion(Failure(InvalidSession))
            }
        } else {
            completion(Failure(BiometricAuthenticationNotSupported))
        }
    }

    private fun updateUserPIN(pin: String, totpSecret: TotpSecret, completion: (Completion<Boolean>) -> Unit) {
        try {
            val decryptedSecret = EncryptionUtils.decrypt(totpSecret.encryptedSecret)
            val generatedOtp = generateOTP(decryptedSecret, totpSecret.digits, totpSecret.period, totpSecret.algorithm)
            API.credentials.updateUserPIN(UpdateUserPINAPI.Body().apply { data = getUserPINBody(pin, generatedOtp) }).makeBooleanRequest(completion)
        } catch (e: Exception) {
            Timber.e(e, "Could not decrypt the secret while updating the user PIN")
            completion(Failure(e))
        }
    }

    internal fun setPINWithPassword(pin: String, password: String, completion: (Completion<Boolean>) -> Unit) {
        if (setAuthorizationHeader()) {
            API.totp.createOTP(CreateOTP().apply { this.password = password }).enqueue {
                onResponse = {
                    val body = it.body()
                    when {
                        it.isSuccessful && body != null -> {
                            val otp = body.otp
                            if (otp != null) {
                                API.credentials.updateUserPIN(UpdateUserPINAPI.Body().apply { data = getUserPINBody(pin, otp) }).makeBooleanRequest(completion)
                            } else {
                                completion(Failure(InternalError))
                            }
                        }
                        it.code() >= HttpURLConnection.HTTP_INTERNAL_ERROR -> completion(Failure(InternalError))
                        it.code() == HttpURLConnection.HTTP_UNAUTHORIZED -> completion(Failure(InvalidSession))
                        else -> completion(Failure(ApiException(it.code(), it.message(), it.requestId)))
                    }
                }

                onFailure = {
                    completion(Failure(it ?: InternalError))
                }
            }
        } else {
            completion(Failure(InvalidSession))
        }
    }

    internal fun setPINWithOTP(pin: String, otp: String, completion: (Completion<Boolean>) -> Unit) {
        if (setAuthorizationHeader()) {
            API.totp.createTOTP(CreateTOTPAPI.Body().apply { data = getDeviceTOTPBody(otp = otp) }).enqueue {
                onResponse = {
                    val body = it.body()
                    when {
                        it.isSuccessful && body != null -> {
                            val decryptedSecret = body.secret
                            val digits = body.digits
                            val period = body.period
                            val algorithm = body.algorithm
                            if (decryptedSecret != null && digits != null && period != null && algorithm != null) {
                                try {
                                    val generatedOtp = generateOTP(decryptedSecret, digits, period, algorithm.value)
                                    API.credentials.updateUserPIN(UpdateUserPINAPI.Body().apply { data = getUserPINBody(pin, generatedOtp) }).makeBooleanRequest(completion)
                                } catch (e: Exception) {
                                    completion(Failure(e))
                                }
                            } else {
                                completion(Failure(InternalError))
                            }
                        }
                        it.code() >= HttpURLConnection.HTTP_INTERNAL_ERROR -> completion(Failure(InternalError))
                        it.code() == HttpURLConnection.HTTP_UNAUTHORIZED -> completion(Failure(InvalidSession))
                        else -> completion(Failure(ApiException(it.code(), it.message(), it.requestId)))
                    }
                }

                onFailure = {
                    completion(Failure(it ?: InternalError))
                }
            }
        } else {
            completion(Failure(InvalidSession))
        }
    }

    internal fun isPINValid(pin: String): Boolean {
        val charChain = "0123456789012"
        val isCorrectLength = pin.length == 4
        val isDistinct = pin.toCharArray().distinct().count() >= 3
        val isNoChain = !charChain.contains(pin) && !charChain.reversed().contains(pin)

        return isCorrectLength && isDistinct && isNoChain
    }

    internal fun sendMailOTP(completion: (Completion<Boolean>) -> Unit) {
        if (setAuthorizationHeader()) {
            API.totp.sendmailOTP().makeBooleanRequest(completion)
        } else {
            completion(Failure(InvalidSession))
        }
    }

    private fun setAuthorizationHeader(): Boolean {
        val accessToken = authorizationManager.cachedToken()
        return if (accessToken != null) {
            API.addAuthorizationHeader(accessToken)
            true
        } else false
    }

    private fun getUserPINBody(pin: String, otp: String) =
        UserPINAndOTPBody().apply {
            id = UUID.randomUUID().toString()
            type = UserPINAndOTPBody.Type.PIN
            attributes = UserPINAndOTPBody.Attributes().apply {
                this.pin = pin
                this.otp = otp
            }
        }

    private fun getDeviceTOTPBody(pin: String? = null, password: String? = null, otp: String? = null) =
        DeviceTOTPBody().apply {
            id = UUID.randomUUID().toString()
            type = DeviceTOTPBody.Type.DEVICETOTP
            attributes = DeviceTOTPBody.Attributes().apply {
                this.pin = pin
                this.password = password
                this.otp = otp
            }
        }

    private fun <T> Call<T>.makeBooleanRequest(completion: (Completion<Boolean>) -> Unit) =
        enqueue {
            onResponse = {
                when {
                    it.isSuccessful -> completion(Success(true))
                    it.code() >= HttpURLConnection.HTTP_INTERNAL_ERROR -> completion(Failure(InternalError))
                    it.code() == HttpURLConnection.HTTP_UNAUTHORIZED -> completion(Failure(InvalidSession))
                    it.code() == HttpURLConnection.HTTP_NOT_ACCEPTABLE -> completion(Failure(PINNotSecure))
                    else -> completion(Success(false))
                }
            }

            onFailure = {
                completion(Failure(it ?: InternalError))
            }
        }
}
