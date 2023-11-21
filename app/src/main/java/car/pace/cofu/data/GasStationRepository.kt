package car.pace.cofu.data

import android.location.Location
import car.pace.cofu.data.cache.GasStationCache
import car.pace.cofu.util.Constants.GAS_STATION_SEARCH_RADIUS
import car.pace.cofu.util.extension.resume
import cloud.pace.sdk.poikit.POIKit
import cloud.pace.sdk.poikit.poi.GasStation
import cloud.pace.sdk.poikit.poi.LocationPoint
import cloud.pace.sdk.poikit.utils.distanceTo
import com.google.android.gms.maps.model.LatLng
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.suspendCancellableCoroutine

@Singleton
class GasStationRepository @Inject constructor(
    private val gasStationCache: GasStationCache
) {

    suspend fun getGasStations(location: Location, radius: Int = GAS_STATION_SEARCH_RADIUS): Result<List<GasStation>> {
        return suspendCancellableCoroutine {
            POIKit.requestCofuGasStations(location, radius, it::resume)
        }.mapCatching { gasStations ->
            val latLng = LatLng(location.latitude, location.longitude)
            gasStations
                .sortedBy {
                    it.center?.toLatLn()?.distanceTo(latLng)
                }
                .also {
                    gasStationCache.put(it)
                }
        }
    }

    fun getGasStation(id: String, useCache: Boolean = true) = flow {
        val cached = getCachedGasStation(id)
        if (useCache && cached != null) {
            emit(Result.success(cached))
        }

        val latitude = cached?.latitude
        val longitude = cached?.longitude
        val response = if (latitude != null && longitude != null) {
            POIKit.getGasStation(id to LocationPoint(latitude, longitude))
        } else {
            POIKit.getGasStation(id)
        }

        val fresh = response.onSuccess {
            gasStationCache.put(it)
        }

        emit(fresh)
    }

    fun getCachedGasStation(id: String): GasStation? {
        return gasStationCache.getOrNull(id)
    }
}
