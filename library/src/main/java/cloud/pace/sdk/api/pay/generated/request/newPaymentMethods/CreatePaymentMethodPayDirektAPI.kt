/*
 * PLEASE DO NOT EDIT!
 *
 * Generated by SwagGen with Kotlin template.
 * https://github.com/pace/SwagGen
 */

package cloud.pace.sdk.api.pay.generated.request.newPaymentMethods

import cloud.pace.sdk.api.pay.PayAPI
import cloud.pace.sdk.api.pay.generated.model.PaymentMethod
import cloud.pace.sdk.api.pay.generated.model.PaymentMethodPayDirektCreateBody
import cloud.pace.sdk.api.utils.EnumConverterFactory
import cloud.pace.sdk.api.utils.InterceptorUtils
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapters.Rfc3339DateJsonAdapter
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import moe.banana.jsonapi2.JsonApiConverterFactory
import moe.banana.jsonapi2.ResourceAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.*
import java.util.Date
import java.util.concurrent.TimeUnit

object CreatePaymentMethodPayDirektAPI {

    interface CreatePaymentMethodPayDirektService {
        /* Register PayDirekt as a payment method */
        /* By registering you allow the user to use PayDirekt as a payment method.
The payment method ID is optional when posting data.
Registering PayDirekt as payment method is a 2-step process, thus the payment method will only be created after the user approved it on the PayDirekt website. The approval URL in the response will point you to the correct page. After the user takes action the user is redirected to one of the three redirect URLs provided by you.
 */
        @POST("payment-methods/paydirekt")
        fun createPaymentMethodPayDirekt(
            @HeaderMap headers: Map<String, String>,
            @retrofit2.http.Body body: Body
        ): Call<PaymentMethod>
    }

    /* By registering you allow the user to use PayDirekt as a payment method.
    The payment method ID is optional when posting data.
    Registering PayDirekt as payment method is a 2-step process, thus the payment method will only be created after the user approved it on the PayDirekt website. The approval URL in the response will point you to the correct page. After the user takes action the user is redirected to one of the three redirect URLs provided by you.
     */
    class Body {

        var data: PaymentMethodPayDirektCreateBody? = null
    }

    fun PayAPI.NewPaymentMethodsAPI.createPaymentMethodPayDirekt(
        body: Body,
        readTimeout: Long? = null,
        additionalHeaders: Map<String, String>? = null,
        additionalParameters: Map<String, String>? = null
    ): Call<PaymentMethod> {
        val client = OkHttpClient.Builder().addInterceptor(InterceptorUtils.getInterceptor(additionalParameters))
        val headers = InterceptorUtils.getHeaders(true, "application/vnd.api+json", "application/vnd.api+json", additionalHeaders)

        if (readTimeout != null) {
            client.readTimeout(readTimeout, TimeUnit.SECONDS)
        }

        val service: CreatePaymentMethodPayDirektService =
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
                .create(CreatePaymentMethodPayDirektService::class.java)

        return service.createPaymentMethodPayDirekt(
            headers,
            body
        )
    }
}
