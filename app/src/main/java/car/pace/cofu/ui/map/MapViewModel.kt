package car.pace.cofu.ui.map

import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import car.pace.cofu.SearchRepository
import car.pace.cofu.data.GasStationRepository
import car.pace.cofu.data.LocationRepository
import car.pace.cofu.data.SharedPreferencesRepository
import car.pace.cofu.ui.wallet.fueltype.toFuelTypeGroup
import car.pace.cofu.util.Constants.MAX_SEARCH_RESULTS
import car.pace.cofu.util.Constants.MIN_REDUCED_MARKER_DETAIL
import car.pace.cofu.util.Constants.MIN_SEARCH_QUERY_LENGTH
import car.pace.cofu.util.Constants.SEARCH_DEBOUNCE_MILLIS
import car.pace.cofu.util.Constants.STOP_TIMEOUT_MILLIS
import car.pace.cofu.util.Constants.STREET_ZOOM_LEVEL
import car.pace.cofu.util.Constants.VISIBLE_REGION_PADDING
import car.pace.cofu.util.UiState
import car.pace.cofu.util.UiState.Loading.toUiState
import car.pace.cofu.util.data
import car.pace.cofu.util.extension.toLatLng
import cloud.pace.sdk.poikit.utils.addPadding
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.VisibleRegion
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.Place
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transformLatest

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
@HiltViewModel
class MapViewModel @Inject constructor(
    sharedPreferencesRepository: SharedPreferencesRepository,
    gasStationRepository: GasStationRepository,
    private val locationRepository: LocationRepository,
    private val searchRepository: SearchRepository
) : ViewModel() {

    private val paddedVisibleRegion = MutableStateFlow<VisibleRegion?>(null)
    private val visibleRegion = paddedVisibleRegion.distinctUntilChanged { old, new -> new != null && old?.contains(new) == true }.filterNotNull()
    private val zoomLevel = MutableStateFlow(STREET_ZOOM_LEVEL)

    private val reducedMarkers = visibleRegion.map {
        gasStationRepository.getCofuGasStations(it).map { gasStations ->
            gasStations.map { gasStation ->
                val markerDetails = ReducedMarkerDetails(gasStation.id)
                MarkerItem(markerDetails, gasStation.coordinate)
            }
        }.toUiState()
    }

    private val fullMarkers = visibleRegion.map {
        gasStationRepository.getGasStations(it).map { gasStations ->
            gasStations.mapNotNull { gasStation ->
                val position = gasStation.center?.toLatLn() ?: return@mapNotNull null
                val markerDetails = FullMarkerDetails(gasStation)
                MarkerItem(markerDetails, position)
            }
        }.toUiState()
    }

    val uiState = zoomLevel.flatMapLatest {
        if (it < MIN_REDUCED_MARKER_DETAIL) reducedMarkers else fullMarkers
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = UiState.Loading
    )

    private val initialValue = sharedPreferencesRepository.getInt(SharedPreferencesRepository.PREF_KEY_FUEL_TYPE, -1)
    val fuelTypeGroup = sharedPreferencesRepository
        .getValue(SharedPreferencesRepository.PREF_KEY_FUEL_TYPE, initialValue)
        .map { it.toFuelTypeGroup() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = initialValue.toFuelTypeGroup()
        )

    val userLocation = locationRepository.location
        .map { result ->
            result.map { it.toLatLng() }.toUiState()
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = UiState.Loading
        )

    val searchQuery = MutableStateFlow(TextFieldValue())
    val searchResults = searchQuery
        .debounce {
            if (it.text.isEmpty()) 0L else SEARCH_DEBOUNCE_MILLIS
        }
        .filter {
            it.text.isEmpty() || it.text.length >= MIN_SEARCH_QUERY_LENGTH
        }
        .distinctUntilChanged()
        .transformLatest {
            if (it.text.isEmpty()) {
                emit(emptyList())
            } else {
                val location = userLocation.value.data
                val predictions = searchRepository.findPredictions(it.text, location).getOrNull()?.take(MAX_SEARCH_RESULTS) ?: return@transformLatest
                emit(predictions)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = emptyList()
        )

    fun onMapMovementStop(visibleRegion: VisibleRegion?, zoom: Float) {
        if (visibleRegion == null || zoom <= 0.0) return
        paddedVisibleRegion.value = visibleRegion.addPadding(VISIBLE_REGION_PADDING)
        zoomLevel.value = zoom
    }

    fun onLocationEnabledChanged(enabled: Boolean) {
        locationRepository.onLocationEnabledChanged(enabled)
    }

    fun onLocationPermissionChanged(granted: Boolean) {
        locationRepository.onLocationPermissionChanged(granted)
    }

    fun onSearchQueryChange(textFieldValue: TextFieldValue) {
        searchQuery.value = textFieldValue
    }

    suspend fun onSearchResultClick(searchResult: AutocompletePrediction): Place? {
        return searchRepository.findPlace(searchResult.placeId).getOrNull()
    }

    private fun VisibleRegion.contains(other: VisibleRegion): Boolean {
        return latLngBounds.contains(other.latLngBounds.northeast) && latLngBounds.contains(other.latLngBounds.southwest)
    }

    companion object {
        val fallbackLocation = LatLng(51.1642292, 10.4541194) // Germany
    }
}
