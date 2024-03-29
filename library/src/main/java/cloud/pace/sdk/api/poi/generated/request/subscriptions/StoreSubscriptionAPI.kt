/*
 * PLEASE DO NOT EDIT!
 *
 * Generated by SwagGen with Kotlin template.
 * https://github.com/pace/SwagGen
 */

package cloud.pace.sdk.api.poi.generated.request.subscriptions

import cloud.pace.sdk.api.poi.POIAPI
import cloud.pace.sdk.api.poi.generated.model.*
import cloud.pace.sdk.api.request.BaseRequest
import retrofit2.Call
import retrofit2.http.*

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
            @HeaderMap headers: Map<String, String>,
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

    open class Request : BaseRequest() {

        fun storeSubscription(
            body: Body,
            readTimeout: Long? = null,
            additionalHeaders: Map<String, String>? = null,
            additionalParameters: Map<String, String>? = null
        ): Call<Subscription> {
            val headers = headers(true, "application/vnd.api+json", "application/vnd.api+json", additionalHeaders)

            return retrofit(POIAPI.baseUrl, additionalParameters, readTimeout)
                .create(StoreSubscriptionService::class.java)
                .storeSubscription(
                    headers,
                    body
                )
        }
    }

    fun POIAPI.SubscriptionsAPI.storeSubscription(
        body: Body,
        readTimeout: Long? = null,
        additionalHeaders: Map<String, String>? = null,
        additionalParameters: Map<String, String>? = null
    ) = Request().storeSubscription(
        body,
        readTimeout,
        additionalHeaders,
        additionalParameters
    )
}
