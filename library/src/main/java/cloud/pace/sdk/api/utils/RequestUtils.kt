package cloud.pace.sdk.api.utils

import android.net.Uri
import android.os.Build
import cloud.pace.sdk.BuildConfig
import cloud.pace.sdk.PACECloudSDK
import cloud.pace.sdk.utils.DeviceUtils
import cloud.pace.sdk.utils.randomHexString
import java.util.Locale
import java.util.concurrent.TimeUnit

object RequestUtils {

    const val USER_AGENT_HEADER = "User-Agent"
    const val ACCEPT_HEADER = "Accept"
    const val CONTENT_TYPE_HEADER = "Content-Type"
    const val API_KEY_HEADER = "API-Key"
    const val UBER_TRACE_ID_HEADER = "uber-trace-id"
    const val REQUEST_ID_HEADER = "request-id"
    const val AUTHORIZATION_HEADER = "Authorization"
    const val BEARER = "Bearer"

    private const val TRACING_SPAN_ID = "0053444B"
    private const val TRACING_PARENT_SPAN_ID = "0"
    private const val TRACING_FLAGS = "01"

    private var traceId: Pair<String, Long>? = null
    private var traceIdMaxAge = TimeUnit.MINUTES.toMillis(15)

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
