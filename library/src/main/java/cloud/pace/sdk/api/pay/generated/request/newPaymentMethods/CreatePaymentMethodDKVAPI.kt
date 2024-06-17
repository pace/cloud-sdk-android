/*
 * PLEASE DO NOT EDIT!
 *
 * Generated by SwagGen with Kotlin template.
 * https://github.com/pace/SwagGen
 */

package cloud.pace.sdk.api.pay.generated.request.newPaymentMethods

import cloud.pace.sdk.api.pay.PayAPI
import cloud.pace.sdk.api.pay.generated.model.PaymentMethod
import cloud.pace.sdk.api.pay.generated.model.PaymentMethodDKVCreateBody
import cloud.pace.sdk.api.pay.generated.model.PaymentMethodKind
import cloud.pace.sdk.api.pay.generated.model.PaymentMethodVendor
import cloud.pace.sdk.api.pay.generated.model.PaymentToken
import cloud.pace.sdk.api.request.BaseRequest
import retrofit2.Call
import retrofit2.http.*

object CreatePaymentMethodDKVAPI {

    interface CreatePaymentMethodDKVService {
        /* Register a DKV Card as a payment method */
        /* By registering you allow the user to use a DKV Card as a payment method.
The payment method ID is optional when posting data.
 */
        @POST("payment-methods/dkv")
        fun createPaymentMethodDKV(
            @HeaderMap headers: Map<String, String>,
            @retrofit2.http.Body body: Body
        ): Call<PaymentMethod>
    }

    /* By registering you allow the user to use a DKV Card as a payment method.
    The payment method ID is optional when posting data.
     */
    class Body {

        var data: PaymentMethodDKVCreateBody? = null
    }

    open class Request : BaseRequest() {

        fun createPaymentMethodDKV(
            body: Body,
            readTimeout: Long? = null,
            additionalHeaders: Map<String, String>? = null,
            additionalParameters: Map<String, String>? = null
        ): Call<PaymentMethod> {
            val resources = listOf(PaymentMethod::class.java, PaymentMethodKind::class.java, PaymentMethodVendor::class.java, PaymentToken::class.java)
            val headers = headers(true, "application/vnd.api+json", "application/vnd.api+json", additionalHeaders)

            return retrofit(PayAPI.baseUrl, additionalParameters, readTimeout, resources)
                .create(CreatePaymentMethodDKVService::class.java)
                .createPaymentMethodDKV(
                    headers,
                    body
                )
        }
    }

    fun PayAPI.NewPaymentMethodsAPI.createPaymentMethodDKV(
        body: Body,
        readTimeout: Long? = null,
        additionalHeaders: Map<String, String>? = null,
        additionalParameters: Map<String, String>? = null
    ) = Request().createPaymentMethodDKV(
        body,
        readTimeout,
        additionalHeaders,
        additionalParameters
    )
}
