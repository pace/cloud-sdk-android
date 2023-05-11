package cloud.pace.sdk.api.vendor

import cloud.pace.sdk.api.API
import cloud.pace.sdk.api.converter.EnumConverterFactory
import cloud.pace.sdk.api.request.BaseRequest
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.HeaderMap

object PaymentMethodVendorsAPI : BaseRequest() {

    interface GetPaymentMethodVendorsService {
        @GET("pay/payment-method-vendors.json")
        fun getPaymentMethodVendors(@HeaderMap headers: Map<String, String>): Call<List<PaymentMethodVendor>>
    }

    fun PaymentMethodVendorsAPI.getPaymentMethodVendors(additionalHeaders: Map<String, String>? = null, additionalParameters: Map<String, String>? = null): Call<List<PaymentMethodVendor>> {
        val headers = headers(true, "application/json", "application/json", additionalHeaders)

        return Retrofit.Builder()
            .client(okHttpClient(additionalParameters))
            .baseUrl(API.environment.cdnUrl)
            .addConverterFactory(EnumConverterFactory())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GetPaymentMethodVendorsService::class.java)
            .getPaymentMethodVendors(headers)
    }
}
