/*
 * PLEASE DO NOT EDIT!
 *
 * Generated by SwagGen with Kotlin template.
 * https://github.com/pace/SwagGen
 */

package cloud.pace.sdk.api.pay.generated.request.paymentTokens

import cloud.pace.sdk.api.pay.PayAPI
import cloud.pace.sdk.api.pay.generated.model.PaymentTokens
import cloud.pace.sdk.api.utils.EnumConverterFactory
import cloud.pace.sdk.api.utils.InterceptorUtils
import com.google.gson.annotations.SerializedName
import com.squareup.moshi.Json
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapters.Rfc3339DateJsonAdapter
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import moe.banana.jsonapi2.JsonApiConverterFactory
import moe.banana.jsonapi2.ResourceAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.HeaderMap
import retrofit2.http.Query
import java.util.Date
import java.util.concurrent.TimeUnit

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

    fun PayAPI.PaymentTokensAPI.getPaymentTokens(
        filtervalid: Filtervalid,
        readTimeout: Long? = null,
        additionalHeaders: Map<String, String>? = null,
        additionalParameters: Map<String, String>? = null
    ): Call<PaymentTokens> {
        val client = OkHttpClient.Builder().addInterceptor(InterceptorUtils.getInterceptor(additionalParameters))
        val headers = InterceptorUtils.getHeaders(true, "application/vnd.api+json", "application/vnd.api+json", additionalHeaders)

        if (readTimeout != null) {
            client.readTimeout(readTimeout, TimeUnit.SECONDS)
        }

        val service: GetPaymentTokensService =
            Retrofit.Builder()
                .client(client.build())
                .baseUrl(PayAPI.baseUrl)
                .addConverterFactory(EnumConverterFactory())
                .addConverterFactory(
                    JsonApiConverterFactory.create(
                        Moshi.Builder()
                            .add(
                                ResourceAdapterFactory.builder()
                                    .build()
                            )
                            .add(Date::class.java, Rfc3339DateJsonAdapter().nullSafe())
                            .add(KotlinJsonAdapterFactory())
                            .build()
                    )
                )
                .addConverterFactory(
                    MoshiConverterFactory.create(
                        Moshi.Builder()
                            .add(Date::class.java, Rfc3339DateJsonAdapter().nullSafe())
                            .add(KotlinJsonAdapterFactory())
                            .build()
                    )
                )
                .build()
                .create(GetPaymentTokensService::class.java)

        return service.getPaymentTokens(
            headers,
            filtervalid
        )
    }
}
