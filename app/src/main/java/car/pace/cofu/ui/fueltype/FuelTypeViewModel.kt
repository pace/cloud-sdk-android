package car.pace.cofu.ui.fueltype

import car.pace.cofu.core.events.FragmentEvent
import car.pace.cofu.core.mvvm.BaseItemViewModel
import car.pace.cofu.core.mvvm.BaseViewModel
import car.pace.cofu.repository.FuelType
import car.pace.cofu.repository.UserDataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class FuelTypeViewModel @Inject constructor(internal val userDataRepository: UserDataRepository) :
    BaseViewModel() {

    val buttons = ArrayList<BaseItemViewModel>()

    init {
        val selectedFuelType = userDataRepository.fuelType

        FuelType.values().forEach { fuelType ->
            buttons.add(
                FuelTypeItemViewModel(
                    onClick = ::onSelectFuelType,
                    item = fuelType,
                    isSelected = fuelType == selectedFuelType
                )
            )
        }

    }

    private fun onSelectFuelType(fuelType: FuelType) {
        userDataRepository.fuelType = fuelType
        handleEvent(DismissDialogEvent())
    }

    class DismissDialogEvent : FragmentEvent()
}