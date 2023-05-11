package cloud.pace.sdk.poikit.geo

import android.location.Location
import cloud.pace.sdk.PACECloudSDK
import cloud.pace.sdk.appkit.app.api.AppAPI
import cloud.pace.sdk.poikit.POIKit
import cloud.pace.sdk.poikit.poi.GasStation
import cloud.pace.sdk.poikit.poi.toLocationPoint
import cloud.pace.sdk.poikit.utils.distanceTo
import cloud.pace.sdk.utils.Failure
import cloud.pace.sdk.utils.LocationProvider
import cloud.pace.sdk.utils.Success
import cloud.pace.sdk.utils.SystemManager
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

interface GeoAPIManager {

    suspend fun apps(latitude: Double, longitude: Double): Result<List<GeoGasStation>>
    suspend fun features(latitude: Double, longitude: Double): Result<List<GeoAPIFeature>>
    fun cofuGasStations(completion: (Result<List<CofuGasStation>>) -> Unit)
    fun cofuGasStations(location: Location, radius: Int, completion: (Result<List<GasStation>>) -> Unit)
    suspend fun isPoiInRange(poiId: String, location: Location? = null): Boolean
}

class GeoAPIManagerImpl(
    private val appApi: AppAPI,
    private val systemManager: SystemManager,
    private val locationProvider: LocationProvider
) : GeoAPIManager {

    private val scope = CoroutineScope(Dispatchers.Default)
    private var appsCache: AppsCache? = null
    private var cofuGasStationsCache: CofuGasStationsCache? = null

    init {
        scope.launch {
            try {
                // Execute GeoJson and location requests and cache building concurrently
                val deferredResponse = async(Dispatchers.IO) { appApi.getGeoApiApps() }
                val deferredLocation = async(Dispatchers.IO) { locationProvider.currentLocation(false) }

                val response = deferredResponse.await().getOrThrow() // Throw (handled) exception if GeoJson request fails
                loadCofuGasStationsCache(response)
                Timber.i("Successfully loaded initial CoFu gas stations cache")

                when (val locationResult = deferredLocation.await()) {
                    is Success -> {
                        val location = locationResult.result
                        if (location != null) {
                            loadAppsCache(location.latitude, location.longitude, response)
                            Timber.i("Successfully loaded initial apps cache")
                        } else {
                            Timber.w("Could not load initial apps cache because the location was null")
                        }
                    }

                    is Failure -> Timber.w(locationResult.throwable, "Could not load initial apps cache")
                }
            } catch (e: Exception) {
                Timber.w(e, "Failed loading initial GeoJson cache")
            }
        }
    }

    override suspend fun apps(latitude: Double, longitude: Double): Result<List<GeoGasStation>> {
        if (isAppsCacheValid(latitude, longitude)) {
            return Result.success(getApps(latitude, longitude))
        } else {
            val response = appApi.getGeoApiApps().getOrElse {
                return Result.failure(it)
            }
            loadAppsCache(latitude, longitude, response)

            return Result.success(getApps(latitude, longitude))
        }
    }

    override suspend fun features(latitude: Double, longitude: Double): Result<List<GeoAPIFeature>> {
        if (isAppsCacheValid(latitude, longitude)) {
            return Result.success(appsCache?.features ?: emptyList())
        } else {
            val response = appApi.getGeoApiApps().getOrElse {
                return Result.failure(it)
            }
            val newCache = loadAppsCache(latitude, longitude, response)

            return Result.success(newCache.features)
        }
    }

    override fun cofuGasStations(completion: (Result<List<CofuGasStation>>) -> Unit) {
        val cache = cofuGasStationsCache
        if (cache != null && systemManager.getCurrentTimeMillis() - cache.time <= CACHE_MAX_AGE) {
            completion(Result.success(cache.cofuGasStations))
        } else {
            scope.launch {
                val response = withContext(Dispatchers.IO) {
                    appApi.getGeoApiApps()
                }

                response.onSuccess {
                    val newCache = loadCofuGasStationsCache(it)
                    withContext(Dispatchers.Main) {
                        completion(Result.success(newCache.cofuGasStations))
                    }
                }

                response.onFailure {
                    withContext(Dispatchers.Main) {
                        completion(Result.failure(it))
                    }
                }
            }
        }
    }

    override fun cofuGasStations(location: Location, radius: Int, completion: (Result<List<GasStation>>) -> Unit) {
        cofuGasStations { response ->
            response.onSuccess { cofuGasStations ->
                val targetLocation = LatLng(location.latitude, location.longitude)
                val cofuGasStationsInRange = cofuGasStations.filter { station -> station.coordinate.distanceTo(targetLocation) < radius }
                val locations = cofuGasStationsInRange.associate { station -> station.id to station.coordinate.toLocationPoint() }

                scope.launch {
                    val result = POIKit
                        .getGasStations(locations)
                        .map { gasStations ->
                            gasStations.mapNotNull { gasStation ->
                                val cofuGasStation = cofuGasStationsInRange.firstOrNull { cofuGasStation -> cofuGasStation.id == gasStation.id }
                                if (cofuGasStation != null) {
                                    gasStation.also { it.additionalProperties = cofuGasStation.properties }
                                } else {
                                    null
                                }
                            }
                        }

                    withContext(Dispatchers.Main) {
                        completion(result)
                    }
                }
            }

            response.onFailure {
                completion(Result.failure(it))
            }
        }
    }

    override suspend fun isPoiInRange(poiId: String, location: Location?): Boolean {
        val userLocation = location ?: when (val currentLocation = locationProvider.currentLocation(true)) {
            is Success -> {
                currentLocation.result ?: when (val validLocation = locationProvider.firstValidLocation()) {
                    is Success -> validLocation.result
                    is Failure -> null
                }
            }

            is Failure -> null
        }
        return if (userLocation != null) {
            isPoiInRange(poiId, userLocation.latitude, userLocation.longitude)
        } else {
            false
        }
    }

    private fun getApps(latitude: Double, longitude: Double): List<GeoGasStation> {
        return appsCache?.features
            ?.filter {
                it.isInRange(latitude, longitude, PACECloudSDK.configuration.appsDistanceThresholdInMeters)
            }
            ?.map {
                val appUrls = mutableMapOf<String, MutableSet<String>>()
                (it.properties[APPS_KEY] as? List<*>)?.forEach { app ->
                    val map = app as? Map<*, *>
                    if (map != null) {
                        val type = map[TYPE_KEY] as? String
                        val url = map[URL_KEY] as? String
                        if (type != null && url != null) {
                            appUrls[type] = appUrls[type]?.apply { add(url) } ?: mutableSetOf(url)
                        }
                    }
                }
                GeoGasStation(it.id, appUrls, it.coordinates().firstOrNull())
            } ?: emptyList()
    }

    private fun loadAppsCache(latitude: Double, longitude: Double, response: GeoAPIResponse): AppsCache {
        val center = LatLng(latitude, longitude)
        val time = systemManager.getCurrentTimeMillis()
        val features = response.features.filter {
            it.coordinates().all { coordinate ->
                isInRadius(coordinate.latitude, coordinate.longitude, center)
            }
        }

        return AppsCache(features, time, center).also { appsCache = it }
    }

    private fun loadCofuGasStationsCache(response: GeoAPIResponse): CofuGasStationsCache {
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
                val status = (it.properties[CONNECTED_FUELING_STATUS_KEY] as? String)?.let { status ->
                    ConnectedFuelingStatus.values().associateBy(ConnectedFuelingStatus::value)[status]
                }
                if (lat != null && lng != null) {
                    CofuGasStation(it.id, LatLng(lat, lng), status, it.properties)
                } else {
                    null
                }
            }
        }.flatten()

        return CofuGasStationsCache(cofuGasStations, time).also { cofuGasStationsCache = it }
    }

    private suspend fun isPoiInRange(poiId: String, latitude: Double, longitude: Double): Boolean {
        return features(latitude, longitude)
            .getOrNull()
            ?.firstOrNull { it.id == poiId }
            ?.isInRange(latitude, longitude, IS_POI_IN_RANGE_DISTANCE_THRESHOLD) ?: false
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
        const val FUELING_TYPE = "fueling"
        const val APPS_KEY = "apps"
        const val TYPE_KEY = "type"
        const val URL_KEY = "url"
        const val CONNECTED_FUELING_STATUS_KEY = "connectedFuelingStatus"

        private const val CACHE_MAX_AGE = 60 * 60 * 1000 // 60 min
        private const val CACHE_RADIUS = 30 * 1000 // 30 km
        private const val IS_POI_IN_RANGE_DISTANCE_THRESHOLD = 500 // meters
    }
}
