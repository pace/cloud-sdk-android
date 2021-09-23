package cloud.pace.sdk.poikit.poi.download

import cloud.pace.sdk.api.API
import cloud.pace.sdk.api.utils.EnumConverterFactory
import cloud.pace.sdk.api.utils.InterceptorUtils
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapters.Rfc3339DateJsonAdapter
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import java.util.*

object CmsAPI {
    interface GetPaymentMethodVendorsService {
        @GET("cms/payment-method-vendors")
        fun getPaymentMethodVendors(): Call<List<PaymentMethodVendor>>
    }

    private val service: GetPaymentMethodVendorsService by lazy {
        val client = OkHttpClient.Builder()
            .addNetworkInterceptor(InterceptorUtils.getInterceptor("application/json", "application/json", true))
            .authenticator(InterceptorUtils.getAuthenticator())

        Retrofit.Builder()
            .client(client.build())
            .baseUrl(API.baseUrl)
            .addConverterFactory(EnumConverterFactory())
            .addConverterFactory(
                MoshiConverterFactory.create(
                    Moshi.Builder()
                        .add(Date::class.java, Rfc3339DateJsonAdapter().nullSafe())
                        .add(KotlinJsonAdapterFactory())
                        .build()
                )
            )
            .build()
            .create(GetPaymentMethodVendorsService::class.java)
    }

    fun CmsAPI.getPaymentMethodVendors() = service.getPaymentMethodVendors()
}
