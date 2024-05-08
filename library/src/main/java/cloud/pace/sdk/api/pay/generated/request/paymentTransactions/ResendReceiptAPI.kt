/*
 * PLEASE DO NOT EDIT!
 *
 * Generated by SwagGen with Kotlin template.
 * https://github.com/pace/SwagGen
 */

package cloud.pace.sdk.api.pay.generated.request.paymentTransactions

import cloud.pace.sdk.api.pay.PayAPI
import cloud.pace.sdk.api.pay.generated.model.TransactionIDListBody
import cloud.pace.sdk.api.request.BaseRequest
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

object ResendReceiptAPI {

    interface ResendReceiptService {
        /* Resend receipt for a transaction(s) */
        /* Resends the receipt(s) that has/have already been sent via email (when processing the payment). The maximum amount of receipts per request is 8.
 */
        @POST("receipts/resend")
        fun resendReceipt(
            @HeaderMap headers: Map<String, String>,
            /* (Optional) Specify the language you want the returned receipt to be localized in.
Returns the receipt in the default language that is available if the specified language is not available.
Language does not have to be valid language. For example, `language=local` means that the receipt should be displayed
in the language that is determined to be spoken in the area that the point of intereset at which the receipt has been generated at.
*Prefer using the `Accept-Language` header if you use this endpoint on an end-user device.*
 */
            @Query("language") language: String? = null,
            @retrofit2.http.Body body: Body
        ): Call<ResponseBody>
    }

    /* Resends the receipt(s) that has/have already been sent via email (when processing the payment). The maximum amount of receipts per request is 8.
     */
    class Body {

        var data: TransactionIDListBody? = null
    }

    open class Request : BaseRequest() {

        fun resendReceipt(
            language: String? = null,
            body: Body,
            readTimeout: Long? = null,
            additionalHeaders: Map<String, String>? = null,
            additionalParameters: Map<String, String>? = null
        ): Call<ResponseBody> {
            val headers = headers(true, "application/vnd.api+json", "application/vnd.api+json", additionalHeaders)

            return retrofit(PayAPI.baseUrl, additionalParameters, readTimeout)
                .create(ResendReceiptService::class.java)
                .resendReceipt(
                    headers,
                    language,
                    body
                )
        }
    }

    fun PayAPI.PaymentTransactionsAPI.resendReceipt(
        language: String? = null,
        body: Body,
        readTimeout: Long? = null,
        additionalHeaders: Map<String, String>? = null,
        additionalParameters: Map<String, String>? = null
    ) = Request().resendReceipt(
        language,
        body,
        readTimeout,
        additionalHeaders,
        additionalParameters
    )
}