/*
 * PLEASE DO NOT EDIT!
 *
 * Generated by SwagGen with Kotlin template.
 * https://github.com/pace/SwagGen
 */

package cloud.pace.sdk.api.poi.generated.request.events

import cloud.pace.sdk.api.poi.POIAPI
import cloud.pace.sdk.api.poi.generated.model.*
import cloud.pace.sdk.api.request.BaseRequest
import retrofit2.Call
import retrofit2.http.*

object GetEventsAPI {

    interface GetEventsService {
        /* Returns a list of events */
        /* Returns a list of events optionally filtered by poi type and/or country id and/or user id */
        @GET("events")
        fun getEvents(
            @HeaderMap headers: Map<String, String>,
            /* page number */
            @Query("page[number]") pagenumber: Int? = null,
            /* items per page */
            @Query("page[size]") pagesize: Int? = null,
            /* Filter for all events from given source id */
            @Query("filter[sourceId]") filtersourceId: String? = null,
            /* Filter for all events for the changes made by a given user */
            @Query("filter[userId]") filteruserId: String? = null
        ): Call<Events>
    }

    open class Request : BaseRequest() {

        fun getEvents(
            pagenumber: Int? = null,
            pagesize: Int? = null,
            filtersourceId: String? = null,
            filteruserId: String? = null,
            readTimeout: Long? = null,
            additionalHeaders: Map<String, String>? = null,
            additionalParameters: Map<String, String>? = null
        ): Call<Events> {
            val headers = headers(true, "application/vnd.api+json", "application/vnd.api+json", additionalHeaders)

            return retrofit(POIAPI.baseUrl, additionalParameters, readTimeout)
                .create(GetEventsService::class.java)
                .getEvents(
                    headers,
                    pagenumber,
                    pagesize,
                    filtersourceId,
                    filteruserId
                )
        }
    }

    fun POIAPI.EventsAPI.getEvents(
        pagenumber: Int? = null,
        pagesize: Int? = null,
        filtersourceId: String? = null,
        filteruserId: String? = null,
        readTimeout: Long? = null,
        additionalHeaders: Map<String, String>? = null,
        additionalParameters: Map<String, String>? = null
    ) = Request().getEvents(
        pagenumber,
        pagesize,
        filtersourceId,
        filteruserId,
        readTimeout,
        additionalHeaders,
        additionalParameters
    )
}
