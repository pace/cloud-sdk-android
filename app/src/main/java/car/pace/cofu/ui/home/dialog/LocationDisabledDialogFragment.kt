package car.pace.cofu.ui.home.dialog

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Settings
import car.pace.cofu.R
import car.pace.cofu.core.events.FragmentEvent
import car.pace.cofu.core.mvvm.BaseDialogFragment
import car.pace.cofu.core.util.isLocationEnabled
import car.pace.cofu.core.util.listenForLocationEnabledChanges
import car.pace.cofu.databinding.FragmentDialogBinding
import dagger.hilt.android.AndroidEntryPoint

// Workaround for https://github.com/google/dagger/issues/1904
abstract class BaseLocationDisabledDialogFragment :
    BaseDialogFragment<FragmentDialogBinding, LocationDisabledDialogViewModel>(
        R.layout.fragment_dialog,
        LocationDisabledDialogViewModel::class,
        cancellable = false
    )

@AndroidEntryPoint
class LocationDisabledDialogFragment : BaseLocationDisabledDialogFragment() {

    override fun onHandleFragmentEvent(event: FragmentEvent) {
        when (event) {
            is LocationDisabledDialogViewModel.OpenLocationSettingsEvent -> openLocationSettings()
            else -> super.onHandleFragmentEvent(event)
        }

    }

    private val locationEnabledListener = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (requireContext().isLocationEnabled) dismiss()
        }
    }

    override fun onResume() {
        super.onResume()
        if (requireContext().isLocationEnabled) dismiss()
        activity?.listenForLocationEnabledChanges(locationEnabledListener)
    }

    override fun onPause() {
        activity?.unregisterReceiver(locationEnabledListener)
        super.onPause()
    }

    private fun openLocationSettings() {
        startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
    }
}

