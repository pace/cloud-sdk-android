/*
 * PLEASE DO NOT EDIT!
 *
 * Generated by SwagGen with Kotlin template.
 * https://github.com/pace/SwagGen
 */

package cloud.pace.sdk.api.pay.generated.request.paymentMethods

import cloud.pace.sdk.api.pay.PayAPI
import cloud.pace.sdk.api.request.BaseRequest
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.HeaderMap
import retrofit2.http.Path

object ConfirmPaymentMethodAPI {

    interface ConfirmPaymentMethodService {
        /* Confirm and redirect to frontend */
        /* Redirect endpoint to confirm a payment method. External services redirect the user here and in turn this endpoint redirects the user to the frontend. */
        @GET("payment-methods/confirm/{token}")
        fun confirmPaymentMethod(
            @HeaderMap headers: Map<String, String>,
            /* single use token */
            @Path("token") token: String
        ): Call<ResponseBody>
    }

    open class Request : BaseRequest() {

        fun confirmPaymentMethod(
            token: String,
            readTimeout: Long? = null,
            additionalHeaders: Map<String, String>? = null,
            additionalParameters: Map<String, String>? = null
        ): Call<ResponseBody> {
            val headers = headers(false, "application/json", "application/json", additionalHeaders)

            return retrofit(PayAPI.baseUrl, additionalParameters, readTimeout)
                .create(ConfirmPaymentMethodService::class.java)
                .confirmPaymentMethod(
                    headers,
                    token
                )
        }
    }

    fun PayAPI.PaymentMethodsAPI.confirmPaymentMethod(
        token: String,
        readTimeout: Long? = null,
        additionalHeaders: Map<String, String>? = null,
        additionalParameters: Map<String, String>? = null
    ) = Request().confirmPaymentMethod(
        token,
        readTimeout,
        additionalHeaders,
        additionalParameters
    )
}
