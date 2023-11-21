package car.pace.cofu.data

import car.pace.cofu.di.coroutine.ApplicationScope
import car.pace.cofu.util.Constants.STOP_TIMEOUT_MILLIS
import cloud.pace.sdk.utils.LocationProvider
import com.google.android.gms.location.LocationRequest
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.shareIn

@Singleton
class LocationRepository @Inject constructor(
    @ApplicationScope private val externalScope: CoroutineScope,
    locationRequest: LocationRequest,
    locationProvider: LocationProvider
) {

    val location = locationProvider.locationFlow(locationRequest)
        .shareIn(
            scope = externalScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            replay = 1
        )
}
