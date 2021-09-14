package cloud.pace.sdk.appkit.app.api

import cloud.pace.sdk.api.API
import cloud.pace.sdk.api.poi.POIAPI.apps
import cloud.pace.sdk.api.poi.generated.model.LocationBasedApp
import cloud.pace.sdk.api.poi.generated.model.LocationBasedApps
import cloud.pace.sdk.api.poi.generated.model.LocationBasedAppsWithRefs
import cloud.pace.sdk.api.poi.generated.request.apps.CheckForPaceAppAPI.checkForPaceApp
import cloud.pace.sdk.api.poi.generated.request.apps.GetAppAPI.getApp
import cloud.pace.sdk.api.poi.generated.request.apps.GetAppsAPI.getApps
import cloud.pace.sdk.poikit.geo.GeoAPIClient
import cloud.pace.sdk.poikit.geo.GeoAPIResponse
import cloud.pace.sdk.poikit.utils.ApiException
import cloud.pace.sdk.utils.enqueue
import retrofit2.Call

interface AppAPI {

    fun getGeoApiApps(completion: (Result<GeoAPIResponse>) -> Unit)
    fun getLocationBasedApps(latitude: Double, longitude: Double, completion: (Result<LocationBasedAppsWithRefs>) -> Unit)
    fun getAllApps(completion: (Result<LocationBasedApps>) -> Unit)
    fun getAppByAppId(appId: String, completion: (Result<LocationBasedApp>) -> Unit)
}

class AppAPIImpl(private val geoApiClient: GeoAPIClient) : AppAPI {

    override fun getGeoApiApps(completion: (Result<GeoAPIResponse>) -> Unit) {
        geoApiClient.getGeoApiApps(completion)
    }

    override fun getLocationBasedApps(latitude: Double, longitude: Double, completion: (Result<LocationBasedAppsWithRefs>) -> Unit) {
        API.apps.checkForPaceApp(latitude.toFloat(), longitude.toFloat()).executeWithRetry(completion)
    }

    override fun getAllApps(completion: (Result<LocationBasedApps>) -> Unit) {
        API.apps.getApps().executeWithRetry(completion)
    }

    override fun getAppByAppId(appId: String, completion: (Result<LocationBasedApp>) -> Unit) {
        API.apps.getApp(appId).executeWithRetry(completion)
    }

    companion object {

        private const val MAX_RETRIES = 3

        private fun <T> Call<T>.executeWithRetry(completion: (Result<T>) -> Unit, retryCount: Int = 0) {
            enqueue {
                onResponse = {
                    val body = it.body()
                    if (it.isSuccessful && body != null) {
                        completion(Result.success(body))
                    } else {
                        completion(Result.failure(ApiException(it.code(), it.message())))
                    }
                }

                onFailure = {
                    val newRetryCount = retryCount + 1
                    if (newRetryCount < MAX_RETRIES) {
                        clone().executeWithRetry(completion, newRetryCount)
                    } else {
                        completion(Result.failure(it ?: Exception("Unknown exception")))
                    }
                }
            }
        }
    }
}
