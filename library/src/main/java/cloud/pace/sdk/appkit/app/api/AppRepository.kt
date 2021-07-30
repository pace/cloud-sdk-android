package cloud.pace.sdk.appkit.app.api

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import cloud.pace.sdk.R
import cloud.pace.sdk.api.geo.CofuGasStation
import cloud.pace.sdk.api.geo.GeometryCollection
import cloud.pace.sdk.api.geo.Polygon
import cloud.pace.sdk.appkit.geo.GeoAPIManager
import cloud.pace.sdk.appkit.model.App
import cloud.pace.sdk.appkit.model.AppManifest
import cloud.pace.sdk.appkit.persistence.CacheModel
import cloud.pace.sdk.poikit.poi.GasStation
import cloud.pace.sdk.poikit.utils.distanceTo
import cloud.pace.sdk.utils.*
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.*
import timber.log.Timber

interface AppRepository {

    suspend fun getLocationBasedApps(latitude: Double, longitude: Double): Completion<List<App>>
    suspend fun getAllApps(): Completion<List<App>>
    suspend fun getAppsByUrl(url: String, references: List<String>): Completion<List<App>>
    suspend fun getUrlByAppId(appId: String): Completion<String?>
    fun getCofuGasStations(completion: (Result<List<CofuGasStation>>) -> Unit)
    fun getCofuGasStations(location: Location, radius: Int, completion: (Result<List<GasStation>>) -> Unit)
    suspend fun isPoiInRange(poiId: String, latitude: Double, longitude: Double): Boolean
}

