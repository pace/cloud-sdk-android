package cloud.pace.sdk.appkit.app.api

import car.pace.cloudsdk.api.CloudApi
import car.pace.cloudsdk.api.poi.LocationBasedApp
import cloud.pace.sdk.appkit.AppKit
import retrofit2.Call
import retrofit2.Response

interface AppCloudApi {

    fun getLocationBasedApps(latitude: Double, longitude: Double, retry: Boolean, completion: (Result<Array<LocationBasedApp>?>) -> Unit)
    fun getAllApps(retry: Boolean, completion: (Result<Array<LocationBasedApp>?>) -> Unit)
    fun getAppByAppId(appId: String, completion: (Result<LocationBasedApp?>) -> Unit)
}

class AppCloudApiImpl : AppCloudApi {

    override fun getLocationBasedApps(latitude: Double, longitude: Double, retry: Boolean, completion: (Result<Array<LocationBasedApp>?>) -> Unit) {
        APIRequestWrapper.execute(
            CloudApi.instance.withPoiAPI(null, AppKit.configuration.apiKey).getLocationBasedApps(
                latitude,
                longitude
            ),
            retry
        ) {
            it.onSuccess { response ->
                val apps = response?.body()
                completion(Result.success(apps))
            }

            it.onFailure { throwable ->
                completion(Result.failure(throwable))
            }
        }
    }

    override fun getAllApps(retry: Boolean, completion: (Result<Array<LocationBasedApp>?>) -> Unit) {
        APIRequestWrapper.execute(
            CloudApi.instance.withPoiAPI(null, AppKit.configuration.apiKey).getLocationBasedApps(""),
            retry
        ) {
            it.onSuccess { response ->
                val apps = response?.body()
                completion(Result.success(apps))
            }

            it.onFailure { throwable ->
                completion(Result.failure(throwable))
            }
        }
    }

    override fun getAppByAppId(appId: String, completion: (Result<LocationBasedApp?>) -> Unit) {
        APIRequestWrapper.execute(
            CloudApi.instance.withPoiAPI(null, AppKit.configuration.apiKey).getLocationBasedApp(appId),
            true
        ) {
            it.onSuccess { response ->
                val app = response?.body()
                completion(Result.success(app))
            }

            it.onFailure { throwable ->
                completion(Result.failure(throwable))
            }
        }
    }

    object APIRequestWrapper {
        private const val MAX_RETRIES = 3

        fun <T> execute(call: Call<T>, retry: Boolean, retryCount: Int = 0, completion: (Result<Response<T>?>) -> Unit) {
            CloudApi.instance.execute(call) { result ->
                result.onSuccess { completion(result) }
                result.onFailure {
                    val newRetryCount = retryCount + 1
                    if (retry && newRetryCount < MAX_RETRIES) {
                        execute(call.clone(), retry, newRetryCount, completion)
                    } else {
                        completion(result)
                    }
                }
            }
        }
    }
}
