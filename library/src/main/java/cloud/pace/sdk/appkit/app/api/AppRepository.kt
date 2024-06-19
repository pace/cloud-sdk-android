package cloud.pace.sdk.appkit.app.api

import android.content.Context
import cloud.pace.sdk.R
import cloud.pace.sdk.appkit.model.App
import cloud.pace.sdk.appkit.model.AppIcon
import cloud.pace.sdk.poikit.geo.GeoAPIManager
import cloud.pace.sdk.poikit.geo.GeoAPIManagerImpl
import cloud.pace.sdk.poikit.geo.GeoAPIManagerImpl.Companion.FUELING_TYPE
import cloud.pace.sdk.poikit.geo.GeoAPIManagerImpl.Companion.URL_KEY
import cloud.pace.sdk.poikit.geo.GeoGasStation
import cloud.pace.sdk.poikit.utils.distanceTo
import cloud.pace.sdk.utils.Completion
import cloud.pace.sdk.utils.Failure
import cloud.pace.sdk.utils.IconUtils
import cloud.pace.sdk.utils.Success
import cloud.pace.sdk.utils.URL
import cloud.pace.sdk.utils.dp
import cloud.pace.sdk.utils.resumeIfActive
import cloud.pace.sdk.utils.resumeWithExceptionIfActive
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.async
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.suspendCancellableCoroutine
import timber.log.Timber

interface AppRepository {

    suspend fun getLocationBasedApps(latitude: Double, longitude: Double): Completion<List<App>>
    suspend fun getAppsByUrl(url: String, references: List<String>): Completion<List<App>>
    suspend fun getUrlByAppId(appId: String): Completion<String?>
    fun getFuelingUrl(poiId: String, completion: (String) -> Unit)
}

class AppRepositoryImpl(
    private val context: Context,
    private val appApi: AppAPI,
    private val uriUtil: UriManager,
    private val geoApiManager: GeoAPIManager,
    private val manifestClient: ManifestClient
) : AppRepository {

    override suspend fun getLocationBasedApps(latitude: Double, longitude: Double): Completion<List<App>> {
        val apps = geoApiManager.apps(latitude, longitude).getOrElse {
            return Failure(it)
        }

        val locationBasedApps = runCatching {
            supervisorScope {
                val userLocation = LatLng(latitude, longitude)

                apps
                    .map {
                        async { it.toLocationBasedApps(userLocation) }
                    }
                    .flatMap {
                        runCatching { it.await() }.getOrNull() ?: emptyList()
                    }
                    .sortedByDescending(App::distance)
            }
        }.getOrElse {
            Timber.e(it, "Failed to create location based apps with IDs: ${apps.map(GeoGasStation::id)}")
            return Failure(it)
        }

        return Success(locationBasedApps)
    }

    override suspend fun getAppsByUrl(url: String, references: List<String>): Completion<List<App>> {
        val apps = createLocationBasedApps(url, references)

        return if (apps.isNotEmpty()) {
            Success(apps)
        } else {
            Failure(Exception("Failed to create location based apps for URL: $url"))
        }
    }

    override suspend fun getUrlByAppId(appId: String): Completion<String?> {
        return try {
            val result = suspendCancellableCoroutine { continuation ->
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

    override fun getFuelingUrl(poiId: String, completion: (String) -> Unit) {
        geoApiManager.cofuGasStations { result ->
            result.onSuccess { stations ->
                val cofuGasStation = stations.find { station -> station.id == poiId }
                val apps = cofuGasStation?.properties?.get(GeoAPIManagerImpl.APPS_KEY) as? List<*>
                val fuelingApp = apps?.find { (it as? Map<*, *>)?.get(GeoAPIManagerImpl.TYPE_KEY) as? String == FUELING_TYPE } as? Map<*, *>
                val fuelingUrl = fuelingApp?.get(URL_KEY) as? String ?: URL.fueling

                completion(uriUtil.getStartUrl(fuelingUrl, poiId))
            }

            result.onFailure {
                completion(uriUtil.getStartUrl(URL.fueling, poiId))
            }
        }
    }

    private suspend fun GeoGasStation.toLocationBasedApps(userLocation: LatLng): List<App> {
        val references = listOf(id)
        val distance = coordinate?.let { userLocation.distanceTo(it).toInt() }

        return appUrls[FUELING_TYPE]?.map {
            createLocationBasedApps(it, references, distance)
        }?.flatten() ?: emptyList()
    }

    private suspend fun createLocationBasedApps(appUrl: String?, references: List<String>?, distance: Int? = null): List<App> {
        appUrl ?: return emptyList()

        val manifest = runCatching { manifestClient.getManifest(appUrl) }.getOrNull()
        val icons = manifest?.icons
        val iconUrl = if (icons.isNullOrEmpty()) null else getIconPath(appUrl, icons)

        return uriUtil
            .getStartUrls(appUrl, references)
            .map {
                App(
                    name = manifest?.name,
                    shortName = manifest?.shortName,
                    description = manifest?.description,
                    url = it.value,
                    iconUrl = iconUrl,
                    iconBackgroundColor = manifest?.backgroundColor,
                    textColor = manifest?.textColor,
                    textBackgroundColor = manifest?.themeColor,
                    display = manifest?.display,
                    poiId = it.key,
                    distance = distance,
                    brandUrl = appUrl
                )
            }
    }

    private fun getIconPath(url: String, icons: List<AppIcon>): String? {
        val buttonWidth = context.resources.getDimension(R.dimen.app_drawer_height).dp.toDouble()
        val preferredIconSrc = IconUtils.getBestMatchingIcon(buttonWidth, icons)?.src ?: return null

        return uriUtil.getIconUrl(url, preferredIconSrc)
    }
}
