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

object GetAppsAPI {

    interface GetAppsService {
        /* Returns a paginated list of apps */
        /* Returns a paginated list of apps optionally filtered by type and/or query.
 */
        @GET("apps")
        fun getApps(
            /* page number */
            @Query("page[number]") pagenumber: Int? = null,
            /* items per page */
            @Query("page[size]") pagesize: Int? = null,
            /* Filter for poi type, no filter returns all types */
            @Query("filter[appType]") filterappType: FilterappType? = null,
            /* Filters the location-based app by its caching method.
 */
            @Query("filter[cache]") filtercache: Filtercache? = null,
            /* Filters location-based apps that were changed (created/updated/deleted) since the given point in time */
            @Query("filter[since]") filtersince: String? = null
        ): Call<LocationBasedApps>
    }

    /* Filter for poi type, no filter returns all types */
    enum class FilterappType(val value: String) {
        @SerializedName("fueling")
        @Json(name = "fueling")
        FUELING("fueling")
    }

    /* Filters the location-based app by its caching method.
     */
    enum class Filtercache(val value: String) {
        @SerializedName("preload")
        @Json(name = "preload")
        PRELOAD("preload"),
        @SerializedName("approaching")
        @Json(name = "approaching")
        APPROACHING("approaching")
    }

    fun POIAPI.AppsAPI.getApps(pagenumber: Int? = null, pagesize: Int? = null, filterappType: FilterappType? = null, filtercache: Filtercache? = null, filtersince: Date? = null, readTimeout: Long? = null): Call<LocationBasedApps> {
        val client = OkHttpClient.Builder()
                        .addNetworkInterceptor(InterceptorUtils.getInterceptor("application/vnd.api+json", "application/vnd.api+json", true))
                        .authenticator(InterceptorUtils.getAuthenticator())

        if (readTimeout != null) {
            client.readTimeout(readTimeout, TimeUnit.SECONDS)
        }

        val service: GetAppsService =
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
                .create(GetAppsService::class.java)

        return service.getApps(pagenumber, pagesize, filterappType, filtercache, filtersince?.toIso8601()?.dropLast(9)?.let { it +'Z'} )
    }
}
