package cloud.pace.sdk.appkit.app.drawer.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cloud.pace.sdk.appkit.AppManager
import cloud.pace.sdk.appkit.app.api.UriManager
import cloud.pace.sdk.appkit.communication.AppEventManager
import cloud.pace.sdk.appkit.model.App
import cloud.pace.sdk.utils.LocationProvider
import cloud.pace.sdk.utils.LocationProviderImpl.Companion.DEFAULT_LOCATION_REQUEST
import cloud.pace.sdk.utils.Success
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.Priority
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

abstract class AppDrawerViewModel : ViewModel() {

    abstract val apps: StateFlow<List<App>>
}

class AppDrawerViewModelImpl internal constructor(
    locationProvider: LocationProvider,
    appManager: AppManager,
    eventManager: AppEventManager,
    uriManager: UriManager
) : AppDrawerViewModel() {

    private val locationRequest = LocationRequest.Builder(DEFAULT_LOCATION_REQUEST)
        .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
        .setMinUpdateDistanceMeters(MIN_UPDATE_DISTANCE_METERS)
        .build()

    override val apps = locationProvider.locationFlow(locationRequest)
        .map {
            (appManager.requestLocalApps(it) as? Success)?.result ?: emptyList()
        }
        .combine(eventManager.disabledHost) { apps, disabledHost ->
            val host = disabledHost?.getContentIfNotHandled() ?: return@combine apps
            apps.filter { uriManager.getHost(it.url) != host }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = emptyList()
        )

    companion object {
        private const val MIN_UPDATE_DISTANCE_METERS = 15f
    }
}
