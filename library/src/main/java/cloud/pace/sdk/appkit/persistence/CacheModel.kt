package cloud.pace.sdk.appkit.persistence

import android.content.Context
import android.net.Uri
import cloud.pace.sdk.api.utils.InterceptorUtils
import cloud.pace.sdk.api.utils.InterceptorUtils.UBER_TRACE_ID_HEADER
import cloud.pace.sdk.appkit.AppKit
import cloud.pace.sdk.appkit.model.AppManifest
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import java.io.FileNotFoundException
import java.io.IOException
import java.net.MalformedURLException
import java.net.URL
import java.nio.charset.Charset
import java.util.*

interface CacheModel {

    fun getUri(context: Context, url: String, completion: (Result<ByteArray>) -> Unit)
    fun getManifest(context: Context, url: String, completion: (Result<AppManifest>) -> Unit)
}

class CacheModelImpl : CacheModel {

    override fun getUri(context: Context, url: String, completion: (Result<ByteArray>) -> Unit) {
        try {
            fetch(context, URL(url), AppKit.userAgent, completion)
        } catch (e: MalformedURLException) {
            completion(Result.failure(e))
        }
    }

    private fun fetch(context: Context, url: URL, userAgent: String?, completion: (Result<ByteArray>) -> Unit) {
        val headers = mutableMapOf<String, String>()
        headers[ACCEPT_LANGUAGE_KEY] = Locale.getDefault().language
        headers[UBER_TRACE_ID_HEADER] = InterceptorUtils.getUberTraceId()
        if (userAgent != null) {
            headers[USER_AGENT_KEY] = userAgent
        }

        val request = Request.Builder()
            .url(url)
            .headers(Headers.of(headers))
            .build()

        OkHttpClient.Builder()
            .cache(Cache(context.cacheDir, CACHE_SIZE_BYTES))
            .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.HEADERS))
            .build()
            .newCall(request).enqueue(object : Callback {
                override fun onResponse(call: Call, response: Response) {
                    val data = response.body()?.bytes()
                    if (data != null) {
                        completion(Result.success(data))
                    } else {
                        completion(Result.failure(Exception("Request returned with no content")))
                    }
                }

                override fun onFailure(call: Call, e: IOException) {
                    completion(Result.failure(e))
                }
            })
    }

    override fun getManifest(context: Context, url: String, completion: (Result<AppManifest>) -> Unit) {
        val fullUri = Uri.parse(url)
        val baseUrl = "${fullUri.scheme}://${fullUri.host}"
        val manifestUrl = Uri.parse(baseUrl).buildUpon().appendPath(MANIFEST_FILE_NAME).build().toString()
        getUri(context, manifestUrl) { result ->
            result.onSuccess {
                try {
                    val json = String(it, Charset.defaultCharset())
                    val manifest = Gson().fromJson(json, AppManifest::class.java)
                    completion(Result.success(manifest))
                } catch (e: JsonSyntaxException) {
                    completion(Result.failure(e))
                } catch (e: FileNotFoundException) {
                    completion(Result.failure(e))
                }
            }

            result.onFailure {
                completion(Result.failure(it))
            }
        }
    }

    companion object {
        private const val ACCEPT_LANGUAGE_KEY = "Accept-Language"
        private const val USER_AGENT_KEY = "User-Agent"
        private const val MANIFEST_FILE_NAME = "manifest.json"
        private const val CACHE_SIZE_BYTES = 1024 * 1024 * 2L
    }
}
