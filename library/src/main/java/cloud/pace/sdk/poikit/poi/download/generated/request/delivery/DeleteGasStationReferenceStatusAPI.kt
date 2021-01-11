/*
 * PLEASE DO NOT EDIT!
 *
 * Generated by SwagGen with Kotlin template.
 * https://github.com/pace/SwagGen
 */

package cloud.pace.sdk.api.request.delivery

import cloud.pace.sdk.api.API
import cloud.pace.sdk.api.model.*
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapters.Rfc3339DateJsonAdapter
import moe.banana.jsonapi2.JsonApiConverterFactory
import moe.banana.jsonapi2.ResourceAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.http.*
import java.util.*
import cloud.pace.sdk.api.API.toIso8601
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

interface DeleteGasStationReferenceStatusService {

    /** Deletes a reference status of a gas station **/
    @DELETE("/delivery/gas-stations/{gasStationId}/reference-status/{reference}")
    fun deleteGasStationReferenceStatus(
        @Path("gasStationId") gasStationId: String,
        @Path("reference") reference: String
    ): Call<Void>
}


private val service: DeleteGasStationReferenceStatusService by lazy {
    Retrofit.Builder()
        .client(OkHttpClient.Builder().addInterceptor {
            val builder = it.request()
                .newBuilder()
                .header("API-Key", API.apiKey)

            API.additionalHeaders.forEach { header ->
                builder.header(header.key, header.value)
            }

            it.proceed(builder.build())
        }.build())
        .baseUrl(API.baseUrl)
            .addConverterFactory(
                JsonApiConverterFactory.create(
                    Moshi.Builder().add(
                        ResourceAdapterFactory.builder()
                            .build()
                    )
                        .add(KotlinJsonAdapterFactory())
                        .add(Date::class.java, Rfc3339DateJsonAdapter().nullSafe())
                        .build()
                )
            )
        .build()
        .create(DeleteGasStationReferenceStatusService::class.java)
}

fun API.DeliveryAPI.deleteGasStationReferenceStatus(gasStationId: String, reference: String): Call<Void> {
    return service.deleteGasStationReferenceStatus(gasStationId, reference)
}

