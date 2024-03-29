/*
 * PLEASE DO NOT EDIT!
 *
 * Generated by SwagGen with Kotlin template.
 * https://github.com/pace/SwagGen
 */

package cloud.pace.sdk.api.poi.generated.request.apps

import cloud.pace.sdk.api.poi.POIAPI
import cloud.pace.sdk.api.poi.generated.model.*
import cloud.pace.sdk.api.request.BaseRequest
import retrofit2.Call
import retrofit2.http.*

object UpdateAppPOIsRelationshipsAPI {

    interface UpdateAppPOIsRelationshipsService {
        /* Update all POI relations for specified app id */
        /* Update all POI relations for specified app id */
        @PATCH("apps/{appID}/relationships/pois")
        fun updateAppPOIsRelationships(
            @HeaderMap headers: Map<String, String>,
            /* ID of the App */
            @Path("appID") appID: String? = null,
            @retrofit2.http.Body body: List<AppPOIsRelationships>
        ): Call<List<AppPOIsRelationships>>
    }

    open class Request : BaseRequest() {

        fun updateAppPOIsRelationships(
            appID: String? = null,
            body: List<AppPOIsRelationships>,
            readTimeout: Long? = null,
            additionalHeaders: Map<String, String>? = null,
            additionalParameters: Map<String, String>? = null
        ): Call<List<AppPOIsRelationships>> {
            val headers = headers(true, "application/vnd.api+json", "application/vnd.api+json", additionalHeaders)

            return retrofit(POIAPI.baseUrl, additionalParameters, readTimeout)
                .create(UpdateAppPOIsRelationshipsService::class.java)
                .updateAppPOIsRelationships(
                    headers,
                    appID,
                    body
                )
        }
    }

    fun POIAPI.AppsAPI.updateAppPOIsRelationships(
        appID: String? = null,
        body: List<AppPOIsRelationships>,
        readTimeout: Long? = null,
        additionalHeaders: Map<String, String>? = null,
        additionalParameters: Map<String, String>? = null
    ) = Request().updateAppPOIsRelationships(
        appID,
        body,
        readTimeout,
        additionalHeaders,
        additionalParameters
    )
}
