package cloud.pace.sdk.api

import cloud.pace.sdk.api.utils.InterceptorUtils.AUTHORIZATION_HEADER
import cloud.pace.sdk.utils.Environment

object API {

    var environment: Environment = Environment.PRODUCTION
    val baseUrl: String
        get() = environment.apiUrl
    var apiKey: String = ""
    var additionalHeaders: Map<String, String> = emptyMap()
        private set

    fun setup(environment: Environment, apiKey: String, additionalHeaders: Map<String, String> = emptyMap()) {
        API.environment = environment
        API.apiKey = apiKey
        API.additionalHeaders = additionalHeaders
    }

    fun addHeader(key: String, value: String) {
        val currentHeaders = additionalHeaders.toMutableMap()
        currentHeaders[key] = value
        additionalHeaders = currentHeaders
    }

    fun removeHeader(key: String) {
        val currentHeaders = additionalHeaders.toMutableMap()
        currentHeaders.remove(key)
        additionalHeaders = currentHeaders
    }

    fun addAuthorizationHeader(accessToken: String?) {
        if (accessToken == null) {
            removeHeader(AUTHORIZATION_HEADER)
        } else {
            addHeader(AUTHORIZATION_HEADER, "Bearer $accessToken")
        }
    }
}
