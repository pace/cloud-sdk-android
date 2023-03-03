/*
 * PLEASE DO NOT EDIT!
 *
 * Generated by SwagGen with Kotlin template.
 * https://github.com/pace/SwagGen
 */

package cloud.pace.sdk.api.poi.generated.request.sources

import cloud.pace.sdk.api.poi.POIAPI
import cloud.pace.sdk.api.poi.generated.model.*
import cloud.pace.sdk.api.request.BaseRequest
import retrofit2.Call
import retrofit2.http.*

object GetSourcesAPI {

    interface GetSourcesService {
        /* Returns a paginated list of sources */
        /* Returns a paginated list of sources optionally filtered by poi type and/or name */
        @GET("sources")
        fun getSources(
            @HeaderMap headers: Map<String, String>,
            /* page number */
            @Query("page[number]") pagenumber: Int? = null,
            /* items per page */
            @Query("page[size]") pagesize: Int? = null,
            /* Filter for poi type, no filter returns all types */
            @Query("filter[poiType]") filterpoiType: POIType? = null,
            /* Filter for all sources with given source name */
            @Query("filter[name]") filtername: String? = null
        ): Call<Sources>
    }

    open class Request : BaseRequest() {

        fun getSources(
            pagenumber: Int? = null,
            pagesize: Int? = null,
            filterpoiType: POIType? = null,
            filtername: String? = null,
            readTimeout: Long? = null,
            additionalHeaders: Map<String, String>? = null,
            additionalParameters: Map<String, String>? = null
        ): Call<Sources> {
            val headers = headers(true, "application/vnd.api+json", "application/vnd.api+json", additionalHeaders)

            return retrofit(POIAPI.baseUrl, additionalParameters, readTimeout)
                .create(GetSourcesService::class.java)
                .getSources(
                    headers,
                    pagenumber,
                    pagesize,
                    filterpoiType,
                    filtername
                )
        }
    }

    fun POIAPI.SourcesAPI.getSources(
        pagenumber: Int? = null,
        pagesize: Int? = null,
        filterpoiType: POIType? = null,
        filtername: String? = null,
        readTimeout: Long? = null,
        additionalHeaders: Map<String, String>? = null,
        additionalParameters: Map<String, String>? = null
    ) = Request().getSources(
        pagenumber,
        pagesize,
        filterpoiType,
        filtername,
        readTimeout,
        additionalHeaders,
        additionalParameters
    )
}
