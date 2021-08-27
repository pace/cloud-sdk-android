package car.pace.cofu.ui.home.dialog

import androidx.databinding.ObservableInt
import car.pace.cofu.R
import car.pace.cofu.core.events.FragmentEvent

class LocationPermissionDialogViewModel : BaseDialogViewModel() {
    override val title = ObservableInt(R.string.location_dialog_permission_title)
    override val description = ObservableInt(R.string.location_dialog_permission_text)
    override val buttonText = ObservableInt(R.string.location_dialog_permission_button)

    override fun onButtonClick() {
        handleEvent(RequestPermissionEvent())
    }

    fun showDoNotAskAgainSet() {
        title.set(R.string.location_dialog_permission_denied_title)
        description.set(R.string.location_dialog_permission_denied_text)
        buttonText.set(R.string.location_dialog_permission_denied_button)
    }

    class RequestPermissionEvent : FragmentEvent()

}