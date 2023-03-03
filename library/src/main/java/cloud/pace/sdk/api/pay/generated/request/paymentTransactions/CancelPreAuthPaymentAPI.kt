/*
 * PLEASE DO NOT EDIT!
 *
 * Generated by SwagGen with Kotlin template.
 * https://github.com/pace/SwagGen
 */

package cloud.pace.sdk.api.pay.generated.request.paymentTransactions

import cloud.pace.sdk.api.pay.PayAPI
import cloud.pace.sdk.api.pay.generated.model.*
import cloud.pace.sdk.api.request.BaseRequest
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

object CancelPreAuthPaymentAPI {

    interface CancelPreAuthPaymentService {
        /* Cancel PreAuth payment */
        /* PreAuth payments can be canceled in case the token was not used already. In addition to the transaction, the payment token will be revoked as well.
<br><br>
* Canceling the transaction and or the token is only permitted if the transaction is still open, otherwise a `403 Forbidden` will be returned.
* In case the transaction and token are already canceled and the request is repeated, the result will still be `204 No Content`.
 */
        @POST("transactions/{transactionId}/cancel")
        fun cancelPreAuthPayment(
            @HeaderMap headers: Map<String, String>,
            /* transaction ID. */
            @Path("transactionId") transactionId: String
        ): Call<ResponseBody>
    }

    open class Request : BaseRequest() {

        fun cancelPreAuthPayment(
            transactionId: String,
            readTimeout: Long? = null,
            additionalHeaders: Map<String, String>? = null,
            additionalParameters: Map<String, String>? = null
        ): Call<ResponseBody> {
            val headers = headers(true, "application/json", "application/json", additionalHeaders)

            return retrofit(PayAPI.baseUrl, additionalParameters, readTimeout)
                .create(CancelPreAuthPaymentService::class.java)
                .cancelPreAuthPayment(
                    headers,
                    transactionId
                )
        }
    }

    fun PayAPI.PaymentTransactionsAPI.cancelPreAuthPayment(
        transactionId: String,
        readTimeout: Long? = null,
        additionalHeaders: Map<String, String>? = null,
        additionalParameters: Map<String, String>? = null
    ) = Request().cancelPreAuthPayment(
        transactionId,
        readTimeout,
        additionalHeaders,
        additionalParameters
    )
}
