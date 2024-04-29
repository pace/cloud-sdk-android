package car.pace.cofu.ui.detail

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import car.pace.cofu.data.GasStationRepository
import car.pace.cofu.data.analytics.Analytics
import car.pace.cofu.data.analytics.FuelingStarted
import car.pace.cofu.data.analytics.StationNavigationUsed
import car.pace.cofu.data.location.LocationRepository
import car.pace.cofu.util.Constants.STOP_TIMEOUT_MILLIS
import car.pace.cofu.util.IntentUtils
import car.pace.cofu.util.LogAndBreadcrumb
import car.pace.cofu.util.UiState
import car.pace.cofu.util.UiState.Loading.toUiState
import car.pace.cofu.util.extension.isInFrance
import car.pace.cofu.util.extension.toLatLng
import cloud.pace.sdk.appkit.AppKit
import cloud.pace.sdk.poikit.poi.GasStation
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class DetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    gasStationRepository: GasStationRepository,
    locationRepository: LocationRepository,
    private val analytics: Analytics
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
        .onEach {
            if (it is UiState.Error) {
                LogAndBreadcrumb.e(it.throwable, LogAndBreadcrumb.DETAIL, "Loading of gas station failed")
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = UiState.Loading
        )

    val userLocation = locationRepository.location
        .map { result ->
            result.getOrNull()?.toLatLng()
        }
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

    fun shouldShowLegalWarning(gasStation: GasStation): Boolean = gasStation.isInFrance()

    fun startFueling(context: Context, gasStation: GasStation) {
        LogAndBreadcrumb.i(LogAndBreadcrumb.DETAIL, "Start fueling")
        analytics.logEvent(FuelingStarted)
        AppKit.openFuelingApp(context = context, id = gasStation.id, callback = analytics.TrackingAppCallback())
    }

    fun startNavigation(context: Context, gasStation: GasStation) {
        LogAndBreadcrumb.i(LogAndBreadcrumb.DETAIL, "Start navigation to gas station")
        IntentUtils.startNavigation(context, gasStation).onSuccess {
            analytics.logEvent(StationNavigationUsed)
        }
    }
}
