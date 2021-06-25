/*
 * PLEASE DO NOT EDIT!
 *
 * Generated by SwagGen with Kotlin template.
 * https://github.com/pace/SwagGen
 */

package cloud.pace.sdk.api.poi.generated.request.subscriptions

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
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.*
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit

object StoreSubscriptionAPI {

    interface StoreSubscriptionService {
        /* Stores a POI subscription
 */
        /* Stores a POI subscription to send a push notification to the device with the specified `pushToken` once one of the pois change based on the change condition. The notification contains (max 4kb)
```
{
  "target": "..."
  "subscription": "706087b4-8bca-4db9-b037-8a7ff4ce5633",
  "poi": {
    "id": "4d6dd9db-b0ac-40e8-a099-b606cace6f72", # poi ID
    "type": "gasStation",
    "attributes": {
      # ... more data of the type
    }
  }
}
```
 */
        @PUT("subscriptions/{id}")
        fun storeSubscription(
            @retrofit2.http.Body body: Body
        ): Call<Subscription>
    }

    /* Stores a POI subscription to send a push notification to the device with the specified `pushToken` once one of the pois change based on the change condition. The notification contains (max 4kb)
    ```
    {
      "target": "..."
      "subscription": "706087b4-8bca-4db9-b037-8a7ff4ce5633",
      "poi": {
        "id": "4d6dd9db-b0ac-40e8-a099-b606cace6f72", # poi ID
        "type": "gasStation",
        "attributes": {
          # ... more data of the type
        }
      }
    }
    ```
     */
    class Body {

        var data: SubscriptionBody? = null
    }

    fun POIAPI.SubscriptionsAPI.storeSubscription(body: Body, readTimeout: Long? = null): Call<Subscription> {
        val service: StoreSubscriptionService =
            Retrofit.Builder()
                .client(OkHttpClient.Builder()
                    .addNetworkInterceptor(InterceptorUtils.getInterceptor("application/vnd.api+json", "application/vnd.api+json", true))
                    .authenticator(InterceptorUtils.getAuthenticator())
                    .readTimeout(readTimeout ?: 10L, TimeUnit.SECONDS)
                    .build()
                )
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
                .create(StoreSubscriptionService::class.java)    

        return service.storeSubscription(body)
    }
}
