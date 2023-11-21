package car.pace.cofu.ui.onboarding.twofactor.setup

import androidx.annotation.StringRes
import car.pace.cofu.R

sealed class TwoFactorSetup(
    @StringRes open val titleRes: Int,
    @StringRes open val descriptionRes: Int,
    @StringRes open val buttonTextRes: Int
)

data object BiometrySetup : TwoFactorSetup(
    titleRes = R.string.onboarding_enter_one_time_password_title,
    descriptionRes = R.string.onboarding_enter_one_time_password_description,
    buttonTextRes = R.string.common_use_next
)

sealed class PinSetup(
    @StringRes override val titleRes: Int,
    @StringRes override val descriptionRes: Int,
    @StringRes override val buttonTextRes: Int,
    val nextStep: PinSetup?
) : TwoFactorSetup(
    titleRes = titleRes,
    descriptionRes = descriptionRes,
    buttonTextRes = buttonTextRes
) {

    data object PinInput : PinSetup(
        titleRes = R.string.onboarding_create_pin_title,
        descriptionRes = R.string.onboarding_create_pin_description,
        buttonTextRes = R.string.common_use_next,
        nextStep = PinConfirmation
    )

    data object PinConfirmation : PinSetup(
        titleRes = R.string.onboarding_verify_pin_title,
        descriptionRes = R.string.onboarding_verify_pin_description,
        buttonTextRes = R.string.common_use_next,
        nextStep = OtpInput
    )

    data object OtpInput : PinSetup(
        titleRes = R.string.onboarding_enter_one_time_password_title,
        descriptionRes = R.string.onboarding_enter_one_time_password_description,
        buttonTextRes = R.string.common_use_next,
        nextStep = null
    )
}
