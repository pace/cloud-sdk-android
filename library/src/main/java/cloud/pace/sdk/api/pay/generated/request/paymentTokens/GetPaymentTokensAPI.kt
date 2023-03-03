/*
 * PLEASE DO NOT EDIT!
 *
 * Generated by SwagGen with Kotlin template.
 * https://github.com/pace/SwagGen
 */

package cloud.pace.sdk.api.pay.generated.request.paymentTokens

import cloud.pace.sdk.api.pay.PayAPI
import cloud.pace.sdk.api.pay.generated.model.*
import cloud.pace.sdk.api.request.BaseRequest
import com.google.gson.annotations.SerializedName
import com.squareup.moshi.Json
import retrofit2.Call
import retrofit2.http.*

object GetPaymentTokensAPI {

    interface GetPaymentTokensService {
        /* Get all valid payment tokens for user */
        /* Get all valid payment tokens for user. Valid means that a token was successfully created and is not expired. It might be unusable, for example if it is used in a transaction already. */
        @GET("payment-tokens")
        fun getPaymentTokens(
            @HeaderMap headers: Map<String, String>,
            @Query("filter[valid]") filtervalid: Filtervalid
        ): Call<PaymentTokens>
    }

    /* Get all valid payment tokens for user. Valid means that a token was successfully created and is not expired. It might be unusable, for example if it is used in a transaction already. */
    enum class Filtervalid(val value: String) {
        @SerializedName("true")
        @Json(name = "true")
        `TRUE`("true")
    }

    open class Request : BaseRequest() {

        fun getPaymentTokens(
            filtervalid: Filtervalid,
            readTimeout: Long? = null,
            additionalHeaders: Map<String, String>? = null,
            additionalParameters: Map<String, String>? = null
        ): Call<PaymentTokens> {
            val resources = listOf(PaymentMethodVendor::class.java, PaymentMethodKind::class.java, PaymentToken::class.java, PaymentMethod::class.java)
            val headers = headers(true, "application/vnd.api+json", "application/vnd.api+json", additionalHeaders)

            return retrofit(PayAPI.baseUrl, additionalParameters, readTimeout, resources)
                .create(GetPaymentTokensService::class.java)
                .getPaymentTokens(
                    headers,
                    filtervalid
                )
        }
    }

    fun PayAPI.PaymentTokensAPI.getPaymentTokens(
        filtervalid: Filtervalid,
        readTimeout: Long? = null,
        additionalHeaders: Map<String, String>? = null,
        additionalParameters: Map<String, String>? = null
    ) = Request().getPaymentTokens(
        filtervalid,
        readTimeout,
        additionalHeaders,
        additionalParameters
    )
}
