package cloud.pace.sdk.poikit.geo

import android.location.Location
import cloud.pace.sdk.PACECloudSDK
import cloud.pace.sdk.appkit.app.api.AppAPI
import cloud.pace.sdk.poikit.POIKit
import cloud.pace.sdk.poikit.poi.GasStation
import cloud.pace.sdk.poikit.poi.toLocationPoint
import cloud.pace.sdk.poikit.utils.distanceTo
import cloud.pace.sdk.utils.*
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.suspendCancellableCoroutine
import timber.log.Timber

interface GeoAPIManager {

    fun apps(latitude: Double, longitude: Double, completion: (Result<List<GeoGasStation>>) -> Unit)
    fun features(poiId: String, latitude: Double, longitude: Double, completion: (Result<List<GeoAPIFeature>>) -> Unit)
    fun cofuGasStations(completion: (Result<List<CofuGasStation>>) -> Unit)
    fun cofuGasStations(location: Location, radius: Int, completion: (Result<List<GasStation>>) -> Unit)
    suspend fun isPoiInRange(poiId: String, location: Location? = null): Boolean
}

class GeoAPIManagerImpl(
    private val appApi: AppAPI,
    private val systemManager: SystemManager,
    private val locationProvider: LocationProvider
) : GeoAPIManager {

    private var appsCache: AppsCache? = null
    private var cofuGasStationsCache: CofuGasStationsCache? = null

    init {
        loadCofuGasStationsCache { result ->
            result.onSuccess {
                Timber.d("Successfully loaded initial CoFu gas stations cache")
            }
            result.onFailure { throwable ->
                Timber.e(throwable, "Failed loading initial CoFu gas stations cache")
            }
        }

        onBackgroundThread {
            when (val completion = locationProvider.currentLocation(false)) {
                is Success -> {
                    val location = completion.result
                    if (location != null) {
                        loadAppsCache(location.latitude, location.longitude) { result ->
                            result.onSuccess {
                                Timber.d("Successfully loaded initial apps cache")
                            }
                            result.onFailure { throwable ->
                                Timber.e(throwable, "Failed loading initial apps cache")
                            }
                        }
                    } else {
                        Timber.e("Failed loading initial apps cache because location is null")
                    }
                }
                is Failure -> Timber.e(completion.throwable, "Failed loading initial apps cache")
            }
        }
    }

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

    override fun cofuGasStations(location: Location, radius: Int, completion: (Result<List<GasStation>>) -> Unit) {
        cofuGasStations { result ->
            val targetLocation = LatLng(location.latitude, location.longitude)
            result.onSuccess { cofuGasStations ->
                val cofuGasStationsInRange = cofuGasStations.filter { station -> station.coordinate.distanceTo(targetLocation) < radius }
                val locations = cofuGasStationsInRange.map { station -> station.id to station.coordinate.toLocationPoint() }.toMap()

                POIKit.requestGasStations(locations) { gasStations ->
                    when (gasStations) {
                        is Success -> {
                            val cofuStations = gasStations.result.mapNotNull { gasStation ->
                                val cofuGasStation = cofuGasStationsInRange.firstOrNull { it.id == gasStation.id }
                                if (cofuGasStation != null) {
                                    gasStation.also { it.additionalProperties = cofuGasStation.properties }
                                } else {
                                    null
                                }
                            }
                            completion(Result.success(cofuStations))

                        }
                        is Failure -> completion(Result.failure(gasStations.throwable))
                    }
                }
            }

            result.onFailure {
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
        appApi.getGeoApiApps { result ->
            result.onSuccess { response ->
                val center = LatLng(latitude, longitude)
                val time = systemManager.getCurrentTimeMillis()
                val features = response.features.filter {
                    it.coordinates().all { coordinate ->
                        isInRadius(coordinate.latitude, coordinate.longitude, center)
                    }
                }

                appsCache = AppsCache(features, time, center)

                completion(Result.success(features))
            }
            result.onFailure { completion(Result.failure(it)) }
        }
    }

    private fun loadCofuGasStationsCache(completion: (Result<List<CofuGasStation>>) -> Unit) {
        appApi.getGeoApiApps { result ->
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
                        if (lat != null && lng != null) {
                            CofuGasStation(it.id, LatLng(lat, lng), status, it.properties)
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

    private suspend fun isPoiInRange(poiId: String, latitude: Double, longitude: Double): Boolean {
        return try {
            suspendCancellableCoroutine { continuation ->
                // Try to load the apps from the cache
                features(poiId, latitude, longitude) { response ->
                    response.onSuccess { geoAPIFeatures ->
                        val isPoiInRange = geoAPIFeatures
                            .firstOrNull { it.id == poiId }
                            ?.isInRange(latitude, longitude, IS_POI_IN_RANGE_DISTANCE_THRESHOLD) ?: false

                        continuation.resumeIfActive(isPoiInRange)
                    }

                    response.onFailure {
                        // Fetch the apps from the API
                        appApi.getLocationBasedApps(latitude, longitude) { response ->
                            response.onSuccess { apps ->
                                continuation.resumeIfActive(apps.any { it.references?.any { reference -> reference.resourceUuid == poiId } ?: false })
                            }

                            response.onFailure {
                                continuation.resumeIfActive(false)
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            false
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
        private const val IS_POI_IN_RANGE_DISTANCE_THRESHOLD = 500 // meters
    }
}
