package cloud.pace.sdk.appkit.app.api

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import cloud.pace.sdk.R
import cloud.pace.sdk.appkit.model.App
import cloud.pace.sdk.appkit.model.AppManifest
import cloud.pace.sdk.appkit.persistence.CacheModel
import cloud.pace.sdk.utils.CompletableFutureCompat
import cloud.pace.sdk.utils.IconUtils
import cloud.pace.sdk.utils.dp
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

interface AppRepository {

    fun getLocationBasedApps(context: Context, latitude: Double, longitude: Double, completion: (Result<List<App>>) -> Unit)
    fun getAllApps(context: Context, completion: (Result<List<App>>) -> Unit)
    fun getAppsByUrl(context: Context, url: String, references: List<String>, completion: (Result<List<App>>) -> Unit)
    fun getUrlByAppId(appId: String, completion: (Result<String?>) -> Unit)
}

class AppRepositoryImpl(
    private val context: Context,
    private val cache: CacheModel,
    private val appCloudApi: AppCloudApi,
    private val uriUtil: UriManager
) : AppRepository {

    override fun getLocationBasedApps(context: Context, latitude: Double, longitude: Double, completion: (Result<List<App>>) -> Unit) {
        appCloudApi.getLocationBasedApps(latitude, longitude) { response ->
            response.onSuccess { apps ->
                completion(Result.success(apps.mapNotNull { castLocationBasedApp(context, it.pwaUrl, it.references) }.flatten()))
            }

            response.onFailure { throwable ->
                completion(Result.failure(throwable))
            }
        }
    }

    override fun getAllApps(context: Context, completion: (Result<List<App>>) -> Unit) {
        appCloudApi.getAllApps { response ->
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
        appCloudApi.getAppByAppId(appId) { response ->
            response.onSuccess { app ->
                completion(Result.success(app.pwaUrl))
            }

            response.onFailure { throwable ->
                completion(Result.failure(throwable))
            }
        }
    }

    private fun castLocationBasedApp(context: Context, appUrl: String?, references: List<String>?): List<App>? {
        appUrl ?: return null

        val manifestFuture = CompletableFutureCompat<AppManifest?>()
        cache.getManifest(context, appUrl) { result ->
            result.onSuccess { manifestFuture.complete(it) }
            result.onFailure { manifestFuture.complete(null) }
        }

        val manifest = manifestFuture.get() ?: return null

        val iconUrl = if (manifest.icons.isNullOrEmpty()) null else getIconPath(appUrl, manifest.icons)

        val logo: Bitmap? = if (iconUrl == null) {
            null
        } else {
            val iconFuture = CompletableFutureCompat<Bitmap?>()
            cache.getUri(context, iconUrl) { result ->
                result.onSuccess { iconFuture.complete(BitmapFactory.decodeByteArray(it, 0, it.size)) }
                result.onFailure { iconFuture.complete(null) }
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
                    name = manifest.name,
                    shortName = manifest.shortName,
                    description = manifest.description,
                    url = it.value,
                    logo = logo,
                    iconBackgroundColor = manifest.backgroundColor,
                    textColor = manifest.textColor,
                    textBackgroundColor = manifest.themeColor,
                    display = manifest.display,
                    gasStationId = it.key
                )
            }
    }

    private fun getIconPath(url: String, icons: Array<AppManifest.AppIcons>): String? {
        val buttonWidth = context.resources.getDimension(R.dimen.app_drawer_height).dp.toDouble()
        val preferredIcon = IconUtils.getBestMatchingIcon(buttonWidth, icons) ?: return null

        return uriUtil.buildUrl(url, preferredIcon.src)
    }
}
