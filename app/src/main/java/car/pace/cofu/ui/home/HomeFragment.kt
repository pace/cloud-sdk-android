package car.pace.cofu.ui.home

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import car.pace.cofu.R
import car.pace.cofu.core.events.FragmentEvent
import car.pace.cofu.core.events.NavigateToDirection
import car.pace.cofu.core.mvvm.BaseFragment
import car.pace.cofu.core.navigation.navigate
import car.pace.cofu.core.util.isLocationEnabled
import car.pace.cofu.core.util.listenForLocationEnabledChanges
import car.pace.cofu.databinding.FragmentHomeBinding
import cloud.pace.sdk.appkit.AppKit
import cloud.pace.sdk.poikit.POIKit
import cloud.pace.sdk.poikit.poi.GasStation
import dagger.hilt.android.AndroidEntryPoint

// Workaround for https://github.com/google/dagger/issues/1904
abstract class BaseHomeFragment :
    BaseFragment<FragmentHomeBinding, HomeViewModel>(R.layout.fragment_home, HomeViewModel::class)

@AndroidEntryPoint
class HomeFragment : BaseHomeFragment() {

    override fun onResume() {
        super.onResume()
        if (requireActivity().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            showNoPermissionDialog()
        } else {
            if (requireContext().isLocationEnabled) {
                startLocationListener()
            } else {
                showGpsDisabledDialog()
            }
        }

        findNavController().addOnDestinationChangedListener(destinationChangedListener)
        activity?.listenForLocationEnabledChanges(locationEnabledListener)
    }

    // when coming back from the fuel selection fragment, update prices
    private val destinationChangedListener =
        NavController.OnDestinationChangedListener { _, destination, _ ->
            if (destination.id == R.id.fragment_home) {
                viewModel.updateFuelType()
            }
        }

    private val locationEnabledListener = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (!requireContext().isLocationEnabled) showGpsDisabledDialog()
        }
    }

    override fun onPause() {
        activity?.unregisterReceiver(locationEnabledListener)
        stopLocationListener()
        findNavController().removeOnDestinationChangedListener(destinationChangedListener)
        super.onPause()
    }

    private fun showNoPermissionDialog() {
        navigate(NavigateToDirection(HomeFragmentDirections.locationPermissionDialog()))
    }

    private fun showGpsDisabledDialog() {
        navigate(NavigateToDirection(HomeFragmentDirections.locationDisabledDialog()))
    }

    override fun onHandleFragmentEvent(event: FragmentEvent) {
        when (event) {
            is HomeViewModel.StartNavigationEvent -> startNavigation(event.gasStation)
            is HomeViewModel.FuelUpEvent -> fuelUp(event.gasStation)
            else -> super.onHandleFragmentEvent(event)
        }
    }

    private fun startNavigation(gasStation: GasStation) {
        // is google maps installed? If so, launch navigation directly
        val uri = Uri.parse("google.navigation:q=${gasStation.latitude},${gasStation.longitude}")
        val mapIntent = Intent(Intent.ACTION_VIEW, uri)
        mapIntent.setPackage("com.google.android.apps.maps")

        val list = requireActivity().packageManager.queryIntentActivities(
            mapIntent,
            PackageManager.MATCH_DEFAULT_ONLY
        )
        if (list.size > 0) {
            startActivity(mapIntent)
            return
        }

        // no google maps installed, fallback to regular geo uri
        val intent = Intent(Intent.ACTION_VIEW)
        val address = gasStation.address?.let {
            "${it.street} ${it.houseNumber}, ${it.postalCode} ${it.city}"
        }
        intent.data = Uri.parse("geo:${gasStation.latitude},${gasStation.longitude}?q=$address")
        startActivity(intent)
    }

    private fun fuelUp(gasStation: GasStation) {
        AppKit.openFuelingApp(requireContext(), gasStation.id)
    }

    private fun startLocationListener() {
        POIKit.startLocationListener().location.observe(this) {
            viewModel.currentLocation = it
        }
    }

    private fun stopLocationListener() {
        POIKit.stopLocationListener()
    }
}