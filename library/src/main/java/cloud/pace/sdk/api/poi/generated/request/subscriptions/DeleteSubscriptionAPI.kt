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
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

object DeleteSubscriptionAPI {

    interface DeleteSubscriptionService {
        /* Deletes a previously created POI subscription
 */
        @DELETE("subscriptions/{id}")
        fun deleteSubscription(
            @HeaderMap headers: Map<String, String>,
        ): Call<ResponseBody>
    }

    open class Request : BaseRequest() {

        fun deleteSubscription(
            readTimeout: Long? = null,
            additionalHeaders: Map<String, String>? = null,
            additionalParameters: Map<String, String>? = null
        ): Call<ResponseBody> {
            val headers = headers(true, "application/json", "application/json", additionalHeaders)

            return retrofit(POIAPI.baseUrl, additionalParameters, readTimeout)
                .create(DeleteSubscriptionService::class.java)
                .deleteSubscription(
                    headers
                )
        }
    }

    fun POIAPI.SubscriptionsAPI.deleteSubscription(
        readTimeout: Long? = null,
        additionalHeaders: Map<String, String>? = null,
        additionalParameters: Map<String, String>? = null
    ) = Request().deleteSubscription(
        readTimeout,
        additionalHeaders,
        additionalParameters
    )
}
