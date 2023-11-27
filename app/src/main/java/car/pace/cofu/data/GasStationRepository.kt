package car.pace.cofu.data

import android.location.Location
import car.pace.cofu.util.extension.resume
import cloud.pace.sdk.poikit.POIKit
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.suspendCancellableCoroutine

@Singleton
class GasStationRepository @Inject constructor() {

    suspend fun requestCofuGasStations(location: Location, radius: Int) = suspendCancellableCoroutine {
        POIKit.requestCofuGasStations(location, radius, it::resume)
    }
}
