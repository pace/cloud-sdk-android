package car.pace.cofu.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import car.pace.cofu.data.GasStationRepository
import car.pace.cofu.data.LocationRepository
import car.pace.cofu.util.Constants.STOP_TIMEOUT_MILLIS
import car.pace.cofu.util.UiState
import car.pace.cofu.util.UiState.Loading.toUiState
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class DetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    gasStationRepository: GasStationRepository,
    locationRepository: LocationRepository
) : ViewModel() {

    private val id: String = checkNotNull(savedStateHandle["id"])

    private val refresh = MutableSharedFlow<Unit>(replay = 1).apply {
        tryEmit(Unit)
    }

    val uiState = refresh
        .flatMapLatest {
            gasStationRepository.getGasStation(id)
        }
        .map {
            it.recoverCatching { throwable ->
                gasStationRepository.getCachedGasStation(id) ?: throw throwable
            }.toUiState()
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = UiState.Loading
        )

    val userLocation = locationRepository.location
        .map { LatLng(it.latitude, it.longitude) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = null
        )

    fun refresh() {
        viewModelScope.launch {
            refresh.emit(Unit)
        }
    }
}
