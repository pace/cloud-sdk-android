package car.pace.cofu.ui.onboarding

import car.pace.cofu.R
import car.pace.cofu.core.events.FragmentEvent

class PaceAuthorisationItemViewModel(parent: OnboardingViewModel) :
    OnboardingItemViewModel(parent) {
    override val imageRes = R.drawable.ic_scan
    override val textRes = R.string.onboarding_step_authorisation
    override val titleRes = R.string.onboarding_step_authorisation_title

    private var skipIfRedundant = false

    var hasFingerprint: Boolean? = null
        set(value) {
            field = value
            reloadButtons()
        }

    var existingMethodsResult: AuthorisationMethodsCheckResultEvent? = null
        set(value) {
            field = value
            reloadButtons()
        }

    private fun reloadButtons() {
        if (hasFingerprint == null || existingMethodsResult == null) {
            // we haven't received fingerprint or already set methods yet, wait until both are available
            return
        }

        buttons.clear()

        if (hasFingerprint == true) {
            buttons.add(
                OnboardingButtonViewModel(
                    parent = this,
                    textRes = R.string.onboarding_authorisation_biometry,
                    onClick = {
                        parent.handleEvent(AuthorisationEvent(AuthorisationMethod.FINGERPRINT))
                    }
                )
            )
        }

        val pinAlreadySet = existingMethodsResult!!.pinSet

        buttons.add(
            OnboardingButtonViewModel(
                parent = this,
                textRes = if (pinAlreadySet) R.string.onboarding_authorisation_existing_pin else R.string.onboarding_authorisation_new_pin,
                onClick = {
                    parent.handleEvent(
                        AuthorisationEvent(
                            method = if (pinAlreadySet) AuthorisationMethod.EXISTING_PIN else AuthorisationMethod.NEW_PIN
                        )
                    )
                },
                isPrimary = hasFingerprint == false
            )
        )

        if (hasFingerprint == false) {
            buttons.add(OnboardingPlaceholderButtonViewModel(this))
        }

    }

    override fun onResponse(response: FragmentEvent) {
        when (response) {
            is AuthorisationSetEvent -> parent.next()
            is AuthorisationMethodsCheckResultEvent -> {
                if (skipIfRedundant && response.biometryEnabled && response.pinSet) {
                    // both methods already enabled, no need to setup anything extra
                    parent.next()
                }
                existingMethodsResult = response
            }
        }
    }

    override fun onInit(skipIfRedundant: Boolean) {
        // check which authorisation methods (biometry and/or pin) are already enabled
        // when both are enabled the step can be skipped
        this.skipIfRedundant = skipIfRedundant
        parent.handleEvent(CheckAuthorisationMethodsEvent())
    }
}

enum class AuthorisationMethod {
    EXISTING_PIN,
    NEW_PIN,
    FINGERPRINT,
}