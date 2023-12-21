package car.pace.cofu.ui.list

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
class ListViewModel @Inject constructor(
    sharedPreferencesRepository: SharedPreferencesRepository,
    gasStationRepository: GasStationRepository,
    private val locationRepository: LocationRepository,
    private val analytics: Analytics
) : ViewModel() {

    data class ListStation(
        val gasStation: GasStation,
        val canStartFueling: Boolean,
        val distance: Double?
    )

    private val refresh = MutableStateFlow(false)

    val uiState = combineTransform(locationRepository.location, refresh) { locationResult, refresh ->
        val location = locationResult.getOrElse {
            emit(UiState.Error(it))
            return@combineTransform
        }

        if (refresh) {
            emit(UiState.Loading)
        }

        val gasStations = gasStationRepository.getGasStations(location).toGasStationUiState(location)
        emit(gasStations)
    }.onEach {
        refresh.value = false

        when {
            it is UiState.Success && it.data.isEmpty() -> LogAndBreadcrumb.i(LogAndBreadcrumb.LIST, "No gas stations near user")
            it is UiState.Error -> LogAndBreadcrumb.e(it.throwable, LogAndBreadcrumb.LIST, it.throwable.message ?: "Could not load gas station list")
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
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

    fun onLocationEnabledChanged(enabled: Boolean) {
        locationRepository.onLocationEnabledChanged(enabled)
    }

    fun onLocationPermissionChanged(granted: Boolean) {
        locationRepository.onLocationPermissionChanged(granted)
    }

    fun refresh() {
        refresh.value = true
    }

    fun startFueling(context: Context, gasStation: GasStation) {
        LogAndBreadcrumb.i(LogAndBreadcrumb.LIST, "Start fueling")
        analytics.logEvent(FuelingStarted)
        AppKit.openFuelingApp(context = context, id = gasStation.id, callback = analytics.TrackingAppCallback())
    }

    fun startNavigation(context: Context, gasStation: GasStation) {
        LogAndBreadcrumb.i(LogAndBreadcrumb.LIST, "Start navigation to gas station")
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
}
