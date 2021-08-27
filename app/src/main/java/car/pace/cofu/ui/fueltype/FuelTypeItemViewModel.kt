package car.pace.cofu.ui.fueltype

import car.pace.cofu.R
import car.pace.cofu.core.mvvm.BaseItemViewModel
import car.pace.cofu.repository.FuelType

class FuelTypeItemViewModel(
    private val onClick: (FuelType) -> Unit,
    override val item: FuelType,
    val isSelected: Boolean = true
) : BaseItemViewModel() {

    override val layoutId = R.layout.item_fuel_type_button

    fun onClick() {
        onClick.invoke(item)
    }
}