/*
 * PLEASE DO NOT EDIT!
 *
 * Generated by SwagGen with Kotlin template.
 * https://github.com/pace/SwagGen
 */

package cloud.pace.sdk.api.poi.generated.request.sources

import cloud.pace.sdk.api.API
import cloud.pace.sdk.api.poi.generated.model.*
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
import cloud.pace.sdk.poikit.utils.InterceptorUtils

interface UpdateSourceService {

    /** Updates source with specified id **/
    @PUT("sources/{sourceId}")
    fun updateSource(
        @Path("sourceId") sourceId: String? = null
    ): Call<Source>
}

private val service: UpdateSourceService by lazy {
    Retrofit.Builder()
        .client(OkHttpClient.Builder().addInterceptor(InterceptorUtils.getInterceptor("application/vnd.api+json", "application/vnd.api+json")).build())
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
        .create(UpdateSourceService::class.java)
}

fun API.SourcesAPI.updateSource(sourceId: String? = null): Call<Source> {
    return service.updateSource(sourceId)
}


