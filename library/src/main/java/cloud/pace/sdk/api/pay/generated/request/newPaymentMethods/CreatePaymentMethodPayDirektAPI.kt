/*
 * PLEASE DO NOT EDIT!
 *
 * Generated by SwagGen with Kotlin template.
 * https://github.com/pace/SwagGen
 */

package cloud.pace.sdk.api.pay.generated.request.newPaymentMethods

import cloud.pace.sdk.api.pay.PayAPI
import cloud.pace.sdk.api.pay.generated.model.PaymentMethod
import cloud.pace.sdk.api.pay.generated.model.PaymentMethodKind
import cloud.pace.sdk.api.pay.generated.model.PaymentMethodPayDirektCreateBody
import cloud.pace.sdk.api.pay.generated.model.PaymentMethodVendor
import cloud.pace.sdk.api.pay.generated.model.PaymentToken
import cloud.pace.sdk.api.request.BaseRequest
import retrofit2.Call
import retrofit2.http.*

object CreatePaymentMethodPayDirektAPI {

    interface CreatePaymentMethodPayDirektService {
        /* Register PayDirekt as a payment method */
        /* By registering you allow the user to use PayDirekt as a payment method.
The payment method ID is optional when posting data.
Registering PayDirekt as payment method is a 2-step process, thus the payment method will only be created after the user approved it on the PayDirekt website. The approval URL in the response will point you to the correct page. After the user takes action the user is redirected to one of the three redirect URLs provided by you.
 */
        @POST("payment-methods/paydirekt")
        fun createPaymentMethodPayDirekt(
            @HeaderMap headers: Map<String, String>,
            @retrofit2.http.Body body: Body
        ): Call<PaymentMethod>
    }

    /* By registering you allow the user to use PayDirekt as a payment method.
    The payment method ID is optional when posting data.
    Registering PayDirekt as payment method is a 2-step process, thus the payment method will only be created after the user approved it on the PayDirekt website. The approval URL in the response will point you to the correct page. After the user takes action the user is redirected to one of the three redirect URLs provided by you.
     */
    class Body {

        var data: PaymentMethodPayDirektCreateBody? = null
    }

    open class Request : BaseRequest() {

        fun createPaymentMethodPayDirekt(
            body: Body,
            readTimeout: Long? = null,
            additionalHeaders: Map<String, String>? = null,
            additionalParameters: Map<String, String>? = null
        ): Call<PaymentMethod> {
            val resources = listOf(PaymentMethod::class.java, PaymentMethodKind::class.java, PaymentMethodVendor::class.java, PaymentToken::class.java)
            val headers = headers(true, "application/vnd.api+json", "application/vnd.api+json", additionalHeaders)

            return retrofit(PayAPI.baseUrl, additionalParameters, readTimeout, resources)
                .create(CreatePaymentMethodPayDirektService::class.java)
                .createPaymentMethodPayDirekt(
                    headers,
                    body
                )
        }
    }

    fun PayAPI.NewPaymentMethodsAPI.createPaymentMethodPayDirekt(
        body: Body,
        readTimeout: Long? = null,
        additionalHeaders: Map<String, String>? = null,
        additionalParameters: Map<String, String>? = null
    ) = Request().createPaymentMethodPayDirekt(
        body,
        readTimeout,
        additionalHeaders,
        additionalParameters
    )
}
