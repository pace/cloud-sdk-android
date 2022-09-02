package car.pace.cofu.ui.onboarding

import car.pace.cofu.R

class FuelTypeSelectionViewModel(parent: OnboardingViewModel) :
    OnboardingItemViewModel(parent) {

    var isSmallDevice: Boolean = false

    override val imageRes get() = if (isSmallDevice) 0 else R.drawable.ic_fuel
    override val textRes = R.string.ONBOARDING_FUEL_TYPE_DESCRIPTION
    override val titleRes = R.string.ONBOARDING_FUEL_TYPE_TITLE
    override val isFuelTypeSelection: Boolean = true

    init {
        buttons.add(
            OnboardingButtonViewModel(
                parent = this,
                textRes = R.string.ONBOARDING_ACTIONS_NEXT,
                onClick = {
                    if (parent.fuelType != null) {
                        parent.userDataRepository.fuelType = parent.fuelType
                        parent.next()
                    } else {
                        parent.unselectedFuelType.postValue(Unit)
                    }
                }
            )
        )
    }

    override fun onInit(skipIfRedundant: Boolean) {
        if (skipIfRedundant && parent.userDataRepository.fuelType != null) {
            parent.next()
        }
    }
}