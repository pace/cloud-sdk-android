package car.pace.cofu.ui.onboarding.twofactor.setup

import androidx.lifecycle.ViewModel
import car.pace.cofu.data.UserRepository
import car.pace.cofu.util.PinChecker
import car.pace.cofu.util.extension.PinMismatchException
import cloud.pace.sdk.idkit.model.InternalError
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import timber.log.Timber

@HiltViewModel
class TwoFactorSetupViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    suspend fun sendMailOtp(): Result<Unit> {
        return userRepository.sendMailOTP()
    }

    fun checkPin(pin: String): Result<Unit> {
        return PinChecker.checkPin(pin).onFailure {
            Timber.w("PIN check failed with $it")
        }
    }

    fun confirmPin(pin: String, confirmation: String): Result<Unit> {
        return if (pin == confirmation) {
            Result.success(Unit)
        } else {
            Timber.w("The PIN inputs do not match")
            Result.failure(PinMismatchException())
        }
    }

    suspend fun setBiometryWithOtp(otp: String): Result<Unit> {
        return userRepository.enableBiometricAuthenticationWithOTP(otp).handleResult()
    }

    suspend fun setPinWithOtp(pin: String, otp: String): Result<Unit> {
        return userRepository.setPINWithOTP(pin = pin, otp = otp).handleResult()
    }

    private fun Result<Boolean>.handleResult(): Result<Unit> {
        return map {
            if (it) {
                Result.success(Unit)
            } else {
                Result.failure(InternalError)
            }
        }
    }
}
