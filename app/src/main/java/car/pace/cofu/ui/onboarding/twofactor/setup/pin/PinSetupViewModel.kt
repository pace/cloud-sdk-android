package car.pace.cofu.ui.onboarding.twofactor.setup.pin

import androidx.annotation.StringRes
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import car.pace.cofu.R
import car.pace.cofu.data.UserRepository
import car.pace.cofu.ui.onboarding.twofactor.setup.PinSetup
import car.pace.cofu.util.PinChecker
import car.pace.cofu.util.extension.UserCanceledException
import car.pace.cofu.util.extension.errorTextRes
import cloud.pace.sdk.idkit.model.InternalError
import cloud.pace.sdk.idkit.model.PINNotSecure
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import timber.log.Timber

@HiltViewModel
class PinSetupViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    var bottomSheetType: PinSetup? by mutableStateOf(PinSetup.PinInput)
    var errorTextRes: Int? by mutableStateOf(null)
    var loading by mutableStateOf(false)

    private val _setupFinished = MutableSharedFlow<Result<Unit>>()
    val setupFinished = _setupFinished.asSharedFlow()

    private var pin = ""

    fun next(input: String?) {
        errorTextRes = null

        if (input == null) {
            finish(Result.failure(UserCanceledException()))
            return
        }

        when (bottomSheetType) {
            PinSetup.PinInput -> checkPin(input)
            PinSetup.PinConfirmation -> confirmPin(input)
            PinSetup.OtpInput -> setPinWithOtp(input)
            else -> finish(Result.failure(UserCanceledException()))
        }
    }

    private fun checkPin(pin: String) {
        val pinCheck = PinChecker.checkPin(pin)
        if (pinCheck == PinChecker.Result.OK) {
            this.pin = pin
            nextStep()
        } else {
            Timber.w("PIN check failed with $pinCheck")
            errorTextRes = pinCheck.errorStringRes
        }
    }

    private fun confirmPin(pinConfirmation: String) {
        viewModelScope.launch {
            if (pin == pinConfirmation) {
                loading = true
                userRepository.sendMailOTP()
                    .onSuccess {
                        loading = false
                        nextStep()
                    }
                    .onFailure {
                        loading = false
                        Timber.e(it, "Failed to send OTP mail")
                        finish(Result.failure(it))
                    }
            } else {
                Timber.w("The PIN inputs do not match")
                errorTextRes = R.string.ONBOARDING_PIN_ERROR_MISMATCH
            }
        }
    }

    private fun setPinWithOtp(otp: String) {
        viewModelScope.launch {
            loading = true
            userRepository.setPINWithOTP(pin = pin.trim(), otp = otp)
                .onSuccess {
                    if (it) {
                        finish(Result.success(Unit))
                    } else {
                        loading = false
                        errorTextRes = InternalError.errorTextRes()
                    }
                }
                .onFailure {
                    loading = false
                    Timber.e(it, "Failed to set PIN with OTP")
                    if (it is PINNotSecure) {
                        restart(R.string.ONBOARDING_PIN_ERROR_NOT_SECURE)
                    } else {
                        errorTextRes = it.errorTextRes()
                    }
                }
        }
    }

    private fun nextStep() {
        bottomSheetType = bottomSheetType?.nextStep
    }

    private fun restart(@StringRes errorTextRes: Int? = null) {
        bottomSheetType = PinSetup.PinInput
        this.errorTextRes = errorTextRes
    }

    private fun finish(result: Result<Unit>) {
        viewModelScope.launch {
            restart()
            _setupFinished.emit(result)
        }
    }
}
