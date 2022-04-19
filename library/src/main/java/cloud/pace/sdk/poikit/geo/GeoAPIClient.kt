package cloud.pace.sdk.poikit.geo

import android.content.Context
import cloud.pace.sdk.PACECloudSDK
import cloud.pace.sdk.api.utils.InterceptorUtils
import cloud.pace.sdk.utils.Environment
import cloud.pace.sdk.utils.handleCallback
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapters.PolymorphicJsonAdapterFactory
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.Cache
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.HeaderMap
import retrofit2.http.Path
import java.util.concurrent.TimeUnit

interface GeoAPI {

    @GET("geo/2021-1/apps/{apiName}.geojson")
    fun getGeoApiApps(
        @Path("apiName") apiName: String,
        @HeaderMap headers: Map<String, String>,
    ): Call<GeoAPIResponse>
}

class GeoAPIClient(environment: Environment, private val context: Context) {

    private val service = create(environment.apiUrl)

    fun getGeoApiApps(completion: (Result<GeoAPIResponse>) -> Unit) {
        service.getGeoApiApps(
            PACECloudSDK.configuration.geoAppsScope,
            InterceptorUtils.getHeaders(false, "application/geo+json", "application/geo+json")
        ).handleCallback(completion)
    }

    private fun create(baseUrl: String, readTimeout: Long? = null): GeoAPI {
        val client = OkHttpClient.Builder()
            .cache(Cache(context.cacheDir, CACHE_SIZE))
            .addInterceptor(InterceptorUtils.getInterceptor())

        if (readTimeout != null)
            client.readTimeout(readTimeout, TimeUnit.SECONDS)

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
