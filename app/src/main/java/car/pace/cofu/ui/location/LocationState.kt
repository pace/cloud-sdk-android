package car.pace.cofu.ui.location

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import car.pace.cofu.data.PermissionRepository.Companion.locationPermissions
import car.pace.cofu.ui.onboarding.twofactor.biometric.findActivity
import car.pace.cofu.util.Constants.LOCATION_UPDATE_DISTANCE_METERS
import car.pace.cofu.util.Constants.LOCATION_UPDATE_INTERVAL_MILLIS
import car.pace.cofu.util.LogAndBreadcrumb
import car.pace.cofu.util.extension.isLocationEnabled
import car.pace.cofu.util.extension.listenForLocationEnabledChanges
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun rememberLocationState(
    permissionState: MultiplePermissionsState = rememberMultiplePermissionsState(permissions = locationPermissions),
    onLocationEnabledChanged: (Boolean) -> Unit = {},
    onLocationPermissionChanged: (Boolean) -> Unit = {}
): LocationState {
    val context = LocalContext.current
    val locationServicesState = remember { LocationServicesState(context) }

    LifecycleEventEffect(event = Lifecycle.Event.ON_RESUME) {
        locationServicesState.refreshState()
    }

    DisposableEffect(Unit) {
        val locationEnabledListener = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                locationServicesState.refreshState()
            }
        }
        context.listenForLocationEnabledChanges(locationEnabledListener)

        onDispose {
            context.unregisterReceiver(locationEnabledListener)
        }
    }

    val locationState = remember(permissionState) {
        LocationState(permissionState, locationServicesState)
    }

    val currentOnLocationEnabledChanged by rememberUpdatedState(onLocationEnabledChanged)
    LaunchedEffect(locationState) {
        snapshotFlow { locationState.locationEnabled }.collect {
            currentOnLocationEnabledChanged(it)
        }
    }

    val currentOnLocationPermissionChanged by rememberUpdatedState(onLocationPermissionChanged)
    LaunchedEffect(locationState) {
        snapshotFlow { locationState.locationPermissionGranted }.collect {
            currentOnLocationPermissionChanged(it)
        }
    }

    return locationState
}

@OptIn(ExperimentalPermissionsApi::class)
@Stable
class LocationState(
    private val permissionState: MultiplePermissionsState,
    private val locationServicesState: LocationServicesState
) {

    val locationPermissionGranted get() = permissionState.permissions.any { it.status.isGranted }

    val locationEnabled get() = locationServicesState.locationEnabled

    fun launchMultiplePermissionRequest() = permissionState.launchMultiplePermissionRequest()

    suspend fun launchLocationServicesRequest() = locationServicesState.launchLocationServicesRequest()
}

@Stable
class LocationServicesState(
    private val context: Context
) {

    var locationEnabled by mutableStateOf(getState())

    private val settingsClient by lazy {
        LocationServices.getSettingsClient(context)
    }

    private val locationSettingsRequest by lazy {
        val locationRequest = LocationRequest.Builder(LOCATION_UPDATE_INTERVAL_MILLIS)
            .setMinUpdateDistanceMeters(LOCATION_UPDATE_DISTANCE_METERS)
            .build()

        LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
            .build()
    }

    fun refreshState() {
        locationEnabled = getState()
    }

    suspend fun launchLocationServicesRequest() = try {
        settingsClient.checkLocationSettings(locationSettingsRequest).await()
        true
    } catch (e: ResolvableApiException) {
        try {
            LogAndBreadcrumb.i("Location services", "Location settings are not satisfied. But could be fixed by showing the user a dialog.")
            e.startResolutionForResult(context.findActivity(), 999)
            true
        } catch (e: Exception) {
            LogAndBreadcrumb.e(e, "Location services", "Could not show the location services dialog")
            false
        }
    } catch (e: Exception) {
        LogAndBreadcrumb.e(e, "Location services", "Could not check location services settings")
        false
    }

    private fun getState() = context.isLocationEnabled
}
