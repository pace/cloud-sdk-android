package car.pace.cofu.ui.home.dialog

import androidx.databinding.ObservableInt
import car.pace.cofu.R
import car.pace.cofu.core.events.FragmentEvent

class LocationDisabledDialogViewModel : BaseDialogViewModel() {
    override val title = ObservableInt(R.string.LOCATION_DIALOG_DISABLED_TITLE)
    override val description = ObservableInt(R.string.LOCATION_DIALOG_DISABLED_TEXT)
    override val buttonText = ObservableInt(R.string.ALERT_LOCATION_PERMISSION_ACTIONS_OPEN_SETTINGS)

    override fun onButtonClick() {
        handleEvent(OpenLocationSettingsEvent())
    }

    class OpenLocationSettingsEvent : FragmentEvent()

}