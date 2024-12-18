/*
 * PLEASE DO NOT EDIT!
 *
 * Generated by SwagGen with Kotlin template.
 * https://github.com/pace/SwagGen
 */

package cloud.pace.sdk.api.pay.generated.request.fleetPaymentMethods

import cloud.pace.sdk.api.pay.PayAPI
import cloud.pace.sdk.api.pay.generated.model.FleetPaymentMethod
import cloud.pace.sdk.api.pay.generated.model.PaymentMethod
import cloud.pace.sdk.api.pay.generated.model.PaymentMethodKind
import cloud.pace.sdk.api.pay.generated.model.PaymentMethodVendor
import cloud.pace.sdk.api.pay.generated.model.PaymentToken
import cloud.pace.sdk.api.request.BaseRequest
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.HeaderMap
import retrofit2.http.Path

object GetFleetPaymentMethodAPI {

    interface GetFleetPaymentMethodService {
        /* Get a payment method */
        @GET("fleet/payment-methods/{paymentMethodId}")
        fun getFleetPaymentMethod(
            @HeaderMap headers: Map<String, String>,
            /* ID of the paymentMethod */
            @Path("paymentMethodId") paymentMethodId: String
        ): Call<FleetPaymentMethod>
    }

    open class Request : BaseRequest() {

        fun getFleetPaymentMethod(
            paymentMethodId: String,
            readTimeout: Long? = null,
            additionalHeaders: Map<String, String>? = null,
            additionalParameters: Map<String, String>? = null
        ): Call<FleetPaymentMethod> {
            val resources = listOf(PaymentMethod::class.java, PaymentMethodKind::class.java, PaymentMethodVendor::class.java, PaymentToken::class.java)
            val headers = headers(true, "application/vnd.api+json", "application/vnd.api+json", additionalHeaders)

            return retrofit(PayAPI.baseUrl, additionalParameters, readTimeout, resources)
                .create(GetFleetPaymentMethodService::class.java)
                .getFleetPaymentMethod(
                    headers,
                    paymentMethodId
                )
        }
    }

    fun PayAPI.FleetPaymentMethodsAPI.getFleetPaymentMethod(
        paymentMethodId: String,
        readTimeout: Long? = null,
        additionalHeaders: Map<String, String>? = null,
        additionalParameters: Map<String, String>? = null
    ) = Request().getFleetPaymentMethod(
        paymentMethodId,
        readTimeout,
        additionalHeaders,
        additionalParameters
    )
}
