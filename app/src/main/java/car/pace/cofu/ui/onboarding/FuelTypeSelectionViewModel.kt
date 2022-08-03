package car.pace.cofu.ui.onboarding

import car.pace.cofu.R

class FuelTypeSelectionViewModel(parent: OnboardingViewModel) :
    OnboardingItemViewModel(parent) {

    var isSmallDevice: Boolean = false

    override val imageRes get() = if (isSmallDevice) 0 else R.drawable.ic_fuel
    override val textRes = R.string.onboarding_step_fuel_type
    override val titleRes = R.string.onboarding_step_fuel_type_title
    override val isFuelTypeSelection: Boolean = true

    init {
        buttons.add(
            OnboardingButtonViewModel(
                parent = this,
                textRes = R.string.onboarding_continue,
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