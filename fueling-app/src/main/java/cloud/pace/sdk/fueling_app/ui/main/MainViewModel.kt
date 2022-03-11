package cloud.pace.sdk.fueling_app.ui.main

import android.location.Location
import androidx.lifecycle.*
import cloud.pace.sdk.fueling_app.data.repository.Repository
import cloud.pace.sdk.fueling_app.util.Result
import cloud.pace.sdk.utils.LocationProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.filter
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: Repository,
    private val locationProvider: LocationProvider
) : ViewModel() {

    val lastLocation by lazy { MutableLiveData<Location>() }

    val cofuGasStations = locationProvider.location
        .asFlow()
        .filter { newLocation ->
            val location = lastLocation.value
            (location == null || newLocation.distanceTo(location) > LOCATION_DISTANCE_THRESHOLD)
        }
        .asLiveData()
        .switchMap { newLocation ->
            lastLocation.value = newLocation

            liveData {
                try {
                    emit(Result.Success(repository.requestCofuGasStations(newLocation, SEARCH_RADIUS)))
                } catch (e: Exception) {
                    emit(Result.Error(e))
                }
            }
        }

    fun requestLocationUpdates() {
        locationProvider.requestLocationUpdates()
    }

    companion object {
        private const val LOCATION_DISTANCE_THRESHOLD = 15 // in meters
        private const val SEARCH_RADIUS = 150 // in meters
    }
}
