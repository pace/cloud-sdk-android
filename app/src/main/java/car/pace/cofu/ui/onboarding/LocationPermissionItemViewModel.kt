package car.pace.cofu.ui.onboarding

import car.pace.cofu.R
import car.pace.cofu.core.events.FragmentEvent

class LocationPermissionItemViewModel(parent: OnboardingViewModel) :
    OnboardingItemViewModel(parent) {
    override val imageRes = R.drawable.ic_location
    override val textRes = R.string.ONBOARDING_PERMISSION_DESCRIPTION
    override val titleRes = R.string.ONBOARDING_PERMISSION_TITLE
    override val isFuelTypeSelection = false

    init {

        buttons.add(
            OnboardingButtonViewModel(
                parent = this,
                textRes = R.string.ONBOARDING_ACTIONS_SHARE_LOCATION,
                onClick = {
                    parent.handleEvent(RequestLocationPermissionEvent())
                }
            )
        )

        buttons.add(OnboardingPlaceholderButtonViewModel(this))
    }

    override fun onInit(skipIfRedundant: Boolean) {
        if(skipIfRedundant) parent.handleEvent(CheckLocationPermissionEvent())
    }

    override fun onResponse(response: FragmentEvent) {
        // we'll continue to the next step whether the permission was granted or not
        // if it is denied, the user will be prompted again later
        if (response is LocationPermissionRequestResult || (response as? LocationPermissionCheckResult)?.isGranted == true) {
            parent.next()
        }
    }
}