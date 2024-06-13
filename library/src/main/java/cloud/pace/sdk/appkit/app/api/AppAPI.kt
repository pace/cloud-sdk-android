package cloud.pace.sdk.appkit.app.api

import cloud.pace.sdk.api.API
import cloud.pace.sdk.api.poi.POIAPI.apps
import cloud.pace.sdk.api.poi.generated.model.LocationBasedApp
import cloud.pace.sdk.api.poi.generated.request.apps.GetAppAPI.getApp
import cloud.pace.sdk.poikit.geo.GeoAPIClient
import cloud.pace.sdk.poikit.geo.GeoAPIResponse
import cloud.pace.sdk.poikit.utils.ApiException
import cloud.pace.sdk.poikit.utils.ControlledRunner
import cloud.pace.sdk.utils.enqueue
import cloud.pace.sdk.utils.requestId
import retrofit2.Call
import timber.log.Timber

interface AppAPI {

    suspend fun getGeoApiApps(): Result<GeoAPIResponse>
    fun getAppByAppId(appId: String, completion: (Result<LocationBasedApp>) -> Unit)
}

class AppAPIImpl(private val geoApiClient: GeoAPIClient) : AppAPI {

    private val controlledRunner: ControlledRunner<Result<GeoAPIResponse>> by lazy { ControlledRunner() }

    override suspend fun getGeoApiApps(): Result<GeoAPIResponse> {
        return try {
            controlledRunner.joinPreviousOrRun {
                val response = geoApiClient.getGeoApiApps()
                val body = response.body()

                if (response.isSuccessful && body != null) {
                    Result.success(body)
                } else {
                    Result.failure(ApiException(response.code(), response.message(), response.requestId))
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "GeoJson request failed")
            Result.failure(e)
        }
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
                        completion(Result.failure(ApiException(it.code(), it.message(), it.requestId)))
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
