package car.pace.cofu.ui.onboarding.dialog

import car.pace.cofu.R
import car.pace.cofu.core.util.decrease
import car.pace.cofu.core.util.increase
import cloud.pace.sdk.idkit.IDKit
import cloud.pace.sdk.utils.Failure
import cloud.pace.sdk.utils.Success

class ConfigureBiometricAuthViewModel : BaseOnboardingBottomSheetViewModel() {

    init {
        sendOTP()
    }

    override fun onButtonClick() {
        val inputText = input.get() ?: return
        loading.increase()
        errorText.set(0)

        IDKit.enableBiometricAuthenticationWithOTP(inputText) {
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

}