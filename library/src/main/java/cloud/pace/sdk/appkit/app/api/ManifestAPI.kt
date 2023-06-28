package cloud.pace.sdk.appkit.app.api

import android.content.Context
import cloud.pace.sdk.api.request.BaseRequest
import cloud.pace.sdk.api.utils.RequestUtils.ACCEPT_LANGUAGE_HEADER
import cloud.pace.sdk.appkit.model.AppManifest
import okhttp3.Cache
import okhttp3.OkHttpClient
import retrofit2.http.GET
import retrofit2.http.HeaderMap
import java.util.Locale

interface ManifestAPI {

    @GET("manifest.json")
    suspend fun getManifest(
        @HeaderMap headers: Map<String, String>
    ): AppManifest
}

class ManifestClient(private val context: Context) : BaseRequest() {

    override fun okHttpClient(additionalParameters: Map<String, String>?, readTimeout: Long?): OkHttpClient {
        return super.okHttpClient(additionalParameters, readTimeout)
            .newBuilder()
            .cache(Cache(context.cacheDir, CACHE_SIZE))
            .build()
    }

    suspend fun getManifest(baseUrl: String): AppManifest {
        val headers = headers(false, "application/json", "application/json", mapOf(ACCEPT_LANGUAGE_HEADER to Locale.getDefault().language))

        return retrofit(baseUrl)
            .create(ManifestAPI::class.java)
            .getManifest(headers)
    }

    companion object {
        private const val CACHE_SIZE = 1024 * 1024 * 50L // at most 50 MB
    }
}
