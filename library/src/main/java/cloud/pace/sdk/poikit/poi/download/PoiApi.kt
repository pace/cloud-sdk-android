package cloud.pace.sdk.poikit.poi.download

import cloud.pace.sdk.poikit.poi.FuelType
import cloud.pace.sdk.poikit.utils.ApiException
import cloud.pace.sdk.poikit.utils.ApiUtils
import cloud.pace.sdk.utils.*
import com.squareup.moshi.Moshi
import moe.banana.jsonapi2.JsonApi
import moe.banana.jsonapi2.JsonApiConverterFactory
import moe.banana.jsonapi2.Resource
import moe.banana.jsonapi2.ResourceAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

class PoiApiClient(environment: Environment, apiKey: String) {
    private var api = PoiApi.create(environment.apiUrl, apiKey)

    fun getRegionalPrices(lat: Double, long: Double, completion: (Completion<List<RegionalPriceResponse>?>) -> Unit) {
        api.getRegionalPrices(lat, long).enqueue {
            onResponse = {
                val body = it.body()
                if (it.isSuccessful && body != null) {
                    completion(Success(body))
                } else {
                    completion(Failure(ApiException(it.code(), it.message())))
                }
            }

            onFailure = {
                completion(Failure(it ?: Exception("Unknown exception")))
            }
        }
    }
}

interface PoiApi {
    @GET("poi/beta/prices/regional")
    fun getRegionalPrices(
        @Query("filter[latitude]") latitude: Double,
        @Query("filter[longitude]") longitude: Double
    ): Call<List<RegionalPriceResponse>>

    companion object Factory {
        const val CONNECT_TIMEOUT = 10_000L
        const val READ_TIMEOUT = 30_000L

        private val jsonApiAdapterFactory = ResourceAdapterFactory.builder()
            .add(RegionalPriceResponse::class.java)
            .build()

        private val moshi = Moshi.Builder()
            .add(jsonApiAdapterFactory)
            .build()

        fun create(baseUrl: String, apiKey: String): PoiApi {
            val okHttpClient = OkHttpClient.Builder()
                .connectTimeout(CONNECT_TIMEOUT, TimeUnit.MILLISECONDS)
                .readTimeout(READ_TIMEOUT, TimeUnit.MILLISECONDS)
                .addInterceptor {
                    it.proceed(
                        it.request()
                            .newBuilder()
                            .header(ApiUtils.ACCEPT_HEADER, "application/vnd.api+json")
                            .header(ApiUtils.CONTENT_TYPE_HEADER, "application/vnd.api+json")
                            .header(ApiUtils.USER_AGENT_HEADER, ApiUtils.getUserAgent())
                            .header(ApiUtils.API_KEY, apiKey)
                            .build()
                    )
                }
                .build()

            val client = Retrofit.Builder()
                .client(okHttpClient)
                .baseUrl(baseUrl)
                .addConverterFactory(JsonApiConverterFactory.create(moshi))
                .build()

            return client.create(PoiApi::class.java)
        }
    }
}

@JsonApi(type = "regionalPrices")
class RegionalPriceResponse : Resource() {
    var lower: Double? = null
    var upper: Double? = null
    var average: Double? = null
    var currency: String? = null

    fun getFuelType(): FuelType? {
        return FuelType.fromValue(id)
    }
}
