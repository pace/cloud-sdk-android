/*
 * PLEASE DO NOT EDIT!
 *
 * Generated by SwagGen with Kotlin template.
 * https://github.com/pace/SwagGen
 */

package cloud.pace.sdk.api.pay.generated.request.paymentTransactions

import cloud.pace.sdk.api.pay.PayAPI
import cloud.pace.sdk.api.pay.generated.model.Discount
import cloud.pace.sdk.api.pay.generated.model.Transaction
import cloud.pace.sdk.api.pay.generated.model.TransactionCreateBody
import cloud.pace.sdk.api.request.BaseRequest
import retrofit2.Call
import retrofit2.http.*

object ProcessPaymentAPI {

    interface ProcessPaymentService {
        /* Process payment */
        /* Process payment and notify user (payment receipt) if transaction is finished successfully.
The `priceIncludingVAT` and `currency` attributes are required, unless when announcing a transaction in which case those values are copied from the token and any given values are ignored.
<br><br>
Only use after approaching (fueling api), otherwise returns `403 Forbidden`.
 */
        @POST("transactions")
        fun processPayment(
            @HeaderMap headers: Map<String, String>,
            /* Announcing the transaction without actually capturing the payment. An announced transaction can later be processed only if providing the same `paymentToken`, `purposePRN`, and `providerPRN`. By announcing the transaction the token is locked to be used only with this transaction. The `priceIncludingVAT` and `currency` will be taken from the token, and upon capturing the transaction, must be equal or lower than what was announced. */
            @Query("announce") announce: Boolean? = null,
            @retrofit2.http.Body body: Body
        ): Call<Transaction>
    }

    /* Process payment and notify user (payment receipt) if transaction is finished successfully.
    The `priceIncludingVAT` and `currency` attributes are required, unless when announcing a transaction in which case those values are copied from the token and any given values are ignored.
    <br><br>
    Only use after approaching (fueling api), otherwise returns `403 Forbidden`.
     */
    class Body {

        var data: TransactionCreateBody? = null
    }

    open class Request : BaseRequest() {

        fun processPayment(
            announce: Boolean? = null,
            body: Body,
            readTimeout: Long? = null,
            additionalHeaders: Map<String, String>? = null,
            additionalParameters: Map<String, String>? = null
        ): Call<Transaction> {
            val resources = listOf(Discount::class.java)
            val headers = headers(true, "application/vnd.api+json", "application/vnd.api+json", additionalHeaders)

            return retrofit(PayAPI.baseUrl, additionalParameters, readTimeout, resources)
                .create(ProcessPaymentService::class.java)
                .processPayment(
                    headers,
                    announce,
                    body
                )
        }
    }

    fun PayAPI.PaymentTransactionsAPI.processPayment(
        announce: Boolean? = null,
        body: Body,
        readTimeout: Long? = null,
        additionalHeaders: Map<String, String>? = null,
        additionalParameters: Map<String, String>? = null
    ) = Request().processPayment(
        announce,
        body,
        readTimeout,
        additionalHeaders,
        additionalParameters
    )
}
