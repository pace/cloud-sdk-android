/*
 * PLEASE DO NOT EDIT!
 *
 * Generated by SwagGen with Kotlin template.
 * https://github.com/pace/SwagGen
 */

package cloud.pace.sdk.api.poi.generated.request.priceHistories

import cloud.pace.sdk.api.poi.POIAPI
import cloud.pace.sdk.api.poi.generated.model.*
import cloud.pace.sdk.api.request.BaseRequest
import cloud.pace.sdk.utils.toIso8601
import retrofit2.Call
import retrofit2.http.*
import java.util.Date

object GetPriceHistoryAPI {

    interface GetPriceHistoryService {
        /* Get price history for a specific gas station */
        /* Get the price history for a specific gas station and fuel type on a period of time which can begin no sooner than 37 days ago; the time interval between price changes can be set to minute, hour, day, week, month or year
 */
        @GET("gas-stations/{id}/fuel-price-histories/{fuel_type}")
        fun getPriceHistory(
            @HeaderMap headers: Map<String, String>,
            /* Gas station ID */
            @Path("id") id: String,
            /* Filter after a specific fuel type */
            @Path("fuel_type") fuelType: Fuel? = null,
            /* Filters data from the given point in time */
            @Query("filter[from]") filterfrom: String? = null,
            /* Filters data to the given point in time */
            @Query("filter[to]") filterto: String? = null,
            /* Base time interval between price changes */
            @Query("filter[granularity]") filtergranularity: String? = null
        ): Call<PriceHistory>
    }

    open class Request : BaseRequest() {

        fun getPriceHistory(
            id: String,
            fuelType: Fuel? = null,
            filterfrom: Date? = null,
            filterto: Date? = null,
            filtergranularity: String? = null,
            readTimeout: Long? = null,
            additionalHeaders: Map<String, String>? = null,
            additionalParameters: Map<String, String>? = null
        ): Call<PriceHistory> {
            val headers = headers(true, "application/vnd.api+json", "application/vnd.api+json", additionalHeaders)

            return retrofit(POIAPI.baseUrl, additionalParameters, readTimeout)
                .create(GetPriceHistoryService::class.java)
                .getPriceHistory(
                    headers,
                    id,
                    fuelType,
                    filterfrom?.toIso8601()?.dropLast(9)?.let { it + 'Z' },
                    filterto?.toIso8601()?.dropLast(9)?.let { it + 'Z' },
                    filtergranularity
                )
        }
    }

    fun POIAPI.PriceHistoriesAPI.getPriceHistory(
        id: String,
        fuelType: Fuel? = null,
        filterfrom: Date? = null,
        filterto: Date? = null,
        filtergranularity: String? = null,
        readTimeout: Long? = null,
        additionalHeaders: Map<String, String>? = null,
        additionalParameters: Map<String, String>? = null
    ) = Request().getPriceHistory(
        id,
        fuelType,
        filterfrom,
        filterto,
        filtergranularity,
        readTimeout,
        additionalHeaders,
        additionalParameters
    )
}
