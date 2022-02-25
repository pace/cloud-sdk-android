/*
 * PLEASE DO NOT EDIT!
 *
 * Generated by SwagGen with Kotlin template.
 * https://github.com/pace/SwagGen
 */

package cloud.pace.sdk.api.poi.generated.request.apps

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
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.*
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit

object UpdateAppPOIsRelationshipsAPI {

    interface UpdateAppPOIsRelationshipsService {
        /* Update all POI relations for specified app id */
        /* Update all POI relations for specified app id */
        @PATCH("apps/{appID}/relationships/pois")
        fun updateAppPOIsRelationships(
            /* ID of the App */
            @Path("appID") appID: String? = null, 
            @retrofit2.http.Body body: AppPOIsRelationships
        ): Call<AppPOIsRelationships>
    }

    fun POIAPI.AppsAPI.updateAppPOIsRelationships(appID: String? = null, body: AppPOIsRelationships, readTimeout: Long? = null, additionalHeaders: Map<String, String>? = null): Call<AppPOIsRelationships> {
        val client = OkHttpClient.Builder()
                        .addNetworkInterceptor(InterceptorUtils.getInterceptor("application/vnd.api+json", "application/vnd.api+json", true, additionalHeaders))
                        .authenticator(InterceptorUtils.getAuthenticator())

        if (readTimeout != null) {
            client.readTimeout(readTimeout, TimeUnit.SECONDS)
        }

        val service: UpdateAppPOIsRelationshipsService =
            Retrofit.Builder()
                .client(client.build())
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
                .addConverterFactory(
                    MoshiConverterFactory.create(
                        Moshi.Builder()
                            .add(Date::class.java, Rfc3339DateJsonAdapter().nullSafe())
                            .add(KotlinJsonAdapterFactory())
                            .build()
                    )
                )
                .build()
                .create(UpdateAppPOIsRelationshipsService::class.java)

        return service.updateAppPOIsRelationships(appID, body)
    }
}
