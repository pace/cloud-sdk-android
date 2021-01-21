package cloud.pace.sdk.poikit.utils

import cloud.pace.sdk.PACECloudSDK
import cloud.pace.sdk.api.API
import okhttp3.Interceptor

object InterceptorUtils {

    fun getInterceptor(accept: String?, contentType: String?): Interceptor {
        return Interceptor {
            val httpUrl = it.request().url().newBuilder()
            httpUrl.addQueryParameter("utm_partner_client", PACECloudSDK.configuration.clientAppName)
            PACECloudSDK.additionalQueryParams.forEach {
                httpUrl.addQueryParameter(it.key, it.value)
            }

            val builder = it.request()
                .newBuilder()
                .url(httpUrl.build())
                .header("API-Key", API.apiKey)

            if (accept != null) builder.header("Accept", accept)
            if (contentType != null) builder.header("Content-Type", contentType)

            API.additionalHeaders.forEach { header ->
                builder.header(header.key, header.value)
            }

            it.proceed(builder.build())
        }
    }
}
