/*
 * PLEASE DO NOT EDIT!
 *
 * Generated by SwagGen with Kotlin template.
 * https://github.com/pace/SwagGen
 */

package cloud.pace.sdk.api.pay.generated.request.paymentTokens

import cloud.pace.sdk.api.pay.PayAPI
import cloud.pace.sdk.api.pay.generated.model.*
import cloud.pace.sdk.api.utils.EnumConverterFactory
import cloud.pace.sdk.api.utils.InterceptorUtils
import cloud.pace.sdk.utils.toIso8601
import com.google.gson.annotations.SerializedName
import com.squareup.moshi.Json
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapters.Rfc3339DateJsonAdapter
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import moe.banana.jsonapi2.JsonApi
import moe.banana.jsonapi2.JsonApiConverterFactory
import moe.banana.jsonapi2.Resource
import moe.banana.jsonapi2.ResourceAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.*
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit

object DeletePaymentTokenAPI {

    interface DeletePaymentTokenService {
        /* Delete the paymentToken record. */
        @DELETE("payment-tokens/{paymentTokenId}")
        fun deletePaymentToken(
            /* paymentToken ID. */
            @Path("paymentTokenId") paymentTokenId: String
        ): Call<Void>
    }

    fun PayAPI.PaymentTokensAPI.deletePaymentToken(paymentTokenId: String, readTimeout: Long? = null): Call<Void> {
        val service: DeletePaymentTokenService =
            Retrofit.Builder()
                .client(OkHttpClient.Builder()
                    .addNetworkInterceptor(InterceptorUtils.getInterceptor("application/json", "application/json", true))
                    .authenticator(InterceptorUtils.getAuthenticator())
                    .readTimeout(readTimeout ?: 10L, TimeUnit.SECONDS)
                    .build()
                )
                .baseUrl(PayAPI.baseUrl)
                .addConverterFactory(EnumConverterFactory())
                .addConverterFactory(
                    JsonApiConverterFactory.create(
                        Moshi.Builder()
                            .add(ResourceAdapterFactory.builder()
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
                .create(DeletePaymentTokenService::class.java)    

        return service.deletePaymentToken(paymentTokenId)
    }
}
