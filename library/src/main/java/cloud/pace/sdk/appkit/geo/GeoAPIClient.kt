package cloud.pace.sdk.appkit.geo

import android.content.Context
import cloud.pace.sdk.PACECloudSDK
import cloud.pace.sdk.api.utils.InterceptorUtils
import cloud.pace.sdk.poikit.utils.ApiException
import cloud.pace.sdk.utils.Environment
import cloud.pace.sdk.utils.enqueue
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.Cache
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path

interface GeoAPI {

    @GET("geo/2021-1/apps/{apiName}.geojson")
    fun getGeoApiApps(@Path("apiName") apiName: String): Call<GeoAPIResponse>
}

class GeoAPIClient(environment: Environment, private val context: Context) {

    private val service = create(environment.apiUrl)

    fun getGeoApiApps(completion: (Result<GeoAPIResponse>) -> Unit) {
        service.getGeoApiApps(PACECloudSDK.configuration.geoAppsScope).enqueue {
            onResponse = {
                val body = it.body()
                if (it.isSuccessful && body != null) {
                    completion(Result.success(body))
                } else {
                    completion(Result.failure(ApiException(it.code(), it.message())))
                }
            }

            onFailure = {
                completion(Result.failure(it ?: Exception("Unknown exception")))
            }
        }
    }

    private fun create(baseUrl: String): GeoAPI {
        return Retrofit.Builder()
            .client(
                OkHttpClient.Builder()
                    .cache(Cache(context.cacheDir, CACHE_SIZE))
                    .addInterceptor(InterceptorUtils.getInterceptor("application/geo+json", "application/geo+json"))
                    .build()
            )
            .baseUrl(baseUrl)
            .addConverterFactory(
                MoshiConverterFactory.create(
                    Moshi.Builder()
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