class AppRepositoryImpl(
    private val context: Context,
    private val cache: CacheModel,
    private val appApi: AppAPI,
    private val uriUtil: UriManager,
    private val geoApiManager: GeoAPIManager
) : AppRepository {

    override suspend fun getLocationBasedApps(latitude: Double, longitude: Double): Completion<List<App>> {
        return try {
            val deferred = suspendCancellableCoroutine<List<Deferred<List<App>>>> { continuation ->
                // Try to load the apps from the cache
                geoApiManager.apps(latitude, longitude) { response ->
                    response.onSuccess {
                        val result = it.flatMap { geoGasStation ->
                            geoGasStation.appUrls.map { url ->
                                CoroutineScope(Dispatchers.IO).async {
                                    castLocationBasedApp(url, listOf(geoGasStation.id))
                                }
                            }
                        }

                        continuation.resumeIfActive(result)
                    }

                    response.onFailure {
                        // Fetch the apps from the API
                        appApi.getLocationBasedApps(latitude, longitude) { response ->
                            response.onSuccess { apps ->
                                val result = apps.map {
                                    CoroutineScope(Dispatchers.IO).async {
                                        castLocationBasedApp(it.pwaUrl, it.references)
                                    }
                                }
                                continuation.resumeIfActive(result)
                            }

                            response.onFailure { throwable ->
                                continuation.resumeWithExceptionIfActive(throwable)
                            }
                        }
                    }
                }
            }
            Success(deferred.awaitAll().flatten())
        } catch (e: Exception) {
            Failure(e)
        }
    }

    override suspend fun getAllApps(): Completion<List<App>> {
        return try {
            val deferred = suspendCancellableCoroutine<List<Deferred<List<App>>>> { continuation ->
                appApi.getAllApps { response ->
                    response.onSuccess { apps ->
                        val result = apps.map {
                            CoroutineScope(Dispatchers.IO).async {
                                castLocationBasedApp(it.pwaUrl, null)
                            }
                        }
                        continuation.resumeIfActive(result)
                    }

                    response.onFailure { throwable ->
                        continuation.resumeWithExceptionIfActive(throwable)
                    }
                }
            }
            Success(deferred.awaitAll().flatten())
        } catch (e: Exception) {
            Failure(e)
        }
    }

    override suspend fun getAppsByUrl(url: String, references: List<String>): Completion<List<App>> {
        val apps = castLocationBasedApp(url, references)
        return if (apps.isNotEmpty()) {
            Success(apps)
        } else {
            Failure(Exception("Could not load Apps for URL $url"))
        }
    }

    override suspend fun getUrlByAppId(appId: String): Completion<String?> {
        return try {
            val result = suspendCancellableCoroutine<String?> { continuation ->
                appApi.getAppByAppId(appId) { response ->
                    response.onSuccess { app ->
                        continuation.resumeIfActive(app.pwaUrl)
                    }

                    response.onFailure { throwable ->
                        continuation.resumeWithExceptionIfActive(throwable)
                    }
                }
            }
            Success(result)
        } catch (e: Exception) {
            Failure(e)
        }
    }

    override fun getCofuGasStations(completion: (Result<List<CofuGasStation>>) -> Unit) {
        geoApiManager.cofuGasStations(completion)
    }

    override fun getCofuGasStations(location: Location, radius: Int, completion: (Result<List<GasStation>>) -> Unit) {
        geoApiManager.cofuGasStations(location, radius, completion)
    }

    override suspend fun isPoiInRange(poiId: String, latitude: Double, longitude: Double): Boolean {
        return try {
            suspendCancellableCoroutine { continuation ->
                // Try to load the apps from the cache
                geoApiManager.features(poiId, latitude, longitude) { response ->
                    response.onSuccess { geoAPIFeatures ->
                        val isPoiInRange = geoAPIFeatures.firstOrNull { it.id == poiId }?.let {
                            val currentLocation = LatLng(latitude, longitude)
                            val polygons = when (it.geometry) {
                                is GeometryCollection -> it.geometry.geometries.filterIsInstance<Polygon>()
                                is Polygon -> listOf(it.geometry)
                                else -> emptyList()
                            }

                            polygons.map { polygon ->
                                polygon.coordinates.flatMap { ring ->
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
                            }.flatten().any { coordinate ->
                                currentLocation.distanceTo(coordinate) <= IS_POI_IN_RANGE_DISTANCE_THRESHOLD
                            }
                        } ?: false

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

    private suspend fun castLocationBasedApp(appUrl: String?, references: List<String>?): List<App> {
        appUrl ?: return emptyList()

        val manifest = loadManifest(appUrl) ?: return emptyList()
        val icons = manifest.icons
        val iconUrl = if (icons.isNullOrEmpty()) null else getIconPath(appUrl, icons)
        val logo = if (iconUrl == null) null else loadIcon(iconUrl)

        return uriUtil
            .getStartUrls(appUrl, appUrl, manifest.sdkStartUrl, references)
            .map {
                App(
                    name = manifest.name ?: "",
                    shortName = manifest.shortName ?: "",
                    description = manifest.description,
                    url = it.value,
                    logo = logo,
                    iconBackgroundColor = manifest.backgroundColor,
                    textColor = manifest.textColor,
                    textBackgroundColor = manifest.themeColor,
                    display = manifest.display,
                    poiId = it.key
                )
            }
    }

    private suspend fun loadManifest(appUrl: String): AppManifest? {
        return try {
            suspendCancellableCoroutine { continuation ->
                cache.getManifest(context, appUrl) { result ->
                    result.onSuccess { continuation.resumeIfActive(it) }
                    result.onFailure { continuation.resumeWithExceptionIfActive(it) }
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to download the manifest")
            null
        }
    }

    private suspend fun loadIcon(iconUrl: String): Bitmap? {
        return try {
            suspendCoroutineWithTimeout(2000) { continuation ->
                cache.getUri(context, iconUrl) { result ->
                    result.onSuccess { continuation.resumeIfActive(BitmapFactory.decodeByteArray(it, 0, it.size)) }
                    result.onFailure { continuation.resumeWithExceptionIfActive(it) }
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to download the icon")
            null
        }
    }

    private fun getIconPath(url: String, icons: Array<AppManifest.AppIcons>): String? {
        val buttonWidth = context.resources.getDimension(R.dimen.app_drawer_height).dp.toDouble()
        val preferredIcon = IconUtils.getBestMatchingIcon(buttonWidth, icons) ?: return null

        return uriUtil.buildUrl(url, preferredIcon.src)
    }

    companion object {
        private const val IS_POI_IN_RANGE_DISTANCE_THRESHOLD = 500 // meters
    }
}
