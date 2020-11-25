package cloud.pace.sdk.poikit.poi.download

import android.os.Parcelable
import cloud.pace.sdk.poikit.utils.ApiUtils
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

class GasStationApiClient(environment: Environment, apiKey: String) {
    private var api = GasStationApi.create(environment.apiUrl, apiKey)

    fun getGasStation(id: String, compileOpeningHours: Boolean, forMovedGasStation: Boolean, completion: (Completion<GasStationMovedResponse>) -> Unit) {
        api.getGasStation(id, compileOpeningHours).enqueue {
            onResponse = {
                when (it.code()) {
                    STATUS_MOVED -> {
                        val newUuid: String? = it.headers().values(HEADER_LOCATION).first()?.split("/")?.last()
                        if (newUuid.isNotNullOrEmpty()) {
                            completion(Success(GasStationMovedResponse(newUuid, true, null, null)))
                        } else {
                            completion(Success(GasStationMovedResponse(null, true, null, null)))
                        }
                    }
                    STATUS_OK -> {
                        val priorResponse = it.raw().priorResponse()
                        if (priorResponse != null) {
                            val newUuid = priorResponse.headers().values(HEADER_LOCATION).first()?.split("/")?.last()
                            if (newUuid.isNotNullOrEmpty()) {
                                completion(Success(GasStationMovedResponse(newUuid, true, null, null)))
                            } else {
                                completion(Success(GasStationMovedResponse(null, false, null, null)))
                            }
                        } else {
                            if (forMovedGasStation)
                                completion(
                                    Success(
                                        GasStationMovedResponse(
                                            null, true, it.body()?.apiGasStation?.attributes?.latitude,
                                            it.body()?.apiGasStation?.attributes?.longitude
                                        )
                                    )
                                )
                            else
                                completion(Success(GasStationMovedResponse(null, false, null, null)))
                        }
                    }
                    STATUS_NOT_FOUND -> {
                        completion(Success(GasStationMovedResponse(null, true, null, null)))
                    }
                    else -> {
                        completion(Failure(Exception("Server error")))
                    }
                }
            }
            onFailure = {
                completion(Failure(Exception("Unknown exception")))
            }
        }
    }

    companion object {
        const val STATUS_OK = 200
        const val STATUS_MOVED = 301
        const val STATUS_NOT_FOUND = 404

        const val HEADER_LOCATION = "location"
    }
}

interface GasStationApi {
    @GET("poi/beta/gas-stations/{id}")
    fun getGasStation(
        @Path("id") id: String,
        @Query("compile[openingHours]") compileOpeningHours: Boolean
    ): Call<GasStationApiResponse>

    companion object Factory {
        fun create(baseUrl: String, apiKey: String): GasStationApi {
            val retrofit: Retrofit = Retrofit.Builder()
                .client(
                    OkHttpClient.Builder()
                        .addInterceptor {
                            it.proceed(
                                it.request()
                                    .newBuilder()
                                    .header(ApiUtils.USER_AGENT_HEADER, ApiUtils.getUserAgent())
                                    .header(ApiUtils.API_KEY, apiKey)
                                    .build()
                            )
                        }
                        .build()
                )
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(baseUrl)
                .build()

            return retrofit.create(GasStationApi::class.java)
        }
    }
}

data class GasStationApiResponse(
    @SerializedName("data")
    var apiGasStation: ApiGasStation
)

@Parcelize
data class ApiGasStation(
    @SerializedName("id")
    var id: String,
    @SerializedName("attributes")
    var attributes: GasStationAttributes
) : Parcelable

@Parcelize
data class GasStationAttributes(
    @SerializedName("latitude")
    var latitude: Double,
    @SerializedName("longitude")
    var longitude: Double
) : Parcelable

data class GasStationMovedResponse(
    var id: String?,
    var hasChanged: Boolean,
    var latitude: Double?,
    var longitude: Double?
)
