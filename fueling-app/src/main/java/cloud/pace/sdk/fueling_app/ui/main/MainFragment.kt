package cloud.pace.sdk.fueling_app.ui.main

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import cloud.pace.sdk.fueling_app.R
import cloud.pace.sdk.fueling_app.databinding.FragmentMainBinding
import cloud.pace.sdk.fueling_app.util.Result
import cloud.pace.sdk.fueling_app.util.asSafeArgsGasStation
import cloud.pace.sdk.idkit.IDKit
import cloud.pace.sdk.poikit.poi.GasStation
import cloud.pace.sdk.utils.Failure
import cloud.pace.sdk.utils.Success
import cloud.pace.sdk.utils.viewBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * This is the entry point of the app and searches for [Connected Fueling gas stations][cloud.pace.sdk.poikit.POIKit.requestCofuGasStations] in the vicinity with each location update.
 */
@AndroidEntryPoint
class MainFragment : Fragment(R.layout.fragment_main) {

    private val binding by viewBinding(FragmentMainBinding::bind)
    private val viewModel by viewModels<MainViewModel>()

    // Since target SDK 31 (Android 12) ACCESS_FINE_LOCATION must be requested with ACCESS_COARSE_LOCATION
    private val permissions = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
    private val requestPermissions = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        if (it[Manifest.permission.ACCESS_FINE_LOCATION] == true) {
            // Location permissions granted
            viewModel.requestLocationUpdates()
        } else {
            MaterialAlertDialogBuilder(requireContext())
                .setIcon(R.drawable.ic_location_off)
                .setTitle(R.string.location_permission_dialog_title)
                .setMessage(R.string.location_permission_dialog_message)
                .setNeutralButton(android.R.string.ok, null)
                .setPositiveButton(R.string.settings) { dialog, _ ->
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts("package", requireContext().packageName, null)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                    intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
                    intent.data = uri
                    startActivity(intent)

                    dialog.dismiss()
                }
                .show()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val gasStationsAdapter = GasStationsAdapter { gasStation ->
            if (IDKit.isAuthorizationValid()) {
                IDKit.refreshToken {
                    when (it) {
                        is Success -> selectGasStation(gasStation)
                        is Failure -> authorize(gasStation)
                    }
                }
            } else {
                authorize(gasStation)
            }
        }

        binding.gasStations.apply {
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
            layoutManager = LinearLayoutManager(context)
            adapter = gasStationsAdapter
        }

        viewModel.lastLocation.observe(viewLifecycleOwner) {
            gasStationsAdapter.userLocation = it
        }

        viewModel.cofuGasStations.observe(viewLifecycleOwner) {
            binding.loadingView.root.isGone = true

            if (it is Result.Success) {
                if (it.data.isEmpty()) {
                    binding.gasStationsGroup.isGone = true
                    binding.errorView.text.setText(R.string.no_gas_stations)
                    binding.errorView.image.setImageResource(R.drawable.ic_no_cofu_stations)
                    binding.errorView.root.isVisible = true
                } else {
                    binding.errorView.root.isGone = true
                    binding.gasStationsGroup.isVisible = true
                    gasStationsAdapter.entries = it.data
                }
            } else if (it is Result.Error) {
                showError()
            }
        }

        if (permissions.any { checkSelfPermission(requireContext(), it) != PackageManager.PERMISSION_GRANTED }) {
            requestPermissions.launch(permissions)
        }
    }

    private fun authorize(gasStation: GasStation) {
        lifecycleScope.launch {
            IDKit.authorize(this@MainFragment) {
                when (it) {
                    is Success -> {
                        MaterialAlertDialogBuilder(requireContext())
                            .setIcon(R.drawable.ic_fingerprint)
                            .setTitle(R.string.biometric_dialog_title)
                            .setMessage(R.string.biometric_dialog_message)
                            .setPositiveButton(R.string.common_yes) { dialog, _ ->
                                IDKit.enableBiometricAuthentication { completion ->
                                    if ((completion as? Success)?.result != true) {
                                        Toast.makeText(requireContext(), R.string.biometric_setup_error, Toast.LENGTH_SHORT).show()
                                    }
                                    dialog.dismiss()
                                }
                            }
                            .setNegativeButton(R.string.common_no, null)
                            .setOnDismissListener {
                                selectGasStation(gasStation)
                            }
                            .show()
                    }
                    is Failure -> showError()
                }
            }
        }
    }

    private fun selectGasStation(gasStation: GasStation) {
        findNavController().navigate(MainFragmentDirections.actionMainFragmentToPaymentMethodsFragment(gasStation.asSafeArgsGasStation()))
    }

    private fun showError() {
        binding.gasStationsGroup.isGone = true
        binding.errorView.text.setText(R.string.generic_error)
        binding.errorView.image.setImageResource(R.drawable.ic_pump_error)
        binding.errorView.root.isVisible = true
    }
}
