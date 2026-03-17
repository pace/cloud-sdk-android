package car.pace.cofu.data.interceptor

import android.os.LocaleList
import cloud.pace.sdk.api.request.BaseRequest
import okhttp3.Headers.Companion.toHeaders
import okhttp3.Interceptor
import okhttp3.Response

class ApiTokenInterceptor(private val parameters: Map<String, String>? = null) : Interceptor, BaseRequest() {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        val acceptLanguage = LocaleList.getDefault().toLanguageTags()
        val additionalParameters = parameters?.toMutableMap() ?: mutableMapOf()
        additionalParameters["Accept-Language"] = acceptLanguage

        val acceptHeader = request.header("accept")
        val headers = headers(
            true,
            "application/json",
            acceptHeader ?: "application/json",
            additionalParameters
        ).toHeaders()

        val newRequest = request.newBuilder().headers(headers).build()
        return chain.proceed(newRequest)
    }
}
