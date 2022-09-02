package car.pace.cofu.ui.home.dialog

import androidx.databinding.ObservableInt
import car.pace.cofu.R
import car.pace.cofu.core.events.FragmentEvent

class LocationPermissionDialogViewModel : BaseDialogViewModel() {
    override val title = ObservableInt(R.string.ALERT_LOCATION_PERMISSION_TITLE)
    override val description = ObservableInt(R.string.ALERT_LOCATION_PERMISSION_DESCRIPTION)
    override val buttonText = ObservableInt(R.string.ONBOARDING_ACTIONS_SHARE_LOCATION)

    override fun onButtonClick() {
        handleEvent(RequestPermissionEvent())
    }

    fun showDoNotAskAgainSet() {
        title.set(R.string.LOCATION_DIALOG_PERMISSION_DENIED_TITLE)
        description.set(R.string.LOCATION_DIALOG_PERMISSION_DENIED_TEXT)
        buttonText.set(R.string.ALERT_LOCATION_PERMISSION_ACTIONS_OPEN_SETTINGS)
    }

    class RequestPermissionEvent : FragmentEvent()

}