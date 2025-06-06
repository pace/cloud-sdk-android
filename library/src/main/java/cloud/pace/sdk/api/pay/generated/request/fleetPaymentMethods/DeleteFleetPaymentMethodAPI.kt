/*
 * PLEASE DO NOT EDIT!
 *
 * Generated by SwagGen with Kotlin template.
 * https://github.com/pace/SwagGen
 */

package cloud.pace.sdk.api.pay.generated.request.fleetPaymentMethods

import cloud.pace.sdk.api.pay.PayAPI
import cloud.pace.sdk.api.request.BaseRequest
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.DELETE
import retrofit2.http.HeaderMap
import retrofit2.http.Path
import retrofit2.http.Query

object DeleteFleetPaymentMethodAPI {

    interface DeleteFleetPaymentMethodService {
        /* Delete a payment method */
        @DELETE("fleet/payment-methods/{paymentMethodId}")
        fun deleteFleetPaymentMethod(
            @HeaderMap headers: Map<String, String>,
            /* ID of the paymentMethod */
            @Path("paymentMethodId") paymentMethodId: String,
            /* ID of the user that is required when user ID is not present in the authorization token. */
            @Query("userId") userId: String
        ): Call<ResponseBody>
    }

    open class Request : BaseRequest() {

        fun deleteFleetPaymentMethod(
            paymentMethodId: String,
            userId: String,
            readTimeout: Long? = null,
            additionalHeaders: Map<String, String>? = null,
            additionalParameters: Map<String, String>? = null
        ): Call<ResponseBody> {
            val headers = headers(true, "application/json", "application/json", additionalHeaders)

            return retrofit(PayAPI.baseUrl, additionalParameters, readTimeout)
                .create(DeleteFleetPaymentMethodService::class.java)
                .deleteFleetPaymentMethod(
                    headers,
                    paymentMethodId,
                    userId
                )
        }
    }

    fun PayAPI.FleetPaymentMethodsAPI.deleteFleetPaymentMethod(
        paymentMethodId: String,
        userId: String,
        readTimeout: Long? = null,
        additionalHeaders: Map<String, String>? = null,
        additionalParameters: Map<String, String>? = null
    ) = Request().deleteFleetPaymentMethod(
        paymentMethodId,
        userId,
        readTimeout,
        additionalHeaders,
        additionalParameters
    )
}
