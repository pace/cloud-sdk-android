package cloud.pace.sdk.api.utils

import android.net.Uri
import android.os.Build
import cloud.pace.sdk.BuildConfig
import cloud.pace.sdk.PACECloudSDK
import cloud.pace.sdk.api.API
import cloud.pace.sdk.idkit.IDKit
import cloud.pace.sdk.utils.DeviceUtils
import cloud.pace.sdk.utils.randomHexString
import cloud.pace.sdk.utils.requestId
import cloud.pace.sdk.utils.resume
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine
import net.openid.appauth.AuthorizationException.GeneralErrors.NETWORK_ERROR
import net.openid.appauth.AuthorizationException.GeneralErrors.SERVER_ERROR
import okhttp3.Interceptor
import timber.log.Timber
import java.net.HttpURLConnection
import java.util.*
import java.util.concurrent.TimeUnit

object InterceptorUtils {

    const val USER_AGENT_HEADER = "User-Agent"
    const val ACCEPT_HEADER = "Accept"
    const val CONTENT_TYPE_HEADER = "Content-Type"
    const val API_KEY_HEADER = "API-Key"
    const val UBER_TRACE_ID_HEADER = "uber-trace-id"
    const val AUTHORIZATION_HEADER = "Authorization"
    const val REQUEST_ID_HEADER = "request-id"

    private const val TRACING_SPAN_ID = "0053444B"
    private const val TRACING_PARENT_SPAN_ID = "0"
    private const val TRACING_FLAGS = "01"

    private var traceId: Pair<String, Long>? = null
    private var traceIdMaxAge = TimeUnit.MINUTES.toMillis(15)

    fun getHeaders(isAuthorizationRequired: Boolean, contentTypeHeader: String, acceptHeader: String? = null, additionalHeaders: Map<String, String>? = null): Map<String, String> {
        val headers = API.additionalHeaders.toMutableMap()
        if (!isAuthorizationRequired) {
            // Only send the authorization header when authorization is required
            headers.remove(AUTHORIZATION_HEADER)
        }

        if (acceptHeader != null) headers[ACCEPT_HEADER] = acceptHeader
        headers[CONTENT_TYPE_HEADER] = contentTypeHeader
        headers[API_KEY_HEADER] = API.apiKey
        headers[UBER_TRACE_ID_HEADER] = getUberTraceId()
        headers[USER_AGENT_HEADER] = getUserAgent()

        // Additional headers of the request have priority over the globally set headers, so set them last to override any existing header
        if (!additionalHeaders.isNullOrEmpty()) {
            headers += additionalHeaders
        }

        return headers
    }

    fun getQueryParameters(additionalParams: Map<String, String>? = null): Map<String, String> {
        // Additional query parameters of the request have priority over the globally set query parameters, so set them last to override any existing query parameter
        return if (additionalParams != null) PACECloudSDK.additionalQueryParams + additionalParams else PACECloudSDK.additionalQueryParams
    }

    fun getInterceptor() = Interceptor {
        val response = it.proceed(it.request())
        if (!response.isSuccessful) {
            Timber.e("Request failed: code = ${response.code} || message = ${response.message} || request ID = ${response.requestId} || url: ${it.request().url}")
        }

        if (response.code == HttpURLConnection.HTTP_UNAUTHORIZED && IDKit.isInitialized && IDKit.isAuthorizationValid()) {
            val oldToken = IDKit.cachedToken()
            // Make sure that the token is only refreshed once for multiple requests
            synchronized(this) {
                try {
                    val cachedToken = IDKit.cachedToken()
                    val newToken = if (oldToken == cachedToken) {
                        runBlocking {
                            getNewToken()
                        }
                    } else {
                        // Token has already been refreshed from another request. Use the cached token
                        cachedToken
                    }

                    if (newToken != null) {
                        // Close previous response body
                        response.body?.close()
                        it.proceed(it.request().newBuilder().header(AUTHORIZATION_HEADER, "Bearer $newToken").build())
                    } else {
                        response
                    }
                } catch (e: Exception) {
                    if (e == NETWORK_ERROR || e == SERVER_ERROR) {
                        response.newBuilder().code(HttpURLConnection.HTTP_UNAVAILABLE).build()
                    } else {
                        response
                    }
                }
            }
        } else {
            response
        }
    }

    private suspend fun getNewToken() = suspendCancellableCoroutine<String?> { continuation ->
        IDKit.refreshToken {
            continuation.resume(it)
        }
    }

    fun getUrlWithQueryParams(url: String): String {
        val newUrl = Uri.parse(url).buildUpon()
        PACECloudSDK.additionalQueryParams.forEach {
            newUrl.appendQueryParameter(it.key, it.value)
        }

        return newUrl.build().toString()
    }

    fun getUserAgent(versionName: String = BuildConfig.VERSION_NAME, versionCode: Int = BuildConfig.VERSION_CODE): String {
        return "PACECloudSDK/$versionName.$versionCode (${DeviceUtils.getDeviceName()}; Android/${Build.VERSION.RELEASE})"
    }

    internal fun getUberTraceId() = "${getTraceId()}:$TRACING_SPAN_ID:$TRACING_PARENT_SPAN_ID:$TRACING_FLAGS"

    internal fun getTraceId() = traceId?.let { (id, time) ->
        if (System.currentTimeMillis() - time > traceIdMaxAge) {
            newTraceId()
        } else {
            traceId = id to System.currentTimeMillis()
            id
        }
    } ?: newTraceId()

    private fun newTraceId() = String.randomHexString(8).toUpperCase(Locale.ROOT).also {
        traceId = it to System.currentTimeMillis()
    }
}
