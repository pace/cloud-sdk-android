package car.pace.cofu.ui.onboarding.dialog

import android.text.InputType
import car.pace.cofu.R
import car.pace.cofu.core.util.decrease
import car.pace.cofu.core.util.increase
import cloud.pace.sdk.idkit.IDKit
import cloud.pace.sdk.idkit.model.PINNotSecure
import cloud.pace.sdk.utils.Failure
import cloud.pace.sdk.utils.Success

class ConfigurePinViewModel : BaseOnboardingBottomSheetViewModel() {

    private var step = Step.ASK_FOR_PIN
    private var pin: String? = null

    init {
        askForPin()
    }

    override fun handleApiFailure(throwable: Throwable) {
        if (throwable is PINNotSecure) {
            errorText.set(R.string.onboarding_error_pin_not_secure)
            askForPin()
        } else {
            super.handleApiFailure(throwable)
        }
    }


    private fun askForPin() {
        step = Step.ASK_FOR_PIN
        input.set("")
        inputType.set(InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD)
        title.set(R.string.onboarding_choose_pin)
        description.set(R.string.onboarding_choose_pin_description)
    }

    private fun askForPinConfirmation() {
        step = Step.CONFIRM_PIN
        input.set("")
        errorText.set(0)
        inputType.set(InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD)
        title.set(R.string.onboarding_confirm_pin)
        description.set(R.string.onboarding_confirm_pin_description)
    }

    override fun askForOTP() {
        super.askForOTP()
        input.set("")
        errorText.set(0)
        step = Step.ASK_FOR_OTP
    }


    override fun onButtonClick() {
        val inputText = input.get()?.trim() ?: return

        when (step) {
            Step.ASK_FOR_PIN -> {
                val pinCheck = PinChecker.checkPin(inputText)
                if (pinCheck == PinChecker.Result.OK) {
                    pin = inputText
                    askForPinConfirmation()
                } else {
                    errorText.set(pinCheck.errorStringRes)
                }
            }
            Step.CONFIRM_PIN -> {
                if (pin != inputText) {
                    errorText.set(R.string.onboarding_error_pin_mismatch)
                    askForPin()
                } else {
                    sendOTP()
                }
            }
            Step.ASK_FOR_OTP -> savePin()
        }
    }

    private fun savePin() {
        val inputText = input.get()?.trim() ?: return
        val pinCode = pin ?: return

        loading.increase()
        errorText.set(0)

        IDKit.setPINWithOTP(pinCode, inputText) {
            loading.decrease()
            when (it) {
                is Success -> when (it.result) {
                    true -> done()
                    false -> errorText.set(R.string.onboarding_error_authorisation)
                }
                is Failure -> handleApiFailure(it.throwable)
            }
        }
    }

    private enum class Step {
        ASK_FOR_PIN,
        CONFIRM_PIN,
        ASK_FOR_OTP,
    }
}