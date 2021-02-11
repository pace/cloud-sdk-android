/*
 * PLEASE DO NOT EDIT!
 *
 * Generated by SwagGen with Kotlin template.
 * https://github.com/pace/SwagGen
 */

package cloud.pace.sdk.api.fueling.generated.request.fueling

import cloud.pace.sdk.api.fueling.FuelingAPI
import cloud.pace.sdk.api.fueling.generated.model.*
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

object ApproachingAtTheForecourtAPI {

    interface ApproachingAtTheForecourtService {
        /* Gather information when approaching at the forecourt
 */
        /* This request will:
* Return a list of paymentMethods of the user which can be used at the
  gas station.
* Return up-to-date price information (price structure) at the gas
  station.
* Return a list of pumps available at the gas station together with the
  current status (free, inUse, readyToPay, outOfOrder). No pumps might
  be returned if the list of payment methods is empty.
* Create payment tokens for the paymentMethods of the user that are also
  supported at the gas station and pre-authorize the calculated maximum
  amount of money (background task).
The approaching is a necessary first api call for connected fueling. Without a valid approaching the [get pump](#operation/GetPump) and [wait for status change](#operation/WaitOnPumpStatusChange) calls may be answered with a `403 Forbidden` status code. An approaching is valid for one fueling only and can't be reused. If a long (not further disclosed time) has passed, the approaching is also invalidated. So if the client is receiving a `403 Forbidden` on the above mentioned calls, a new approaching has to be issued, this can be done transparent to the user.
 */
        @POST("gas-stations/{gasStationId}/approaching")
        fun approachingAtTheForecourt(
            /** Gas station ID */
            @Path("gasStationId") gasStationId: String,
            /** Reduces the opening hours rules. After compilation, only rules with the action open will remain in the response. */
            @Query("compile[openingHours]") compileopeningHours: Boolean? = null
        ): Call<ApproachingResponse>
    }

    private val service: ApproachingAtTheForecourtService by lazy {
        Retrofit.Builder()
            .client(OkHttpClient.Builder().addInterceptor(InterceptorUtils.getInterceptor("application/json", "application/json")).build())
            .baseUrl(FuelingAPI.baseUrl)
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
            .create(ApproachingAtTheForecourtService::class.java)
    }

    fun FuelingAPI.FuelingAPI.approachingAtTheForecourt(gasStationId: String, compileopeningHours: Boolean? = null) =
        service.approachingAtTheForecourt(gasStationId, compileopeningHours)
}