package car.pace.cofu.ui.home

import android.content.Context
import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import car.pace.cofu.data.GasStationRepository
import car.pace.cofu.data.LocationRepository
import car.pace.cofu.data.SharedPreferencesRepository
import car.pace.cofu.data.SharedPreferencesRepository.Companion.PREF_KEY_FUEL_TYPE
import car.pace.cofu.features.analytics.Analytics
import car.pace.cofu.features.analytics.FuelingStarted
import car.pace.cofu.features.analytics.StationNavigationUsed
import car.pace.cofu.features.analytics.StationNearby
import car.pace.cofu.ui.wallet.fueltype.toFuelTypeGroup
import car.pace.cofu.util.Constants.STOP_TIMEOUT_MILLIS
import car.pace.cofu.util.IntentUtils
import car.pace.cofu.util.LogAndBreadcrumb
import car.pace.cofu.util.UiState
import car.pace.cofu.util.extension.userIsNearStation
import cloud.pace.sdk.appkit.AppKit
import cloud.pace.sdk.poikit.poi.GasStation
import cloud.pace.sdk.poikit.utils.distanceTo
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combineTransform
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class HomeViewModel @Inject constructor(
    sharedPreferencesRepository: SharedPreferencesRepository,
    gasStationRepository: GasStationRepository,
    locationRepository: LocationRepository,
    val analytics: Analytics
) : ViewModel() {
    data class ListStation(val gasStation: GasStation, val canStartFueling: Boolean, val distance: Double?)

    private val refresh = MutableStateFlow(false)
    private var locationPermissionEnabled = MutableStateFlow<Boolean?>(null)
    private var locationEnabled = MutableStateFlow<Boolean?>(null)
    private var location = locationRepository.location.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = null
    )

    val uiState = combineTransform(locationEnabled, locationPermissionEnabled, location, refresh) { locationEnabled, locationPermission, location, refresh ->
        when {
            locationPermission == false -> emit(UiState.Error(LocationPermissionDenied()))
            locationEnabled == false -> emit(UiState.Error(LocationDisabled()))
            location == null -> emit(UiState.Loading)
            else -> {
                if (refresh) {
                    emit(UiState.Loading)
                }
                val gasStations = gasStationRepository.getGasStations(location).toGasStationUiState(location)
                emit(gasStations)
            }
        }
    }.onEach {
        refresh.value = false

        when {
            it is UiState.Success && it.data.isEmpty() -> LogAndBreadcrumb.i(LogAndBreadcrumb.HOME, "No gas stations near user")
            it is UiState.Error -> LogAndBreadcrumb.e(it.throwable, LogAndBreadcrumb.HOME, it.throwable.message ?: "Could not load gas station list")
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = UiState.Loading
    )

    private val initialValue = sharedPreferencesRepository.getInt(PREF_KEY_FUEL_TYPE, -1)
    val fuelTypeGroup = sharedPreferencesRepository
        .getValue(PREF_KEY_FUEL_TYPE, initialValue)
        .map { it.toFuelTypeGroup() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = initialValue.toFuelTypeGroup()
        )

    val userLocation = locationRepository.location
        .map { LatLng(it.latitude, it.longitude) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = null
        )

    fun onLocationPermissionChanged(enabled: Boolean) {
        locationPermissionEnabled.value = enabled
    }

    fun onLocationEnabledChanged(enabled: Boolean) {
        locationEnabled.value = enabled
    }

    fun refresh() {
        refresh.value = true
    }

    fun startFueling(context: Context, gasStation: GasStation) {
        LogAndBreadcrumb.i(LogAndBreadcrumb.HOME, "Start fueling")
        analytics.logEvent(FuelingStarted)
        AppKit.openFuelingApp(context = context, id = gasStation.id, callback = analytics.TrackingAppCallback())
    }

    fun startNavigation(context: Context, gasStation: GasStation) {
        LogAndBreadcrumb.i(LogAndBreadcrumb.HOME, "Start navigation to gas station")
        IntentUtils.startNavigation(context, gasStation).onSuccess {
            analytics.logEvent(StationNavigationUsed)
        }
    }

    private fun Result<List<GasStation>>.toGasStationUiState(location: Location) = fold(
        onSuccess = {
            val gasStations = it.map { gasStation ->
                val latLng = LatLng(location.latitude, location.longitude)
                val canStartFueling = gasStation.userIsNearStation(latLng)
                val distance = getDistance(gasStation, latLng)
                ListStation(gasStation, canStartFueling, distance)
            }

            if (gasStations.any { it.canStartFueling }) {
                analytics.logEvent(StationNearby)
            }

            UiState.Success(gasStations)
        },
        onFailure = {
            UiState.Error(it)
        }
    )

    private fun getDistance(gasStation: GasStation, location: LatLng): Double? {
        val center = gasStation.center?.toLatLn() ?: return null
        return location.distanceTo(center)
    }

    class LocationPermissionDenied : Throwable("Location permission denied")
    class LocationDisabled : Throwable("Location disabled")
}
