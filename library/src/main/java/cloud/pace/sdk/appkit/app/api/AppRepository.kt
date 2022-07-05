package cloud.pace.sdk.appkit.app.api

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import cloud.pace.sdk.R
import cloud.pace.sdk.appkit.model.App
import cloud.pace.sdk.appkit.model.AppManifest
import cloud.pace.sdk.appkit.persistence.CacheModel
import cloud.pace.sdk.poikit.geo.GeoAPIManager
import cloud.pace.sdk.poikit.geo.GeoAPIManagerImpl
import cloud.pace.sdk.poikit.geo.GeoAPIManagerImpl.Companion.FUELING_TYPE
import cloud.pace.sdk.poikit.geo.GeoAPIManagerImpl.Companion.URL_KEY
import cloud.pace.sdk.utils.Completion
import cloud.pace.sdk.utils.Failure
import cloud.pace.sdk.utils.IconUtils
import cloud.pace.sdk.utils.Success
import cloud.pace.sdk.utils.URL
import cloud.pace.sdk.utils.dp
import cloud.pace.sdk.utils.resumeIfActive
import cloud.pace.sdk.utils.resumeWithExceptionIfActive
import cloud.pace.sdk.utils.suspendCoroutineWithTimeout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.suspendCancellableCoroutine
import timber.log.Timber

interface AppRepository {

    suspend fun getLocationBasedApps(latitude: Double, longitude: Double): Completion<List<App>>
    suspend fun getAllApps(): Completion<List<App>>
    suspend fun getAppsByUrl(url: String, references: List<String>): Completion<List<App>>
    suspend fun getUrlByAppId(appId: String): Completion<String?>
    fun getFuelingUrl(poiId: String, completion: (String) -> Unit)
}

class AppRepositoryImpl(
    private val context: Context,
    private val cache: CacheModel,
    private val appApi: AppAPI,
    private val uriUtil: UriManager,
    private val geoApiManager: GeoAPIManager
) : AppRepository {

    override suspend fun getLocationBasedApps(latitude: Double, longitude: Double): Completion<List<App>> {
        try {
            val apps = geoApiManager.apps(latitude, longitude).getOrElse {
                return Failure(it)
            }

            val deferred = apps.flatMap { geoGasStation ->
                geoGasStation.appUrls[FUELING_TYPE]?.map { url ->
                    CoroutineScope(Dispatchers.IO).async {
                        castLocationBasedApp(url, listOf(geoGasStation.id))
                    }
                } ?: emptyList()
            }

            return Success(deferred.awaitAll().flatten())
        } catch (e: Exception) {
            return Failure(e)
        }
    }

    override suspend fun getAllApps(): Completion<List<App>> {
        return try {
            val deferred = suspendCancellableCoroutine<Completion<List<Deferred<List<App>>>>> { continuation ->
                appApi.getAllApps { response ->
                    response.onSuccess { apps ->
                        val result = apps.map {
                            CoroutineScope(Dispatchers.IO).async {
                                castLocationBasedApp(it.pwaUrl, null)
                            }
                        }
                        continuation.resumeIfActive(Success(result))
                    }

                    response.onFailure { throwable ->
                        continuation.resumeIfActive(Failure(throwable))
                    }
                }
            }

            when (deferred) {
                is Success -> Success(deferred.result.awaitAll().flatten())
                is Failure -> Failure(deferred.throwable)
            }
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

    private suspend fun castLocationBasedApp(appUrl: String?, references: List<String>?): List<App> {
        appUrl ?: return emptyList()

        val manifest = loadManifest(appUrl)
        val icons = manifest?.icons
        val iconUrl = if (icons.isNullOrEmpty()) null else getIconPath(appUrl, icons)
        val logo = if (iconUrl == null) null else loadIcon(iconUrl)

        return uriUtil
            .getStartUrls(appUrl, references)
            .map {
                App(
                    name = manifest?.name ?: "",
                    shortName = manifest?.shortName ?: "",
                    description = manifest?.description,
                    url = it.value,
                    logo = logo,
                    iconBackgroundColor = manifest?.backgroundColor,
                    textColor = manifest?.textColor,
                    textBackgroundColor = manifest?.themeColor,
                    display = manifest?.display,
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

        return uriUtil.appendPath(url, preferredIcon.src)
    }
}
