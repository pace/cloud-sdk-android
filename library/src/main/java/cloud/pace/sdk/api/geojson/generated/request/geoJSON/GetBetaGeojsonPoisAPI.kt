/*
 * PLEASE DO NOT EDIT!
 *
 * Generated by SwagGen with Kotlin template.
 * https://github.com/pace/SwagGen
 */

package cloud.pace.sdk.api.geojson.generated.request.geoJSON

import cloud.pace.sdk.api.geojson.GeoJSONAPI
import cloud.pace.sdk.api.geojson.generated.model.GeoJson
import cloud.pace.sdk.api.utils.EnumConverterFactory
import cloud.pace.sdk.api.utils.InterceptorUtils
import com.google.gson.annotations.SerializedName
import com.squareup.moshi.Json
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapters.Rfc3339DateJsonAdapter
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.HeaderMap
import retrofit2.http.Query
import java.util.*
import java.util.concurrent.TimeUnit

object GetBetaGeojsonPoisAPI {

    interface GetBetaGeojsonPoisService {
        /* GeoJSON */
        /* get a GeoJSON representation of POIs */
        @GET("beta/geojson/pois")
        fun getBetaGeojsonPois(
            @HeaderMap headers: Map<String, String>,
            /** Comma separated list of fields. Selects additional fields to be returned. The requested fields will be shown in the `properties` attribute of each `GeoJsonFeature`.
            Possible values are:
             * type
             * address
             * stationName
             * brand
             * dkvStationID */
            @Query("fields[gasStation]") fieldsgasStation: String? = null,
            /** Only show POIs of the given type */
            @Query("filter[poiType]") filterpoiType: FilterpoiType? = null,
            /** Filter results based on available online payment methods. Use a comma separated list to get stations for multiple payment method. */
            @Query("filter[onlinePaymentMethod]") filteronlinePaymentMethod: String? = null,
            /** Filter results based on merchant name. Value has to be the same as provided in the merchant field. */
            @Query("filter[merchant]") filtermerchant: String? = null,
            /** Select a country to query. If this parameter is not provided data of all countries is returned. Country code in ISO 3166-1 alpha-2 format */
            @Query("filter[country]") filtercountry: String? = null,
            /** If set, the request will only return POIs which have PACE Connected Fueling activated.
            To activate the filter, use one of the following values (all other values will result in the filter being ignored):
             * true
             * yes
             * y
             * 1
             * on
             */
            @Query("filter[connectedFueling]") filterconnectedFueling: String? = null,
            /** If set, the request will only return POIs that allow the payment method dkvAppAndGo.
            To activate the filter, use one of the following values (all other values will result in the filter being ignored):
             * true
             * yes
             * y
             * 1
             * on
             */
            @Query("filter[dkvAppAndGo]") filterdkvAppAndGo: String? = null
        ): Call<GeoJson>
    }

    /* Only show POIs of the given type */
    enum class FilterpoiType(val value: String) {
        @SerializedName("GasStation")
        @Json(name = "GasStation")
        GASSTATION("GasStation"),

        @SerializedName("SpeedCamera")
        @Json(name = "SpeedCamera")
        SPEEDCAMERA("SpeedCamera")
    }

    fun GeoJSONAPI.GeoJSONAPI.getBetaGeojsonPois(fieldsgasStation: String? = null, filterpoiType: FilterpoiType? = null, filteronlinePaymentMethod: String? = null, filtermerchant: String? = null, filtercountry: String? = null, filterconnectedFueling: String? = null, filterdkvAppAndGo: String? = null, readTimeout: Long? = null, additionalHeaders: Map<String, String>? = null, additionalParameters: Map<String, String>? = null): Call<GeoJson> {
        val client = OkHttpClient.Builder().addInterceptor(InterceptorUtils.getInterceptor(additionalParameters))
        val headers = InterceptorUtils.getHeaders(false, "application/json", "application/json", additionalHeaders)

        if (readTimeout != null) {
            client.readTimeout(readTimeout, TimeUnit.SECONDS)
        }

        val service: GetBetaGeojsonPoisService =
            Retrofit.Builder()
                .client(client.build())
                .baseUrl(GeoJSONAPI.baseUrl)
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
                .create(GetBetaGeojsonPoisService::class.java)

        return service.getBetaGeojsonPois(headers, fieldsgasStation, filterpoiType, filteronlinePaymentMethod, filtermerchant, filtercountry, filterconnectedFueling, filterdkvAppAndGo)
    }
}
