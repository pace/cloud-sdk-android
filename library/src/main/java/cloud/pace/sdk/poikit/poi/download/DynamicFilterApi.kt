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
import retrofit2.http.Query

class DynamicFilterApiClient(environment: Environment, apiKey: String) {
    private var api = FilterApi.create(environment.apiUrl, apiKey)

    fun getDynamicFilters(lat: Double, lon: Double, completion: (Completion<DynamicFilterResponse?>) -> Unit) {
        api.dynamicFilters(lat, lon).enqueue {
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

interface FilterApi {
    @GET("poi/beta/meta")
    fun dynamicFilters(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double
    ): Call<DynamicFilterResponse>

    companion object Factory {
        fun create(baseUrl: String, apiKey: String): FilterApi {
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

            return retrofit.create(FilterApi::class.java)
        }
    }
}

data class DynamicFilterResponse(
    @SerializedName("data")
    var dynamicFilters: List<DynamicFilter>
)

@Parcelize
data class DynamicFilter(
    @SerializedName("type")
    var type: String,
    @SerializedName("attributes")
    var filterAttribute: FilterAttribute
) : Parcelable

@Parcelize
data class FilterAttribute(
    @SerializedName("field")
    var field: String,
    @SerializedName("fieldName")
    var fieldName: String,
    @SerializedName("available")
    var availableFilters: List<String>,
    @SerializedName("unavailable")
    var unavailableFilters: List<String>?
) : Parcelable
