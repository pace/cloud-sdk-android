package cloud.pace.sdk.appkit.geo

import cloud.pace.sdk.appkit.app.api.AppAPI
import cloud.pace.sdk.poikit.utils.distanceTo
import cloud.pace.sdk.utils.SystemManager
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.PolyUtil

interface GeoAPIManager {

    fun apps(latitude: Double, longitude: Double, completion: (Result<List<GeoGasStation>>) -> Unit)
    fun features(poiId: String, latitude: Double, longitude: Double, completion: (Result<List<GeoAPIFeature>>) -> Unit)
}

class GeoAPIManagerImpl(
    private val appAPI: AppAPI,
    private val systemManager: SystemManager
) : GeoAPIManager {

    private var cache: GeoAPICache? = null

    override fun apps(latitude: Double, longitude: Double, completion: (Result<List<GeoGasStation>>) -> Unit) {
        if (isCacheValid(latitude, longitude)) {
            completion(Result.success(loadApps(latitude, longitude)))
        } else {
            buildCache(latitude, longitude) {
                it.onSuccess {
                    completion(Result.success(loadApps(latitude, longitude)))
                }

                it.onFailure { throwable ->
                    completion(Result.failure(throwable))
                }
            }
        }
    }

    override fun features(poiId: String, latitude: Double, longitude: Double, completion: (Result<List<GeoAPIFeature>>) -> Unit) {
        if (isCacheValid(latitude, longitude)) {
            completion(Result.success(cache?.features ?: emptyList()))
        } else {
            buildCache(latitude, longitude, completion)
        }
    }

    private fun loadApps(latitude: Double, longitude: Double): List<GeoGasStation> {
        return cache?.features
            ?.filter {
                it.geometry?.coordinates
                    ?.map { coordinates ->
                        coordinates.mapNotNull { coordinate ->
                            val lat = coordinate.lastOrNull()
                            val lng = coordinate.firstOrNull()
                            if (lat != null && lng != null) {
                                LatLng(lat, lng)
                            } else {
                                null
                            }
                        }
                    }
                    ?.any { polygon ->
                        PolyUtil.containsLocation(latitude, longitude, polygon, false)
                    } ?: false
            }
            ?.mapNotNull {
                val id = it.id
                val apps = it.properties?.apps
                if (id != null && apps != null) {
                    GeoGasStation(id, apps)
                } else {
                    null
                }
            } ?: emptyList()
    }

    private fun buildCache(latitude: Double, longitude: Double, completion: (Result<List<GeoAPIFeature>>) -> Unit) {
        appAPI.getGeoApiApps { result ->
            result.onSuccess { response ->
                val center = LatLng(latitude, longitude)
                val time = systemManager.getCurrentTimeMillis()
                val features = response.features
                    ?.filter {
                        it.geometry?.coordinates
                            ?.all { coordinates ->
                                coordinates.all { coordinate ->
                                    val lat = coordinate.lastOrNull()
                                    val lng = coordinate.firstOrNull()
                                    isInRadius(lat, lng, center)
                                }
                            } ?: false
                    } ?: emptyList()

                cache = GeoAPICache(features, time, center)

                completion(Result.success(features))
            }
            result.onFailure { completion(Result.failure(it)) }
        }
    }

    private fun isCacheValid(latitude: Double, longitude: Double): Boolean {
        val cache = cache
        return cache != null && isInRadius(latitude, longitude, cache.center) && systemManager.getCurrentTimeMillis() - cache.time <= CACHE_MAX_AGE
    }

    private fun isInRadius(latitude: Double?, longitude: Double?, center: LatLng): Boolean {
        return if (latitude != null && longitude != null) {
            LatLng(latitude, longitude).distanceTo(center) < CACHE_RADIUS
        } else {
            false
        }
    }

    data class GeoAPICache(val features: List<GeoAPIFeature>, val time: Long, val center: LatLng)

    companion object {
        private const val CACHE_MAX_AGE = 60 * 60 * 1000 // 60 min
        private const val CACHE_RADIUS = 30 * 1000 // 30 km
    }
}
