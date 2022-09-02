package car.pace.cofu.ui.home

import android.location.Location
import android.util.Log
import androidx.databinding.ObservableArrayList
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableInt
import car.pace.cofu.R
import car.pace.cofu.core.events.FragmentEvent
import car.pace.cofu.core.events.ShowSnack
import car.pace.cofu.core.mvvm.BaseItemViewModel
import car.pace.cofu.core.mvvm.BaseViewModel
import car.pace.cofu.core.resources.ResourcesProvider
import car.pace.cofu.repository.UserDataRepository
import cloud.pace.sdk.poikit.POIKit
import cloud.pace.sdk.poikit.poi.GasStation
import cloud.pace.sdk.utils.Failure
import cloud.pace.sdk.utils.Success
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val resourceProvider: ResourcesProvider,
    private val userDataRepository: UserDataRepository
) : BaseViewModel() {

    private var loadingCount = 0
        set(value) {
            field = value
            swipeRefreshLayoutRefreshing.set(value > 0)
            // only show big loading screen when there are no results yet
            // the loading indicator of the swipe refresh layout is always shown
            if (value == 0 || items.size == 0) showInfo.set(value > 0)
        }

    val swipeRefreshLayoutRefreshing = ObservableBoolean(true)
    val showInfo = ObservableBoolean(true)
    val infoTitle = ObservableInt(R.string.HOME_LOOKING_FOR_LOCATION_TITLE)
    val infoText = ObservableInt(R.string.HOME_LOOKING_FOR_LOCATION_TEXT)
    val infoImageNoResults = ObservableBoolean(false)

    val items = ObservableArrayList<BaseItemViewModel>()

    var currentLocation: Location? = null
        set(value) {
            if (field == null && value != null) {
                // first location fix, load stations now
                field = value
                loadPetrolStations()
            } else if (value != null) {
                // location updated, update distances
                items.forEach {
                    (it as? PetrolStationItemViewModel)?.updateLocation(value)
                }
            }

            field = value
        }

    fun reload() {
        loadPetrolStations()
    }

    private fun loadPetrolStations() {
        val location = currentLocation ?: return
        infoTitle.set(R.string.DASHBOARD_LOADING_VIEW_TITLE)
        infoImageNoResults.set(false)
        infoText.set(R.string.DASHBOARD_LOADING_VIEW_DESCRIPTION)
        loadingCount += 1
        POIKit.requestCofuGasStations(location, 10000) {
            loadingCount -= 1
            when (it) {
                is Success -> setPetrolStations(it.result, location)
                is Failure -> setLoadingError(it.throwable)
            }
        }

    }

    private fun setLoadingError(throwable: Throwable) {
        Log.w("ConnectedFueling", throwable)
        // do not show big error screen but just a snackbar when there were results previously
        if (items.size > 0) {
            handleEvent(ShowSnack(resourceProvider.getString(R.string.HOME_LOADING_FAILED_TEXT)))
            return
        }
        showInfo.set(true)
        infoTitle.set(R.string.HOME_LOADING_FAILED)
        infoImageNoResults.set(true)
        infoText.set(R.string.HOME_LOADING_FAILED_TEXT)
    }

    private fun setPetrolStations(gasStations: List<GasStation>, location: Location) {
        items.clear()

        if (gasStations.isEmpty()) {
            showInfo.set(true)
            infoTitle.set(R.string.DASHBOARD_EMPTY_VIEW_TITLE)
            infoImageNoResults.set(true)
            infoText.set(R.string.DASHBOARD_EMPTY_VIEW_DESCRIPTION)
            return
        }

        val viewModels = gasStations.map {
            PetrolStationItemViewModel(
                it,
                userDataRepository.fuelType?.identifier,
                resourceProvider,
                this,
                location
            )
        }.sortedBy { it.distance }

        viewModels.forEachIndexed { index, viewModel ->
            when (index) {
                0 -> items.add(SimpleTextItemViewModel(resourceProvider.getString(R.string.DASHBOARD_SECTIONS_NEAREST_GAS_STATION)))
                1 -> items.add(SimpleTextItemViewModel(resourceProvider.getString(R.string.DASHBOARD_SECTIONS_OTHER_GAS_STATIONS)))
            }
            items.add(viewModel)
        }
    }

    fun updateFuelType() {
        val identifier = userDataRepository.fuelType?.identifier
        items.forEach {
            (it as? PetrolStationItemViewModel)?.fuelTypeIdentifier = identifier
        }
    }


    class StartNavigationEvent(val gasStation: GasStation) : FragmentEvent()
    class FuelUpEvent(val gasStation: GasStation) : FragmentEvent()

}