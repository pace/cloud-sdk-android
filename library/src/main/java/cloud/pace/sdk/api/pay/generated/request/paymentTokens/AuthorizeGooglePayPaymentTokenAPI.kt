/*
 * PLEASE DO NOT EDIT!
 *
 * Generated by SwagGen with Kotlin template.
 * https://github.com/pace/SwagGen
 */

package cloud.pace.sdk.api.pay.generated.request.paymentTokens

import cloud.pace.sdk.api.pay.PayAPI
import cloud.pace.sdk.api.pay.generated.model.PaymentMethod
import cloud.pace.sdk.api.pay.generated.model.PaymentMethodKind
import cloud.pace.sdk.api.pay.generated.model.PaymentMethodVendor
import cloud.pace.sdk.api.pay.generated.model.PaymentToken
import cloud.pace.sdk.api.pay.generated.model.PaymentTokenCreateGooglePayBody
import cloud.pace.sdk.api.request.BaseRequest
import retrofit2.Call
import retrofit2.http.*

object AuthorizeGooglePayPaymentTokenAPI {

    interface AuthorizeGooglePayPaymentTokenService {
        /* Authorize a payment using Google Pay by providing a Google Pay token. */
        /* When successful, returns a paymentToken value. Requires the caller to interact with Google Pay
to create the `googlePay` specific authorization data.
 */
        @POST("payment-method-kinds/googlepay/authorize")
        fun authorizeGooglePayPaymentToken(
            @HeaderMap headers: Map<String, String>,
            @retrofit2.http.Body body: Body
        ): Call<PaymentToken>
    }

    /* When successful, returns a paymentToken value. Requires the caller to interact with Google Pay
    to create the `googlePay` specific authorization data.
     */
    class Body {

        var data: PaymentTokenCreateGooglePayBody? = null
    }

    open class Request : BaseRequest() {

        fun authorizeGooglePayPaymentToken(
            body: Body,
            readTimeout: Long? = null,
            additionalHeaders: Map<String, String>? = null,
            additionalParameters: Map<String, String>? = null
        ): Call<PaymentToken> {
            val resources = listOf(PaymentMethod::class.java, PaymentMethodKind::class.java, PaymentMethodVendor::class.java, PaymentToken::class.java)
            val headers = headers(true, "application/vnd.api+json", "application/vnd.api+json", additionalHeaders)

            return retrofit(PayAPI.baseUrl, additionalParameters, readTimeout, resources)
                .create(AuthorizeGooglePayPaymentTokenService::class.java)
                .authorizeGooglePayPaymentToken(
                    headers,
                    body
                )
        }
    }

    fun PayAPI.PaymentTokensAPI.authorizeGooglePayPaymentToken(
        body: Body,
        readTimeout: Long? = null,
        additionalHeaders: Map<String, String>? = null,
        additionalParameters: Map<String, String>? = null
    ) = Request().authorizeGooglePayPaymentToken(
        body,
        readTimeout,
        additionalHeaders,
        additionalParameters
    )
}
