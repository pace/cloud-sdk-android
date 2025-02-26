/*
 * PLEASE DO NOT EDIT!
 *
 * Generated by SwagGen with Kotlin template.
 * https://github.com/pace/SwagGen
 */

package cloud.pace.sdk.api.pay.generated.request.paymentMethodKinds

import cloud.pace.sdk.api.pay.PayAPI
import cloud.pace.sdk.api.pay.generated.model.PaymentMethodKinds
import cloud.pace.sdk.api.request.BaseRequest
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.HeaderMap
import retrofit2.http.Query

object GetPaymentMethodKindsAPI {

    interface GetPaymentMethodKindsService {
        /* Get all payment method kinds */
        /* Returns all payment method kinds that are supported by this service. Use the Accept-Language header for localization. */
        @GET("payment-method-kinds")
        fun getPaymentMethodKinds(
            @HeaderMap headers: Map<String, String>,
            /* Language preference of localized response properties. The full standard of RFC 7231 (https://tools.ietf.org/html/rfc7231#section-5.3.5) is supported. */
            @Header("Accept-Language") acceptLanguage: String? = null,
            /* Flag to allow more data to the payment method kinds. */
            @Query("additionalData") additionalData: Boolean? = null,
            /* Filter allowed payment methods kinds by poi. */
            @Query("poiID") poiID: String? = null
        ): Call<PaymentMethodKinds>
    }

    open class Request : BaseRequest() {

        fun getPaymentMethodKinds(
            acceptLanguage: String? = null,
            additionalData: Boolean? = null,
            poiID: String? = null,
            readTimeout: Long? = null,
            additionalHeaders: Map<String, String>? = null,
            additionalParameters: Map<String, String>? = null
        ): Call<PaymentMethodKinds> {
            val headers = headers(true, "application/vnd.api+json", "application/vnd.api+json", additionalHeaders)

            return retrofit(PayAPI.baseUrl, additionalParameters, readTimeout)
                .create(GetPaymentMethodKindsService::class.java)
                .getPaymentMethodKinds(
                    headers,
                    acceptLanguage,
                    additionalData,
                    poiID
                )
        }
    }

    fun PayAPI.PaymentMethodKindsAPI.getPaymentMethodKinds(
        acceptLanguage: String? = null,
        additionalData: Boolean? = null,
        poiID: String? = null,
        readTimeout: Long? = null,
        additionalHeaders: Map<String, String>? = null,
        additionalParameters: Map<String, String>? = null
    ) = Request().getPaymentMethodKinds(
        acceptLanguage,
        additionalData,
        poiID,
        readTimeout,
        additionalHeaders,
        additionalParameters
    )
}
