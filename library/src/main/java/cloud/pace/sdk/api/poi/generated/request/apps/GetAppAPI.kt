/*
 * PLEASE DO NOT EDIT!
 *
 * Generated by SwagGen with Kotlin template.
 * https://github.com/pace/SwagGen
 */

package cloud.pace.sdk.api.poi.generated.request.apps

import cloud.pace.sdk.api.poi.POIAPI
import cloud.pace.sdk.api.poi.generated.model.LocationBasedApp
import cloud.pace.sdk.api.request.BaseRequest
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.HeaderMap
import retrofit2.http.Path

object GetAppAPI {

    interface GetAppService {
        /* Returns App with specified id */
        /* Returns App with specified id.
In case the query returns a `404` (`Not Found`) the app was deleted and should be deleted from any caches.
 */
        @GET("apps/{appID}")
        fun getApp(
            @HeaderMap headers: Map<String, String>,
            /* ID of the App */
            @Path("appID") appID: String
        ): Call<LocationBasedApp>
    }

    open class Request : BaseRequest() {

        fun getApp(
            appID: String,
            readTimeout: Long? = null,
            additionalHeaders: Map<String, String>? = null,
            additionalParameters: Map<String, String>? = null
        ): Call<LocationBasedApp> {
            val headers = headers(true, "application/vnd.api+json", "application/vnd.api+json", additionalHeaders)

            return retrofit(POIAPI.baseUrl, additionalParameters, readTimeout)
                .create(GetAppService::class.java)
                .getApp(
                    headers,
                    appID
                )
        }
    }

    fun POIAPI.AppsAPI.getApp(
        appID: String,
        readTimeout: Long? = null,
        additionalHeaders: Map<String, String>? = null,
        additionalParameters: Map<String, String>? = null
    ) = Request().getApp(
        appID,
        readTimeout,
        additionalHeaders,
        additionalParameters
    )
}
