package cloud.pace.sdk.api.interceptor

import cloud.pace.sdk.PACECloudSDK
import cloud.pace.sdk.utils.requestId
import okhttp3.Interceptor
import okhttp3.Response
import timber.log.Timber

class QueryParametersInterceptor(private val additionalParameters: Map<String, String>? = null) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val url = chain.request().url.newBuilder()

        // Additional query parameters of the request have priority over the globally set query parameters, so set them last to override any existing query parameter
        val queryParams = if (additionalParameters != null) PACECloudSDK.additionalQueryParams + additionalParameters else PACECloudSDK.additionalQueryParams
        queryParams.forEach { param ->
            url.addQueryParameter(param.key, param.value)
        }

        val newRequest = chain.request().newBuilder().url(url.build()).build()
        val response = chain.proceed(newRequest)

        if (!response.isSuccessful) {
            Timber.e("Request failed: code = ${response.code} || message = ${response.message} || request ID = ${response.requestId} || url: ${newRequest.url}")
        }

        return response
    }
}
