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
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.*
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit

object GetPumpsAPI {

    interface GetPumpsService {
        /* Return pump information on all pumps of the gas station */
        /* Returns the current pump status (free, inUse, readyToPay, outOfOrder, locked) and identifier. If the status is readyToPay, the result also contains fuelType, productName, fuelAmount, VAT (amount & rate), priceWithoutVAT, priceIncludingVAT, currency.
<br><br>
Only use after approaching, otherwise returns `403 Forbidden`.
 */
        @GET("gas-stations/{gasStationId}/pumps")
        fun getPumps(
            /* Gas station ID */
            @Path("gasStationId") gasStationId: String
        ): Call<GetPumpsResponse>
    }

    fun FuelingAPI.FuelingAPI.getPumps(gasStationId: String, readTimeout: Long? = null): Call<GetPumpsResponse> {
        val client = OkHttpClient.Builder()
                        .addNetworkInterceptor(InterceptorUtils.getInterceptor("application/vnd.api+json", "application/vnd.api+json", true))
                        .authenticator(InterceptorUtils.getAuthenticator())

        if (readTimeout != null) {
            client.readTimeout(readTimeout, TimeUnit.SECONDS)
        }

        val service: GetPumpsService =
            Retrofit.Builder()
                .client(client.build())
                .baseUrl(FuelingAPI.baseUrl)
                .addConverterFactory(EnumConverterFactory())
                .addConverterFactory(
                    JsonApiConverterFactory.create(
                        Moshi.Builder()
                            .add(ResourceAdapterFactory.builder()
                                .add(Pump::class.java)
                                .build()
                            )
                            .add(Date::class.java, Rfc3339DateJsonAdapter().nullSafe())
                            .add(KotlinJsonAdapterFactory())
                            .build()
                    )
                )
                .addConverterFactory(
                    MoshiConverterFactory.create(
                        Moshi.Builder()
                            .add(Date::class.java, Rfc3339DateJsonAdapter().nullSafe())
                            .add(KotlinJsonAdapterFactory())
                            .build()
                    )
                )
                .build()
                .create(GetPumpsService::class.java)

        return service.getPumps(gasStationId)
    }
}
