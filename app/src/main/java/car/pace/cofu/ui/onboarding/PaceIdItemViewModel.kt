package car.pace.cofu.ui.onboarding

import car.pace.cofu.R
import car.pace.cofu.core.events.FragmentEvent
import car.pace.cofu.core.events.ShowSnack
import cloud.pace.sdk.idkit.IDKit

class PaceIdItemViewModel(parent: OnboardingViewModel) :
    OnboardingItemViewModel(parent) {
    override val imageRes = R.drawable.ic_profile
    override val textRes = R.string.ONBOARDING_AUTHENTICATION_DESCRIPTION
    override val titleRes = R.string.ONBOARDING_AUTHENTICATION_TITLE
    override val isFuelTypeSelection = false

    init {
        buttons.add(
            OnboardingButtonViewModel(
                parent = this,
                textRes = R.string.ONBOARDING_ACTIONS_AUTHENTICATE,
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
                parent.handleEvent(ShowSnack(messageRes = R.string.ONBOARDING_LOG_IN_UNSUCCESSFUL))
            }
        }
    }

    override fun onInit(skipIfRedundant: Boolean) {
        if (skipIfRedundant && IDKit.isAuthorizationValid()) parent.next()
    }
}