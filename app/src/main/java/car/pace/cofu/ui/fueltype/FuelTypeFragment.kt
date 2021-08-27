package car.pace.cofu.ui.fueltype

import car.pace.cofu.R
import car.pace.cofu.core.events.FragmentEvent
import car.pace.cofu.core.mvvm.BaseDialogFragment
import car.pace.cofu.databinding.FragmentFuelTypeBottomSheetBinding
import dagger.hilt.android.AndroidEntryPoint

// Workaround for https://github.com/google/dagger/issues/1904
abstract class BaseFuelTypeFragment :
    BaseDialogFragment<FragmentFuelTypeBottomSheetBinding, FuelTypeViewModel>(
        R.layout.fragment_fuel_type_bottom_sheet,
        FuelTypeViewModel::class,
        dialogMode = DialogMode.BOTTOM_SHEET
    )

@AndroidEntryPoint
class FuelTypeFragment : BaseFuelTypeFragment() {
    override fun onHandleFragmentEvent(event: FragmentEvent) {
        when (event) {
            is FuelTypeViewModel.DismissDialogEvent -> dismiss()
            else -> super.onHandleFragmentEvent(event)
        }
    }
}


