package cloud.pace.sdk.poikit.pricehistory

import cloud.pace.sdk.api.utils.EnumConverterFactory
import cloud.pace.sdk.api.utils.InterceptorUtils
import cloud.pace.sdk.utils.Completion
import cloud.pace.sdk.utils.Environment
import cloud.pace.sdk.utils.handleCallback
import cloud.pace.sdk.utils.toIso8601
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapters.Rfc3339DateJsonAdapter
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.HeaderMap
import retrofit2.http.Path
import retrofit2.http.Query
import java.util.*

interface PriceHistoryAPI {

    @GET("prices/fueling/countries/{countryCode}")
    fun getPricesByCountry(
        @HeaderMap headers: Map<String, String>,
        @Path("countryCode") countryCode: String,
        @Query("filter[since]") since: String,
        @Query("granularity") granularity: String,
        @Query("forecast") forecast: Boolean
    ): Call<List<PriceHistory>>

    @GET("prices/fueling/countries/{countryCode}/{fuelType}")
    fun getPricesByCountry(
        @HeaderMap headers: Map<String, String>,
        @Path("countryCode") countryCode: String,
        @Path("fuelType") fuelType: String,
        @Query("filter[since]") since: String,
        @Query("granularity") granularity: String,
        @Query("forecast") forecast: Boolean
    ): Call<List<PriceHistoryFuelType>>

    @GET("prices/fueling/stations/{stationId}")
    fun getPricesByStation(
        @HeaderMap headers: Map<String, String>,
        @Path("stationId") stationId: String,
        @Query("filter[since]") since: String,
        @Query("granularity") granularity: String,
        @Query("forecast") forecast: Boolean
    ): Call<List<PriceHistory>>

    @GET("prices/fueling/stations/{stationId}/{fuelType}")
    fun getPricesByStation(
        @HeaderMap headers: Map<String, String>,
        @Path("stationId") stationId: String,
        @Path("fuelType") fuelType: String,
        @Query("filter[since]") since: String,
        @Query("granularity") granularity: String,
        @Query("forecast") forecast: Boolean
    ): Call<List<PriceHistoryFuelType>>
}

class PriceHistoryClient(environment: Environment) {

    private val service = create("${environment.apiUrl}/price-service/")

    fun getPricesByCountry(countryCode: String, since: Date, granularity: String, forecast: Boolean, completion: (Completion<List<PriceHistory>>) -> Unit) {
        service.getPricesByCountry(
            InterceptorUtils.getHeaders(true, "application/vnd.api+json", "application/vnd.api+json"),
            countryCode,
            since.toIso8601(),
            granularity,
            forecast
        ).handleCallback(completion)
    }

    fun getPricesByCountry(countryCode: String, fuelType: String, since: Date, granularity: String, forecast: Boolean, completion: (Completion<List<PriceHistoryFuelType>>) -> Unit) {
        service.getPricesByCountry(
            InterceptorUtils.getHeaders(true, "application/vnd.api+json", "application/vnd.api+json"),
            countryCode,
            fuelType,
            since.toIso8601(),
            granularity,
            forecast
        ).handleCallback(completion)
    }

    fun getPricesByStation(stationId: String, since: Date, granularity: String, forecast: Boolean, completion: (Completion<List<PriceHistory>>) -> Unit) {
        service.getPricesByStation(
            InterceptorUtils.getHeaders(true, "application/vnd.api+json", "application/vnd.api+json"),
            stationId,
            since.toIso8601(),
            granularity,
            forecast
        ).handleCallback(completion)
    }

    fun getPricesByStation(stationId: String, fuelType: String, since: Date, granularity: String, forecast: Boolean, completion: (Completion<List<PriceHistoryFuelType>>) -> Unit) {
        service.getPricesByStation(
            InterceptorUtils.getHeaders(true, "application/vnd.api+json", "application/vnd.api+json"),
            stationId,
            fuelType,
            since.toIso8601(),
            granularity,
            forecast
        ).handleCallback(completion)
    }

    private fun create(baseUrl: String): PriceHistoryAPI {
        return Retrofit.Builder()
            .client(OkHttpClient.Builder().addInterceptor(InterceptorUtils.getInterceptor()).build())
            .baseUrl(baseUrl)
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
            .create(PriceHistoryAPI::class.java)
    }
}
