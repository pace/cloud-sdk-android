package car.pace.cofu.ui.onboarding.twofactor.setup

import androidx.annotation.StringRes
import car.pace.cofu.R

enum class TwoFactorSetupPage(
    val cellsCount: Int,
    @StringRes val titleRes: Int,
    @StringRes val descriptionRes: Int,
    @StringRes val buttonRes: Int
) {
    PIN_INPUT(
        cellsCount = 4,
        titleRes = R.string.onboarding_create_pin_title,
        descriptionRes = R.string.onboarding_create_pin_description,
        buttonRes = R.string.common_use_next
    ),
    PIN_CONFIRMATION(
        cellsCount = 4,
        titleRes = R.string.onboarding_verify_pin_title,
        descriptionRes = R.string.onboarding_verify_pin_description,
        buttonRes = R.string.common_use_next
    ),
    OTP_INPUT(
        cellsCount = 6,
        titleRes = R.string.onboarding_enter_one_time_password_title,
        descriptionRes = R.string.onboarding_enter_one_time_password_description,
        buttonRes = R.string.common_use_next
    )
}

enum class TwoFactorSetupType(val pages: List<TwoFactorSetupPage>) {
    BIOMETRY(listOf(TwoFactorSetupPage.OTP_INPUT)),
    PIN(listOf(TwoFactorSetupPage.PIN_INPUT, TwoFactorSetupPage.PIN_CONFIRMATION, TwoFactorSetupPage.OTP_INPUT))
}
