/*
 * PLEASE DO NOT EDIT!
 *
 * Generated by SwagGen with Kotlin template.
 * https://github.com/pace/SwagGen
 */

package cloud.pace.sdk.api.pay.generated.request.paymentMethods

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
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.*
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit

object GetPaymentMethodsIncludingCreditCheckAPI {

    interface GetPaymentMethodsIncludingCreditCheckService {
        /* Get all ready-to-use payment methods for user */
        /* This request will return a list of supported payment methods for the current user that they can, in theory, use. That is, ones that are valid and can immediately be used.</br></br>
This is as opposed to the regular `/payment-methods`, which does not categorize payment methods as valid for use.</br></br>
You should trigger this when the user is approaching on a gas station with fueling support to get a list of available payment methods.</br></br>
If the list is empty, you can ask the user to add a payment method to use PACE fueling. */
        @GET("payment-methods")
        fun getPaymentMethodsIncludingCreditCheck(
            @Query("filter[status]") filterstatus: Filterstatus,
            @Query("filter[purpose]") filterpurpose: PRN? = null
        ): Call<PaymentMethods>
    }

    /* This request will return a list of supported payment methods for the current user that they can, in theory, use. That is, ones that are valid and can immediately be used.</br></br>
    This is as opposed to the regular `/payment-methods`, which does not categorize payment methods as valid for use.</br></br>
    You should trigger this when the user is approaching on a gas station with fueling support to get a list of available payment methods.</br></br>
    If the list is empty, you can ask the user to add a payment method to use PACE fueling. */
    enum class Filterstatus(val value: String) {
        @SerializedName("valid")
        @Json(name = "valid")
        VALID("valid")
    }

    fun PayAPI.PaymentMethodsAPI.getPaymentMethodsIncludingCreditCheck(filterstatus: Filterstatus, filterpurpose: PRN? = null, readTimeout: Long? = null): Call<PaymentMethods> {
        val client = OkHttpClient.Builder()
                        .addNetworkInterceptor(InterceptorUtils.getInterceptor("application/vnd.api+json", "application/vnd.api+json", true))
                        .authenticator(InterceptorUtils.getAuthenticator())

        if (readTimeout != null) {
            client.readTimeout(readTimeout, TimeUnit.SECONDS)
        }

        val service: GetPaymentMethodsIncludingCreditCheckService =
            Retrofit.Builder()
                .client(client.build())
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
                .create(GetPaymentMethodsIncludingCreditCheckService::class.java)

        return service.getPaymentMethodsIncludingCreditCheck(filterstatus, filterpurpose)
    }
}
