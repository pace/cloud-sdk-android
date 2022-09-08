package cloud.pace.sdk.poikit.geo

import android.content.Context
import cloud.pace.sdk.PACECloudSDK
import cloud.pace.sdk.api.request.BaseRequest
import cloud.pace.sdk.utils.Environment
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapters.PolymorphicJsonAdapterFactory
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.Cache
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.HeaderMap
import retrofit2.http.Path

interface GeoAPI {

    @GET("geo/2021-1/apps/{apiName}.geojson")
    suspend fun getGeoApiApps(
        @Path("apiName") apiName: String,
        @HeaderMap headers: Map<String, String>,
    ): Response<GeoAPIResponse>
}

class GeoAPIClient(environment: Environment, private val context: Context) : BaseRequest() {

    private val service = create(environment.cdnUrl)

    suspend fun getGeoApiApps(): Response<GeoAPIResponse> {
        return service.getGeoApiApps(PACECloudSDK.configuration.geoAppsScope, headers(false, "application/geo+json", "application/geo+json"))
    }

    private fun create(baseUrl: String, readTimeout: Long? = null): GeoAPI {
        val client = okHttpClient(readTimeout = readTimeout)
            .newBuilder()
            .cache(Cache(context.cacheDir, CACHE_SIZE))

        return Retrofit.Builder()
            .client(client.build())
            .baseUrl(baseUrl)
            .addConverterFactory(
                MoshiConverterFactory.create(
                    Moshi.Builder()
                        .add(
                            PolymorphicJsonAdapterFactory.of(Geometry::class.java, "type")
                                .withSubtype(Polygon::class.java, POLYGON_NAME)
                                .withSubtype(Point::class.java, POINT_NAME)
                                .withSubtype(GeometryCollection::class.java, GEOMETRY_COLLECTION_NAME)
                        )
                        .add(KotlinJsonAdapterFactory())
                        .build()
                )
            )
            .build()
            .create(GeoAPI::class.java)
    }

    companion object {
        private const val CACHE_SIZE = 1024 * 1024 * 100L // at most 100 MB
    }
}
