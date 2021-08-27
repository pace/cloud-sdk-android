package car.pace.cofu.ui.home.dialog

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import car.pace.cofu.R
import car.pace.cofu.core.events.FragmentEvent
import car.pace.cofu.core.mvvm.BaseDialogFragment
import car.pace.cofu.databinding.FragmentDialogBinding
import dagger.hilt.android.AndroidEntryPoint

// Workaround for https://github.com/google/dagger/issues/1904
abstract class BaseLocationDialogFragment :
    BaseDialogFragment<FragmentDialogBinding, LocationPermissionDialogViewModel>(
        R.layout.fragment_dialog,
        LocationPermissionDialogViewModel::class,
        cancellable = false
    )

@AndroidEntryPoint
class LocationPermissionDialogFragment : BaseLocationDialogFragment() {

    private lateinit var permissionResultCallback: ActivityResultLauncher<String>
    private lateinit var appSettingsCallback: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        permissionResultCallback = registerForActivityResult(
            ActivityResultContracts.RequestPermission(),
            this::onPermissionDialogResult
        )
        appSettingsCallback = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult(),
            this::onAppSettingsResult
        )
        determinePermissionState()
    }

    @Suppress("UNUSED_PARAMETER")
    private fun onPermissionDialogResult(hasPermission: Boolean) {
        determinePermissionState()
    }

    @Suppress("UNUSED_PARAMETER")
    private fun onAppSettingsResult(activityResult: ActivityResult) {
        determinePermissionState()
    }

    private val doNotAskAgainSet: Boolean
        get() = !ActivityCompat.shouldShowRequestPermissionRationale(
            requireActivity(),
            Manifest.permission.ACCESS_FINE_LOCATION
        )

    private fun determinePermissionState() {
        if (requireActivity().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // permission granted now, close dialog
            dismiss()
        }
        if (doNotAskAgainSet) {
            // user checked "Do not ask again"
            viewModel.showDoNotAskAgainSet()
        }

    }

    override fun onHandleFragmentEvent(event: FragmentEvent) {
        when (event) {
            is LocationPermissionDialogViewModel.RequestPermissionEvent -> requestPermission()
            else -> super.onHandleFragmentEvent(event)
        }

    }

    private fun requestPermission() {
        if (doNotAskAgainSet) {
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", requireContext().packageName, null)
                appSettingsCallback.launch(this)
            }
        } else {
            permissionResultCallback.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }
}

