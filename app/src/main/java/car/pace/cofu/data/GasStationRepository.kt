package car.pace.cofu.data

import android.location.Location
import car.pace.cofu.util.resume
import cloud.pace.sdk.poikit.POIKit
import javax.inject.Inject
import kotlinx.coroutines.suspendCancellableCoroutine

class GasStationRepository @Inject constructor() {

    suspend fun requestCofuGasStations(location: Location, radius: Int) = suspendCancellableCoroutine {
        POIKit.requestCofuGasStations(location, radius, it::resume)
    }
}
