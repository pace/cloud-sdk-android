/*
 * PLEASE DO NOT EDIT!
 *
 * Generated by SwagGen with Kotlin template.
 * https://github.com/pace/SwagGen
 */

package cloud.pace.sdk.api.fueling.generated.request.notification

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
import java.util.concurrent.TimeUnit

object CreateNotificationAPI {

    interface CreateNotificationService {
        /* Deliver notification
 */
        /* Delivers a notification from a partner system to the PACE fueling system.
  * `406` is returned in case the vendor format is not unknown
  * `400` is returned in case the vendor format is not valid when comparing against the provided input (request body)
 */
        @POST("notifications")
        fun createNotification(
            /* Name of the vendor providing the notification. This will enforce a certain format depending on the vendor. */
            @Query("vendor") vendor: String? = null
        ): Call<Void>
    }

    fun FuelingAPI.NotificationAPI.createNotification(vendor: String? = null, readTimeout: Long? = null): Call<Void> {
        val client = OkHttpClient.Builder()
                        .addNetworkInterceptor(InterceptorUtils.getInterceptor("application/json", "application/json", true))
                        .authenticator(InterceptorUtils.getAuthenticator())

        if (readTimeout != null) {
            client.readTimeout(readTimeout, TimeUnit.SECONDS)
        }

        val service: CreateNotificationService =
            Retrofit.Builder()
                .client(client.build())
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
                .create(CreateNotificationService::class.java)    

        return service.createNotification(vendor)
    }
}
