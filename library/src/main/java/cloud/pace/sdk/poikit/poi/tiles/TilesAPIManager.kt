package cloud.pace.sdk.poikit.poi.tiles

import cloud.pace.sdk.api.utils.RequestUtils.LOCATION_HEADER
import cloud.pace.sdk.poikit.poi.GasStation
import cloud.pace.sdk.poikit.poi.LocationPoint
import cloud.pace.sdk.poikit.poi.toTileQueryRequest
import cloud.pace.sdk.poikit.utils.POIKitConfig
import cloud.pace.sdk.poikit.utils.addPadding
import cloud.pace.sdk.poikit.utils.toTileQueryRequest
import com.google.android.gms.maps.model.VisibleRegion
import kotlinx.coroutines.async
import kotlinx.coroutines.supervisorScope
import timber.log.Timber
import java.net.HttpURLConnection

interface TilesAPIManager {

    suspend fun getTiles(visibleRegion: VisibleRegion, padding: Double, zoomLevel: Int = POIKitConfig.ZOOMLEVEL): Result<List<GasStation>>

    suspend fun getTiles(ids: List<String>, zoomLevel: Int = POIKitConfig.ZOOMLEVEL): Result<List<GasStation>>

    suspend fun getTiles(idsWithLocations: Map<String, LocationPoint>, zoomLevel: Int = POIKitConfig.ZOOMLEVEL): Result<List<GasStation>>

    suspend fun getTiles(id: String, zoomLevel: Int = POIKitConfig.ZOOMLEVEL): Result<GasStation>

    suspend fun getTiles(idWithLocation: Pair<String, LocationPoint>, zoomLevel: Int = POIKitConfig.ZOOMLEVEL): Result<GasStation>
}

class TilesAPIManagerImpl(private val poiApi: POIAPI) : TilesAPIManager {

    override suspend fun getTiles(visibleRegion: VisibleRegion, padding: Double, zoomLevel: Int): Result<List<GasStation>> {
        return runCatching {
            val paddedVisibleRegion = visibleRegion.addPadding(padding)
            val tileRequest = paddedVisibleRegion.toTileQueryRequest(zoomLevel)
            poiApi.getTiles(tileRequest)
        }.onFailure {
            Timber.e(it, "Failed fetching gas station within visible region: $visibleRegion")
        }
    }

    override suspend fun getTiles(ids: List<String>, zoomLevel: Int): Result<List<GasStation>> {
        val locations = runCatching {
            supervisorScope {
                ids
                    .map {
                        async { getGasStation(it) }
                    }
                    .mapNotNull {
                        val gasStation = runCatching { it.await() }.getOrNull() ?: return@mapNotNull null
                        val latitude = gasStation.latitude?.toDouble() ?: return@mapNotNull null
                        val longitude = gasStation.longitude?.toDouble() ?: return@mapNotNull null

                        return@mapNotNull gasStation.id to LocationPoint(latitude, longitude)
                    }
                    .toMap()
            }
        }.getOrElse {
            Timber.e(it, "Failed fetching gas station with IDs: $ids")
            return Result.failure(it)
        }

        return getTiles(locations, zoomLevel)
    }

    override suspend fun getTiles(idsWithLocations: Map<String, LocationPoint>, zoomLevel: Int): Result<List<GasStation>> {
        return runCatching {
            val tileRequest = idsWithLocations.values.toTileQueryRequest(zoomLevel)
            val ids = idsWithLocations.keys
            poiApi.getTiles(tileRequest).filter { it.id in ids }
        }.onFailure {
            Timber.e(it, "Failed fetching gas stations with IDs and locations: $idsWithLocations")
        }
    }

    override suspend fun getTiles(id: String, zoomLevel: Int): Result<GasStation> {
        val location = runCatching {
            val gasStation = getGasStation(id)
            val latitude = gasStation.latitude?.toDouble()
            val longitude = gasStation.longitude?.toDouble()

            if (latitude != null && longitude != null) {
                gasStation.id to LocationPoint(latitude, longitude)
            } else {
                throw Exception("Location of gas station with ID ${gasStation.id} is null")
            }
        }.getOrElse {
            Timber.e(it, "Failed fetching gas station with ID: $id")
            return Result.failure(it)
        }

        return getTiles(location, zoomLevel)
    }

    override suspend fun getTiles(idWithLocation: Pair<String, LocationPoint>, zoomLevel: Int): Result<GasStation> {
        return runCatching {
            val tileRequest = idWithLocation.second.toTileQueryRequest(zoomLevel)
            poiApi.getTiles(tileRequest).find { it.id == idWithLocation.first } ?: throw Exception("Could not find a gas station with ID: ${idWithLocation.first}")
        }.onFailure {
            Timber.e(it, "Failed fetching gas station with ID and location: $idWithLocation")
        }
    }

    private suspend fun getGasStation(id: String, redirectCount: Int = 1): cloud.pace.sdk.api.poi.generated.model.GasStation {
        if (redirectCount > MAX_REDIRECTS) throw Exception("Too many redirects (max: $MAX_REDIRECTS) for gas station with ID: $id")

        val response = poiApi.getGasStation(id)

        return when {
            response.isSuccessful -> response.body() ?: throw Exception("Gas station response body is null. Gas station ID: $id")
            response.code() == HttpURLConnection.HTTP_MOVED_PERM || response.code() == HttpURLConnection.HTTP_MOVED_TEMP || response.code() == TEMPORARY_REDIRECT -> {
                val newId = response.headers().get(LOCATION_HEADER)?.split("/")?.lastOrNull()
                if (!newId.isNullOrEmpty()) {
                    getGasStation(newId, redirectCount + 1)
                } else {
                    throw Exception("Moved gas station ID is null or empty. Old gas station ID: $id")
                }
            }

            else -> throw Exception("Could not find a gas station with ID: $id")
        }
    }

    companion object {
        private const val MAX_REDIRECTS = 3
        private const val TEMPORARY_REDIRECT = 307
    }
}
