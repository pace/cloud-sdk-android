package car.pace.cofu.ui.home.dialog

import androidx.databinding.ObservableInt
import car.pace.cofu.R
import car.pace.cofu.core.events.FragmentEvent

class LocationDisabledDialogViewModel : BaseDialogViewModel() {
    override val title = ObservableInt(R.string.location_dialog_disabled_title)
    override val description = ObservableInt(R.string.location_dialog_disabled_text)
    override val buttonText = ObservableInt(R.string.location_dialog_disabled_button)

    override fun onButtonClick() {
        handleEvent(OpenLocationSettingsEvent())
    }

    class OpenLocationSettingsEvent : FragmentEvent()

}