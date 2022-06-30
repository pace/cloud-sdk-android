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

    private val service: GetPaymentMethodVendorsService by lazy {
        Retrofit.Builder()
            .client(OkHttpClient.Builder().addInterceptor(InterceptorUtils.getInterceptor()).build())
            .baseUrl(API.environment.cdnUrl)
            .addConverterFactory(EnumConverterFactory())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GetPaymentMethodVendorsService::class.java)
    }

    fun CdnAPI.getPaymentMethodVendors(additionalHeaders: Map<String, String>? = null) =
        service.getPaymentMethodVendors(InterceptorUtils.getHeaders(true, "application/json", "application/json", additionalHeaders))
}
