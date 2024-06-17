/*
 * PLEASE DO NOT EDIT!
 *
 * Generated by SwagGen with Kotlin template.
 * https://github.com/pace/SwagGen
 */

package cloud.pace.sdk.api.pay.generated.request.paymentMethods

import cloud.pace.sdk.api.pay.PayAPI
import cloud.pace.sdk.api.pay.generated.model.PaymentMethod
import cloud.pace.sdk.api.pay.generated.model.PaymentMethodKind
import cloud.pace.sdk.api.pay.generated.model.PaymentMethodVendor
import cloud.pace.sdk.api.pay.generated.model.PaymentMethods
import cloud.pace.sdk.api.pay.generated.model.PaymentToken
import cloud.pace.sdk.api.request.BaseRequest
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.HeaderMap

object GetPaymentMethodsAPI {

    interface GetPaymentMethodsService {
        /* Get all payment methods for user */
        @GET("payment-methods")
        fun getPaymentMethods(
            @HeaderMap headers: Map<String, String>,
        ): Call<PaymentMethods>
    }

    open class Request : BaseRequest() {

        fun getPaymentMethods(
            readTimeout: Long? = null,
            additionalHeaders: Map<String, String>? = null,
            additionalParameters: Map<String, String>? = null
        ): Call<PaymentMethods> {
            val resources = listOf(PaymentMethod::class.java, PaymentMethodKind::class.java, PaymentMethodVendor::class.java, PaymentToken::class.java)
            val headers = headers(true, "application/vnd.api+json", "application/vnd.api+json", additionalHeaders)

            return retrofit(PayAPI.baseUrl, additionalParameters, readTimeout, resources)
                .create(GetPaymentMethodsService::class.java)
                .getPaymentMethods(
                    headers
                )
        }
    }

    fun PayAPI.PaymentMethodsAPI.getPaymentMethods(
        readTimeout: Long? = null,
        additionalHeaders: Map<String, String>? = null,
        additionalParameters: Map<String, String>? = null
    ) = Request().getPaymentMethods(
        readTimeout,
        additionalHeaders,
        additionalParameters
    )
}
