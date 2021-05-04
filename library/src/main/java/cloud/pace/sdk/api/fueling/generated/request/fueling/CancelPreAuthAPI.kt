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
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.*
import java.io.File
import java.util.*

object CancelPreAuthAPI {

    interface CancelPreAuthService {
        /* Cancel a Pre Auth transaction */
        /* Cancel a Pre Auth transaction. This action is only permitted in case the user didn't already start the fueling process. Returns `403 Forbidden` in case the fueling already started.
 */
        @DELETE("gas-stations/{gasStationId}/transactions/{transactionId}")
        fun cancelPreAuth(
            /* Gas station ID */
            @Path("gasStationId") gasStationId: String,
            /* Transaction ID (for pre auth transactions). */
            @Path("transactionId") transactionId: String
        ): Call<Void>
    }

    private val service: CancelPreAuthService by lazy {
        Retrofit.Builder()
            .client(OkHttpClient.Builder().addNetworkInterceptor(InterceptorUtils.getInterceptor("application/vnd.api+json", "application/vnd.api+json")).build())
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
            .addConverterFactory(
                MoshiConverterFactory.create(
                    Moshi.Builder()
                        .add(Date::class.java, Rfc3339DateJsonAdapter().nullSafe())
                        .add(KotlinJsonAdapterFactory())
                        .build()
                )
            )
            .build()
            .create(CancelPreAuthService::class.java)
    }

    fun FuelingAPI.FuelingAPI.cancelPreAuth(gasStationId: String, transactionId: String) =
        service.cancelPreAuth(gasStationId, transactionId)
}
