package cloud.pace.sdk.poikit.search

import cloud.pace.sdk.utils.ApiUtils
import cloud.pace.sdk.utils.Environment
import io.reactivex.rxjava3.core.Observable
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

internal class AddressSearchClient(environment: Environment, apiKey: String) {
    private val api = AddressSearchApi.create(environment.searchBaseUrl, apiKey)

    fun searchAddress(request: AddressSearchRequest): Observable<PhotonResult> {
        val osmTag = ArrayList(request.includeKeys?.map { it } ?: listOf())
        osmTag.addAll(request.excludeValues?.map { ":!$it" } ?: listOf())

        val supportedLanguages = listOf("de", "en", "it", "fr")
        val acceptLanguages = request.acceptLanguages
            ?.filter { supportedLanguages.contains(it) }
            ?: arrayListOf()

        return api.search(
            languages = if (acceptLanguages.isEmpty()) null else acceptLanguages.toTypedArray(),
            query = request.text,
            latitude = request.locationBias?.latitude,
            longitude = request.locationBias?.longitude,
            osmTag = if (osmTag.isEmpty()) null else osmTag.toTypedArray(),
            limit = request.limit
        )
    }
}

interface AddressSearchApi {
    @GET("api")
    fun search(
        @Query("lang") languages: Array<String>?,
        @Query("q") query: String,
        @Query("lat") latitude: Double?,
        @Query("lon") longitude: Double?,
        @Query("osm_tag") osmTag: Array<String>?,
        @Query("limit") limit: Int?
    ): Observable<PhotonResult>

    companion object Factory {
        fun create(baseUrl: String, apiKey: String): AddressSearchApi {
            val loggingInterceptor = HttpLoggingInterceptor()
            loggingInterceptor.level = HttpLoggingInterceptor.Level.BASIC

            val retrofit = Retrofit.Builder()
                .client(
                    OkHttpClient.Builder()
                        .addInterceptor(loggingInterceptor)
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
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                .build()

            return retrofit.create(AddressSearchApi::class.java)
        }
    }
}
