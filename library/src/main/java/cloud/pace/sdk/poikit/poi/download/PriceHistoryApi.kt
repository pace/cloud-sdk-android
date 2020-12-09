package cloud.pace.sdk.poikit.poi.download

import android.os.Parcelable
import cloud.pace.sdk.poikit.utils.ApiException
import cloud.pace.sdk.utils.*
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import java.util.*

class PriceHistoryApiClient(environment: Environment, apiKey: String) {
    private var api = PriceHistoryApi.create(environment.apiUrl, apiKey)

    fun getPriceHistory(id: String, fuelType: String, from: Date, to: Date, completion: (Completion<PriceHistoryApiResponse?>) -> Unit) {
        val formattedFrom = from.toIso8601().dropLast(9) + 'Z'
        val formattedTo = to.toIso8601().dropLast(9) + 'Z'
        api.priceHistory(id, fuelType, formattedFrom, formattedTo).enqueue {
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

interface PriceHistoryApi {
    @GET("poi/beta/gas-stations/{id}/fuel-price-histories/{fuel-type}")
    fun priceHistory(
        @Path("id") id: String,
        @Path("fuel-type") fuelType: String,
        @Query("filter[from]") from: String,
        @Query("filter[to]") to: String
    ): Call<PriceHistoryApiResponse>

    companion object Factory {
        fun create(baseUrl: String, apiKey: String): PriceHistoryApi {
            val retrofit: Retrofit = Retrofit.Builder()
                .client(
                    OkHttpClient.Builder()
                        .addInterceptor {
                            it.proceed(
                                it.request()
                                    .newBuilder()
                                    .header(ApiUtils.USER_AGENT_HEADER, ApiUtils.getUserAgent())
                                    .header(ApiUtils.API_KEY_HEADER, apiKey)
                                    .build()
                            )
                        }
                        .build()
                )
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(baseUrl)
                .build()

            return retrofit.create(PriceHistoryApi::class.java)
        }
    }
}

data class PriceHistoryApiResponse(
    @SerializedName("data")
    var data: ApiPriceHistory
)

@Parcelize
data class ApiPriceHistory(
    @SerializedName("type")
    var type: String,
    @SerializedName("id")
    var id: String,
    @SerializedName("attributes")
    var attributes: PriceHistoryAttributes
) : Parcelable

@Parcelize
data class PriceHistoryAttributes(
    @SerializedName("currency")
    var currency: String,
    @SerializedName("from")
    var from: String,
    @SerializedName("fuelPrices")
    var priceData: List<PriceHistoryFuelPrices>,
    @SerializedName("productName")
    var productName: String,
    @SerializedName("to")
    var to: String
) : Parcelable

@Parcelize
data class PriceHistoryFuelPrices(
    @SerializedName("at")
    var date: Date,
    @SerializedName("price")
    var price: Double
) : Parcelable
