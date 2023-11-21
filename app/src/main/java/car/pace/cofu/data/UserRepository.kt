package car.pace.cofu.data

import car.pace.cofu.util.extension.MailNotSentException
import car.pace.cofu.util.extension.resume
import cloud.pace.sdk.idkit.IDKit
import cloud.pace.sdk.utils.Failure
import cloud.pace.sdk.utils.Success
import cloud.pace.sdk.utils.resumeIfActive
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.suspendCancellableCoroutine

@Singleton
class UserRepository @Inject constructor() {

    fun isAuthorizationValid() = IDKit.isAuthorizationValid()

    fun isBiometricAuthenticationEnabled() = IDKit.isBiometricAuthenticationEnabled()

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

    suspend fun sendMailOTP(): Result<Unit> = suspendCancellableCoroutine {
        IDKit.sendMailOTP { completion ->
            val success = (completion as? Success)?.result
            if (success != null) {
                it.resumeIfActive(Result.success(Unit))
            } else {
                val throwable = (completion as? Failure)?.throwable ?: MailNotSentException()
                it.resumeIfActive(Result.failure(throwable))
            }
        }
    }
}
