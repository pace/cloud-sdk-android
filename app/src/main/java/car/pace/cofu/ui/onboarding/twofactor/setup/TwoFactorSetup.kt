package car.pace.cofu.ui.onboarding.twofactor.setup

import androidx.annotation.StringRes
import car.pace.cofu.R

sealed class TwoFactorSetup(
    @StringRes open val titleRes: Int,
    @StringRes open val descriptionRes: Int,
    @StringRes open val buttonTextRes: Int
)

data object BiometrySetup : TwoFactorSetup(
    titleRes = R.string.ONBOARDING_ENTER_ONE_TIME_PASSWORD_TITLE,
    descriptionRes = R.string.ONBOARDING_ENTER_ONE_TIME_PASSWORD_DESCRIPTION,
    buttonTextRes = R.string.ONBOARDING_ACTIONS_NEXT
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
        titleRes = R.string.ONBOARDING_CREATE_PIN_TITLE,
        descriptionRes = R.string.ONBOARDING_CREATE_PIN_DESCRIPTION,
        buttonTextRes = R.string.ONBOARDING_ACTIONS_NEXT,
        nextStep = PinConfirmation
    )

    data object PinConfirmation : PinSetup(
        titleRes = R.string.ONBOARDING_VERIFY_PIN_TITLE,
        descriptionRes = R.string.ONBOARDING_VERIFY_PIN_DESCRIPTION,
        buttonTextRes = R.string.ONBOARDING_ACTIONS_NEXT,
        nextStep = OtpInput
    )

    data object OtpInput : PinSetup(
        titleRes = R.string.ONBOARDING_ENTER_ONE_TIME_PASSWORD_TITLE,
        descriptionRes = R.string.ONBOARDING_ENTER_ONE_TIME_PASSWORD_DESCRIPTION,
        buttonTextRes = R.string.ONBOARDING_ACTIONS_NEXT,
        nextStep = null
    )
}
