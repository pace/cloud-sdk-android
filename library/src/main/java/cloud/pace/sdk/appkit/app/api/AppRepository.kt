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
import kotlinx.coroutines.suspendCancellableCoroutine
import timber.log.Timber
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

interface AppRepository {

    fun getLocationBasedApps(context: Context, latitude: Double, longitude: Double, completion: (Result<List<App>>) -> Unit)
    fun getAllApps(context: Context, completion: (Result<List<App>>) -> Unit)
    fun getAppsByUrl(context: Context, url: String, references: List<String>, completion: (Result<List<App>>) -> Unit)
    fun getUrlByAppId(appId: String, completion: (Result<String?>) -> Unit)
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

    override fun getLocationBasedApps(context: Context, latitude: Double, longitude: Double, completion: (Result<List<App>>) -> Unit) {
        // Try to load the apps from the cache
        geoApiManager.apps(latitude, longitude) { response ->
            response.onSuccess {
                val apps = it
                    .flatMap { geoGasStation ->
                        geoGasStation.appUrls.mapNotNull { url ->
                            castLocationBasedApp(context, url, listOf(geoGasStation.id))
                        }
                    }.flatten()

                completion(Result.success(apps))
            }

            response.onFailure {
                // Fetch the apps from the API
                appApi.getLocationBasedApps(latitude, longitude) { response ->
                    response.onSuccess { apps ->
                        completion(Result.success(apps.mapNotNull { castLocationBasedApp(context, it.pwaUrl, it.references) }.flatten()))
                    }

                    response.onFailure { throwable ->
                        completion(Result.failure(throwable))
                    }
                }
            }
        }
    }

    override fun getAllApps(context: Context, completion: (Result<List<App>>) -> Unit) {
        appApi.getAllApps { response ->
            response.onSuccess { apps ->
                completion(Result.success(apps.mapNotNull { castLocationBasedApp(context, it.pwaUrl, null) }.flatten()))
            }

            response.onFailure { throwable ->
                completion(Result.failure(throwable))
            }
        }
    }

    override fun getAppsByUrl(context: Context, url: String, references: List<String>, completion: (Result<List<App>>) -> Unit) {
        val apps = castLocationBasedApp(context, url, references)
        if (apps != null) {
            completion(Result.success(apps))
        } else {
            completion(Result.failure(Exception("Could not load Apps for URL $url")))
        }
    }

    override fun getUrlByAppId(appId: String, completion: (Result<String?>) -> Unit) {
        appApi.getAppByAppId(appId) { response ->
            response.onSuccess { app ->
                completion(Result.success(app.pwaUrl))
            }

            response.onFailure { throwable ->
                completion(Result.failure(throwable))
            }
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

    private fun castLocationBasedApp(context: Context, appUrl: String?, references: List<String>?): List<App>? {
        appUrl ?: return null

        val manifestFuture = CompletableFutureCompat<AppManifest?>()
        cache.getManifest(context, appUrl) { result ->
            result.onSuccess { manifestFuture.complete(it) }
            result.onFailure {
                Timber.e(it, "Failed to download the manifest")
                manifestFuture.complete(null)
            }
        }

        val manifest = manifestFuture.get() ?: return null

        val icons = manifest.icons
        val iconUrl = if (icons.isNullOrEmpty()) null else getIconPath(appUrl, icons)

        val logo: Bitmap? = if (iconUrl == null) {
            null
        } else {
            val iconFuture = CompletableFutureCompat<Bitmap?>()
            cache.getUri(context, iconUrl) { result ->
                result.onSuccess { iconFuture.complete(BitmapFactory.decodeByteArray(it, 0, it.size)) }
                result.onFailure {
                    Timber.e(it, "Failed to download the icon")
                    iconFuture.complete(null)
                }
            }
            try {
                iconFuture.get(2, TimeUnit.SECONDS)
            } catch (e: TimeoutException) {
                null
            }
        }

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

    private fun getIconPath(url: String, icons: Array<AppManifest.AppIcons>): String? {
        val buttonWidth = context.resources.getDimension(R.dimen.app_drawer_height).dp.toDouble()
        val preferredIcon = IconUtils.getBestMatchingIcon(buttonWidth, icons) ?: return null

        return uriUtil.buildUrl(url, preferredIcon.src)
    }

    companion object {
        private const val IS_POI_IN_RANGE_DISTANCE_THRESHOLD = 500 // meters
    }
}
