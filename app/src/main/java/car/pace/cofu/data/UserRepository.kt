package car.pace.cofu.data

import androidx.appcompat.app.AppCompatActivity
import car.pace.cofu.data.analytics.Analytics
import car.pace.cofu.util.RequestUtils.getHeaders
import car.pace.cofu.util.extension.MailNotSentException
import car.pace.cofu.util.extension.resume
import cloud.pace.sdk.api.API
import cloud.pace.sdk.api.user.UserAPI.totp
import cloud.pace.sdk.api.user.generated.request.totp.SendmailOTPAPI.sendmailOTP
import cloud.pace.sdk.idkit.IDKit
import cloud.pace.sdk.idkit.model.InternalError
import cloud.pace.sdk.idkit.model.InvalidSession
import java.net.HttpURLConnection
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.suspendCancellableCoroutine
import retrofit2.awaitResponse

@Singleton
class UserRepository @Inject constructor(
    private val sharedPreferencesRepository: SharedPreferencesRepository,
    private val analytics: Analytics
) {

    fun isAuthorizationValid() = IDKit.isAuthorizationValid()

    fun isBiometricAuthenticationEnabled() = IDKit.isBiometricAuthenticationEnabled()

    fun disableBiometricAuthentication() = IDKit.disableBiometricAuthentication()

    suspend fun refreshToken(force: Boolean = false) = suspendCancellableCoroutine {
        IDKit.refreshToken(force, it::resume)
    }

    suspend fun enableBiometricAuthentication() = suspendCancellableCoroutine {
        IDKit.enableBiometricAuthentication(it::resume)
    }

    suspend fun enableBiometricAuthenticationWithOTP(otp: String) = suspendCancellableCoroutine {
        IDKit.enableBiometricAuthenticationWithOTP(otp, it::resume)
    }

    suspend fun isPINSet() = suspendCancellableCoroutine {
        IDKit.isPINSet(it::resume)
    }

    suspend fun setPINWithOTP(pin: String, otp: String) = suspendCancellableCoroutine {
        IDKit.setPINWithOTP(pin, otp, it::resume)
    }

    suspend fun sendMailOTP(): Result<Unit> {
        val response = runCatching {
            API.totp.sendmailOTP(additionalHeaders = getHeaders()).awaitResponse()
        }

        return response.fold(
            onSuccess = {
                when {
                    it.isSuccessful -> Result.success(Unit)
                    it.code() >= HttpURLConnection.HTTP_INTERNAL_ERROR -> Result.failure(InternalError)
                    it.code() == HttpURLConnection.HTTP_UNAUTHORIZED -> Result.failure(InvalidSession)
                    else -> Result.failure(MailNotSentException())
                }
            },
            onFailure = {
                Result.failure(it)
            }
        )
    }

    suspend fun resetAppData(activity: AppCompatActivity) {
        IDKit.endSession(activity)
        analytics.enableAnalyticsFeature(null, false)
        sharedPreferencesRepository.clear()
    }
}
