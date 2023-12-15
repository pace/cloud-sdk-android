package car.pace.cofu.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import car.pace.cofu.data.GasStationRepository
import car.pace.cofu.data.LocationRepository
import car.pace.cofu.data.SharedPreferencesRepository
import car.pace.cofu.data.SharedPreferencesRepository.Companion.PREF_KEY_FUEL_TYPE
import car.pace.cofu.ui.wallet.fueltype.toFuelTypeGroup
import car.pace.cofu.util.Constants.STOP_TIMEOUT_MILLIS
import car.pace.cofu.util.UiState
import car.pace.cofu.util.UiState.Loading.toUiState
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
    locationRepository: LocationRepository
) : ViewModel() {

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
                val gasStations = gasStationRepository.getGasStations(location).toUiState()
                emit(gasStations)
            }
        }
    }.onEach {
        refresh.value = false
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

    class LocationPermissionDenied : Throwable()
    class LocationDisabled : Throwable()
}
