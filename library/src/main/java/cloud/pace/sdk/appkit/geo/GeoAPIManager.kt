package cloud.pace.sdk.appkit.geo

import cloud.pace.sdk.api.geo.*
import cloud.pace.sdk.appkit.app.api.AppAPI
import cloud.pace.sdk.poikit.utils.distanceTo
import cloud.pace.sdk.utils.SystemManager
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.PolyUtil

interface GeoAPIManager {

    fun apps(latitude: Double, longitude: Double, completion: (Result<List<GeoGasStation>>) -> Unit)
    fun features(poiId: String, latitude: Double, longitude: Double, completion: (Result<List<GeoAPIFeature>>) -> Unit)
    fun cofuGasStations(completion: (Result<List<CofuGasStation>>) -> Unit)
}

class GeoAPIManagerImpl(
    private val appAPI: AppAPI,
    private val systemManager: SystemManager
) : GeoAPIManager {

    private var appsCache: AppsCache? = null
    private var cofuGasStationsCache: CofuGasStationsCache? = null

    override fun apps(latitude: Double, longitude: Double, completion: (Result<List<GeoGasStation>>) -> Unit) {
        if (isAppsCacheValid(latitude, longitude)) {
            completion(Result.success(getApps(latitude, longitude)))
        } else {
            loadAppsCache(latitude, longitude) {
                it.onSuccess {
                    completion(Result.success(getApps(latitude, longitude)))
                }

                it.onFailure { throwable ->
                    completion(Result.failure(throwable))
                }
            }
        }
    }

    override fun features(poiId: String, latitude: Double, longitude: Double, completion: (Result<List<GeoAPIFeature>>) -> Unit) {
        if (isAppsCacheValid(latitude, longitude)) {
            completion(Result.success(appsCache?.features ?: emptyList()))
        } else {
            loadAppsCache(latitude, longitude, completion)
        }
    }

    override fun cofuGasStations(completion: (Result<List<CofuGasStation>>) -> Unit) {
        val cache = cofuGasStationsCache
        if (cache != null && systemManager.getCurrentTimeMillis() - cache.time <= CACHE_MAX_AGE) {
            completion(Result.success(cache.cofuGasStations))
        } else {
            loadCofuGasStationsCache(completion)
        }
    }

    private fun getApps(latitude: Double, longitude: Double): List<GeoGasStation> {
        return appsCache?.features
            ?.filter {
                val polygons = when (it.geometry) {
                    is GeometryCollection -> it.geometry.geometries.filterIsInstance<Polygon>()
                    is Polygon -> listOf(it.geometry)
                    else -> emptyList()
                }

                polygons.map { polygon ->
                    polygon.coordinates.map { ring ->
                        ring.mapNotNull { coordinate ->
                            val lat = coordinate.lastOrNull()
                            val lng = coordinate.firstOrNull()
                            if (lat != null && lng != null) {
                                LatLng(lat, lng)
                            } else {
                                null
                            }
                        }
                    }
                }.flatten().any { ring ->
                    PolyUtil.containsLocation(latitude, longitude, ring, false)
                }
            }
            ?.mapNotNull {
                (it.properties["apps"] as? List<*>)
                    ?.mapNotNull { app ->
                        ((app as? Map<*, *>)?.get("url") as? String)
                    }?.let { urls ->
                        GeoGasStation(it.id, urls)
                    }
            } ?: emptyList()
    }

    private fun loadAppsCache(latitude: Double, longitude: Double, completion: (Result<List<GeoAPIFeature>>) -> Unit) {
        appAPI.getGeoApiApps { result ->
            result.onSuccess { response ->
                val center = LatLng(latitude, longitude)
                val time = systemManager.getCurrentTimeMillis()
                val features = response.features.filter {
                    val polygons = when (it.geometry) {
                        is GeometryCollection -> it.geometry.geometries.filterIsInstance<Polygon>()
                        is Polygon -> listOf(it.geometry)
                        else -> emptyList()
                    }

                    polygons.map { polygon -> polygon.coordinates }.flatten().flatten().all { coordinate ->
                        val lat = coordinate.lastOrNull()
                        val lng = coordinate.firstOrNull()
                        isInRadius(lat, lng, center)
                    }
                }

                appsCache = AppsCache(features, time, center)

                completion(Result.success(features))
            }
            result.onFailure { completion(Result.failure(it)) }
        }
    }

    private fun loadCofuGasStationsCache(completion: (Result<List<CofuGasStation>>) -> Unit) {
        appAPI.getGeoApiApps { result ->
            result.onSuccess { response ->
                val time = systemManager.getCurrentTimeMillis()
                val cofuGasStations = response.features.map {
                    val points = when (it.geometry) {
                        is GeometryCollection -> it.geometry.geometries.filterIsInstance<Point>()
                        is Point -> listOf(it.geometry)
                        else -> emptyList()
                    }

                    points.mapNotNull { point ->
                        val lat = point.coordinates.lastOrNull()
                        val lng = point.coordinates.firstOrNull()
                        val status = (it.properties["connectedFuelingStatus"] as? String)?.let { status ->
                            ConnectedFuelingStatus.values().associateBy(ConnectedFuelingStatus::value)[status]
                        }
                        if (lat != null && lng != null && status != null) {
                            CofuGasStation(it.id, LatLng(lat, lng), status)
                        } else {
                            null
                        }
                    }
                }.flatten()

                cofuGasStationsCache = CofuGasStationsCache(cofuGasStations, time)

                completion(Result.success(cofuGasStations))
            }
            result.onFailure { completion(Result.failure(it)) }
        }
    }

    private fun isAppsCacheValid(latitude: Double, longitude: Double): Boolean {
        val cache = appsCache
        return cache != null && isInRadius(latitude, longitude, cache.center) && systemManager.getCurrentTimeMillis() - cache.time <= CACHE_MAX_AGE
    }

    private fun isInRadius(latitude: Double?, longitude: Double?, center: LatLng): Boolean {
        return if (latitude != null && longitude != null) {
            LatLng(latitude, longitude).distanceTo(center) < CACHE_RADIUS
        } else {
            false
        }
    }

    data class AppsCache(val features: List<GeoAPIFeature>, val time: Long, val center: LatLng)

    data class CofuGasStationsCache(val cofuGasStations: List<CofuGasStation>, val time: Long)

    companion object {
        private const val CACHE_MAX_AGE = 60 * 60 * 1000 // 60 min
        private const val CACHE_RADIUS = 30 * 1000 // 30 km
    }
}
