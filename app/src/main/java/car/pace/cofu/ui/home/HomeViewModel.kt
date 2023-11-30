package car.pace.cofu.ui.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import car.pace.cofu.data.GasStationRepository
import car.pace.cofu.data.LocationRepository
import car.pace.cofu.data.SharedPreferencesRepository
import car.pace.cofu.data.SharedPreferencesRepository.Companion.PREF_KEY_FUEL_TYPE
import car.pace.cofu.ui.wallet.fueltype.FuelType
import car.pace.cofu.util.Constants.STOP_TIMEOUT_MILLIS
import car.pace.cofu.util.UiState
import car.pace.cofu.util.UiState.Loading.toUiState
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.launch

@HiltViewModel
class HomeViewModel @Inject constructor(
    sharedPreferencesRepository: SharedPreferencesRepository,
    gasStationRepository: GasStationRepository,
    locationRepository: LocationRepository
) : ViewModel() {

    private val refresh = MutableSharedFlow<Unit>(replay = 1).apply {
        tryEmit(Unit)
    }

    var showPullRefreshIndicator by mutableStateOf(false)
    var showSnackbarError = MutableSharedFlow<Boolean>()
    private var dataAvailable = false

    val uiState = locationRepository.location
        .combine(refresh) { location, _ ->
            gasStationRepository.getGasStations(location)
                .toUiState()
                .also { state ->
                    if (state is UiState.Success) {
                        dataAvailable = state.data.isNotEmpty()
                    }
                }
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
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = null
        )

    val userLocation = locationRepository.location
        .map { LatLng(it.latitude, it.longitude) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = null
        )

    fun onRefresh() {
        viewModelScope.launch {
            showPullRefreshIndicator = true
            refresh.emit(Unit)
        }
    }
}
