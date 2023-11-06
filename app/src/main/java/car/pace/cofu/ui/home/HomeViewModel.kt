package car.pace.cofu.ui.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import car.pace.cofu.data.GasStationRepository
import car.pace.cofu.data.SharedPreferencesRepository
import car.pace.cofu.data.SharedPreferencesRepository.Companion.PREF_KEY_FUEL_TYPE
import car.pace.cofu.ui.fueltype.FuelType
import car.pace.cofu.util.UiState
import cloud.pace.sdk.poikit.utils.distanceTo
import cloud.pace.sdk.utils.LocationProvider
import cloud.pace.sdk.utils.LocationProviderImpl.Companion.DEFAULT_LOCATION_REQUEST
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    locationProvider: LocationProvider,
    sharedPreferencesRepository: SharedPreferencesRepository,
    private val gasStationRepository: GasStationRepository
) : ViewModel() {

    private val locationRequest = LocationRequest.Builder(DEFAULT_LOCATION_REQUEST)
        .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
        .setMinUpdateDistanceMeters(LOCATION_UPDATE_DISTANCE_METERS)
        .build()

    private val location = locationProvider.locationFlow(locationRequest)
    private val refresh = MutableSharedFlow<Unit>(replay = 1).apply {
        tryEmit(Unit)
    }

    var showPullRefreshIndicator by mutableStateOf(false)
    var showSnackbarError = MutableSharedFlow<Boolean>()
    private var dataAvailable = false

    val uiState = location
        .combine(refresh) { location, _ ->
            val gasStations = gasStationRepository.requestCofuGasStations(location, GAS_STATION_SEARCH_RADIUS).getOrElse {
                return@combine UiState.Error(it)
            }
            val latLng = LatLng(location.latitude, location.longitude)
            val sortedGasStations = gasStations.sortedBy { it.center?.toLatLn()?.distanceTo(latLng) }

            dataAvailable = sortedGasStations.isNotEmpty()
            UiState.Success(sortedGasStations)
        }
        .onEach {
            showPullRefreshIndicator = false
        }
        .transform {
            // Skip fullscreen error if data is already visible - show a snackbar instead
            if (it is UiState.Error && dataAvailable) {
                showSnackbarError.emit(true)
            } else {
                showSnackbarError.emit(false)
                emit(it)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = UiState.Loading
        )

    val fuelType = sharedPreferencesRepository.getValue(PREF_KEY_FUEL_TYPE, -1)
        .filter { it != -1 }
        .map { FuelType.values().getOrNull(it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(DEFAULT_TIMEOUT_MILLIS),
            initialValue = null
        )

    val userLocation = location
        .map { LatLng(it.latitude, it.longitude) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(DEFAULT_TIMEOUT_MILLIS),
            initialValue = null
        )

    fun onRefresh() {
        viewModelScope.launch {
            showPullRefreshIndicator = true
            refresh.emit(Unit)
        }
    }

    companion object {
        private const val DEFAULT_TIMEOUT_MILLIS = 5000L
        private const val LOCATION_UPDATE_DISTANCE_METERS = 500f
        private const val GAS_STATION_SEARCH_RADIUS = 10000
    }
}
