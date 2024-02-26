package car.pace.cofu.data

import car.pace.cofu.di.coroutine.ApplicationScope
import car.pace.cofu.util.Constants.STOP_TIMEOUT_MILLIS
import car.pace.cofu.util.extension.LocationDisabledException
import car.pace.cofu.util.extension.LocationPermissionDeniedException
import cloud.pace.sdk.utils.LocationProvider
import cloud.pace.sdk.utils.Success
import com.google.android.gms.location.LocationRequest
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.shareIn

@OptIn(ExperimentalCoroutinesApi::class)
@Singleton
class LocationRepository @Inject constructor(
    @ApplicationScope private val externalScope: CoroutineScope,
    locationRequest: LocationRequest,
    locationProvider: LocationProvider
) {

    private val locationEnabled = MutableStateFlow<Boolean?>(null)
    private val locationPermissionGranted = MutableStateFlow<Boolean?>(null)
    private val currentLocation = locationProvider.locationFlow(locationRequest).onStart {
        // Return the last known location when starting the location listener to reduce the loading time
        val lastKnownLocation = (locationProvider.lastKnownLocation(false) as? Success)?.result
        if (lastKnownLocation != null) {
            emit(lastKnownLocation)
        }
    }

    val location = locationEnabled.combine(locationPermissionGranted) { locationEnabled, permissionGranted ->
        when {
            locationEnabled == false -> LocationDisabledException()
            permissionGranted == false -> LocationPermissionDeniedException()
            else -> null
        }
    }.flatMapLatest { locationError ->
        if (locationError == null) {
            currentLocation.map { Result.success(it) }
        } else {
            flowOf(Result.failure(locationError))
        }
    }.shareIn(
        scope = externalScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        replay = 1
    )

    fun onLocationEnabledChanged(enabled: Boolean) {
        locationEnabled.value = enabled
    }

    fun onLocationPermissionChanged(granted: Boolean) {
        locationPermissionGranted.value = granted
    }
}
