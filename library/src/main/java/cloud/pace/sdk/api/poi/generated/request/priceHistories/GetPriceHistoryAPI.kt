/*
 * PLEASE DO NOT EDIT!
 *
 * Generated by SwagGen with Kotlin template.
 * https://github.com/pace/SwagGen
 */

package cloud.pace.sdk.api.poi.generated.request.priceHistories

import cloud.pace.sdk.api.poi.POIAPI
import cloud.pace.sdk.api.poi.generated.model.*
import cloud.pace.sdk.api.utils.EnumConverterFactory
import cloud.pace.sdk.api.utils.InterceptorUtils
import cloud.pace.sdk.utils.toIso8601
import com.google.gson.annotations.SerializedName
import com.squareup.moshi.Json
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapters.Rfc3339DateJsonAdapter
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import moe.banana.jsonapi2.JsonApi
import moe.banana.jsonapi2.JsonApiConverterFactory
import moe.banana.jsonapi2.Resource
import moe.banana.jsonapi2.ResourceAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.http.*
import java.io.File
import java.util.*

object GetPriceHistoryAPI {

    interface GetPriceHistoryService {
        /* Get price history for a specific gas station */
        /* Get the price history for a specific gas station and fuel type on a period of time which can begin no sooner than 37 days ago; the time interval between price changes can be set to minute, hour, day, week, month or year
 */
        @GET("gas-stations/{id}/fuel-price-histories/{fuel_type}")
        fun getPriceHistory(
            /** Gas station ID */
            @Path("id") id: String,
            /** Filter after a specific fuel type */
            @Path("fuel_type") fuelType: Fuel? = null,
            /** Filters data from the given point in time */
            @Query("filter[from]") filterfrom: String? = null,
            /** Filters data to the given point in time */
            @Query("filter[to]") filterto: String? = null,
            /** Base time interval between price changes */
            @Query("filter[granularity]") filtergranularity: String? = null
        ): Call<PriceHistory>
    }

    private val service: GetPriceHistoryService by lazy {
        Retrofit.Builder()
            .client(OkHttpClient.Builder().addInterceptor(InterceptorUtils.getInterceptor("application/json", "application/json")).build())
            .baseUrl(POIAPI.baseUrl)
            .addConverterFactory(EnumConverterFactory())
            .addConverterFactory(
                JsonApiConverterFactory.create(
                    Moshi.Builder()
                        .add(ResourceAdapterFactory.builder()
                            .build()
                        )
                        .add(Date::class.java, Rfc3339DateJsonAdapter().nullSafe())
                        .add(KotlinJsonAdapterFactory())
                        .build()
                )
            )
            .build()
            .create(GetPriceHistoryService::class.java)
    }

    fun POIAPI.PriceHistoriesAPI.getPriceHistory(id: String, fuelType: Fuel? = null, filterfrom: Date? = null, filterto: Date? = null, filtergranularity: String? = null) =
        service.getPriceHistory(id, fuelType, filterfrom?.toIso8601()?.dropLast(9)?.let { it +'Z'} , filterto?.toIso8601()?.dropLast(9)?.let { it +'Z'} , filtergranularity)
}
