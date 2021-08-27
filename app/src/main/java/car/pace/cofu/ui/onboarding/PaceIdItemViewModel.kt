package car.pace.cofu.ui.onboarding

import car.pace.cofu.R
import car.pace.cofu.core.events.FragmentEvent
import car.pace.cofu.core.events.ShowSnack
import cloud.pace.sdk.idkit.IDKit

class PaceIdItemViewModel(parent: OnboardingViewModel) :
    OnboardingItemViewModel(parent) {
    override val imageRes = R.drawable.ic_profile
    override val textRes = R.string.onboarding_step_pace_id
    override val titleRes = R.string.onboarding_step_pace_id_title

    init {
        buttons.add(
            OnboardingButtonViewModel(
                parent = this,
                textRes = R.string.onboarding_log_in,
                onClick = {
                    parent.handleEvent(StartPaceIdRegistration())
                }
            )
        )

        buttons.add(OnboardingPlaceholderButtonViewModel(this))
    }

    override fun onResponse(response: FragmentEvent) {
        when (response) {
            is PaceIdRegistrationSuccessful -> parent.next()
            is PaceIdRegistrationFailed -> {
                parent.handleEvent(ShowSnack(messageRes = R.string.onboarding_log_in_unsuccessful))
            }
        }
    }

    override fun onInit(skipIfRedundant: Boolean) {
        if (skipIfRedundant && IDKit.isAuthorizationValid()) parent.next()
    }
}