package car.pace.cofu.ui.onboarding

import car.pace.cofu.R
import car.pace.cofu.repository.FuelType

class FuelTypeSelectionViewModel(parent: OnboardingViewModel) :
    OnboardingItemViewModel(parent) {

    var isSmallDevice: Boolean = false

    override val imageRes get() = if (isSmallDevice) 0 else R.drawable.ic_fuel
    override val textRes = R.string.onboarding_step_fuel_type
    override val titleRes = R.string.onboarding_step_fuel_type_title

    init {

        FuelType.values().forEach { fuelType ->
            buttons.add(
                OnboardingButtonViewModel(
                    parent = this,
                    textRes = fuelType.stringRes,
                    onClick = {
                        parent.userDataRepository.fuelType = fuelType
                        parent.next()
                    }
                )
            )
        }

    }

    override fun onInit(skipIfRedundant: Boolean) {
        if (skipIfRedundant && parent.userDataRepository.fuelType != null) {
            parent.next()
        }
    }
}