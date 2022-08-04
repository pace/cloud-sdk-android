package cloud.pace.sdk.poikit.poi.download

import cloud.pace.sdk.api.API
import cloud.pace.sdk.api.utils.EnumConverterFactory
import cloud.pace.sdk.api.utils.InterceptorUtils
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.HeaderMap

object CdnAPI {

    interface GetPaymentMethodVendorsService {
        @GET("pay/payment-method-vendors.json")
        fun getPaymentMethodVendors(@HeaderMap headers: Map<String, String>): Call<List<PaymentMethodVendor>>
    }

    fun CdnAPI.getPaymentMethodVendors(additionalHeaders: Map<String, String>? = null, additionalParameters: Map<String, String>? = null): Call<List<PaymentMethodVendor>> {
        val client = OkHttpClient.Builder().addInterceptor(InterceptorUtils.getInterceptor(additionalParameters))
        val headers = InterceptorUtils.getHeaders(true, "application/json", "application/json", additionalHeaders)
        val service: GetPaymentMethodVendorsService =
            Retrofit.Builder()
                .client(client.build())
                .baseUrl(API.environment.cdnUrl)
                .addConverterFactory(EnumConverterFactory())
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(GetPaymentMethodVendorsService::class.java)

        return service.getPaymentMethodVendors(headers)
    }
}
