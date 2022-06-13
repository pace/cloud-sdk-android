/*
 * PLEASE DO NOT EDIT!
 *
 * Generated by SwagGen with Kotlin template.
 * https://github.com/pace/SwagGen
 */

package cloud.pace.sdk.api.pay.generated.request.newPaymentMethods

import cloud.pace.sdk.api.pay.PayAPI
import cloud.pace.sdk.api.pay.generated.model.PaymentMethod
import cloud.pace.sdk.api.pay.generated.model.PaymentMethodKind
import cloud.pace.sdk.api.pay.generated.model.PaymentMethodPayPalCreateBody
import cloud.pace.sdk.api.pay.generated.model.PaymentMethodVendor
import cloud.pace.sdk.api.pay.generated.model.PaymentToken
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

object CreatePaymentMethodPayPalAPI {

    interface CreatePaymentMethodPayPalService {
        /* Register PayPal as a payment method */
        /* By registering you allow the user to use PayPal as a payment method.
The payment method ID is optional when posting data.
If you provide a valid Billing Agreement ID, the payment method is created directly. Alternatively you can provide all three redirect URLs in which case the backend will create the Billing Agreement for you. Creating a Billing Agreement is a 2-step process, thus the payment method will only be created after the user approved it on the PayPal website. The approval URL in the response will point you to the correct page. After the user takes action the user is redirected to one of the three redirect URLs provided by you.
 */
        @POST("payment-methods/paypal")
        fun createPaymentMethodPayPal(
            @HeaderMap headers: Map<String, String>,
            @retrofit2.http.Body body: Body
        ): Call<PaymentMethod>
    }

    /* By registering you allow the user to use PayPal as a payment method.
    The payment method ID is optional when posting data.
    If you provide a valid Billing Agreement ID, the payment method is created directly. Alternatively you can provide all three redirect URLs in which case the backend will create the Billing Agreement for you. Creating a Billing Agreement is a 2-step process, thus the payment method will only be created after the user approved it on the PayPal website. The approval URL in the response will point you to the correct page. After the user takes action the user is redirected to one of the three redirect URLs provided by you.
     */
    class Body {

        var data: PaymentMethodPayPalCreateBody? = null
    }

    fun PayAPI.NewPaymentMethodsAPI.createPaymentMethodPayPal(
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

        val service: CreatePaymentMethodPayPalService =
            Retrofit.Builder()
                .client(client.build())
                .baseUrl(PayAPI.baseUrl)
                .addConverterFactory(EnumConverterFactory())
                .addConverterFactory(
                    JsonApiConverterFactory.create(
                        Moshi.Builder()
                            .add(
                                ResourceAdapterFactory.builder()
                                    .add(PaymentMethodKind::class.java)
                                    .add(PaymentMethod::class.java)
                                    .add(PaymentToken::class.java)
                                    .add(PaymentMethodVendor::class.java)
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
                .create(CreatePaymentMethodPayPalService::class.java)

        return service.createPaymentMethodPayPal(
            headers,
            body
        )
    }
}
