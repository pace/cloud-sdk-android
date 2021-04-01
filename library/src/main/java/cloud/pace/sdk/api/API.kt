package cloud.pace.sdk.api

import cloud.pace.sdk.utils.ApiUtils.AUTHORIZATION_HEADER
import cloud.pace.sdk.utils.Environment

object API {

    var baseUrl: String = Environment.PRODUCTION.apiUrl
    var apiKey: String = ""
    var additionalHeaders: Map<String, String> = emptyMap()
        private set

    fun setup(baseUrl: String, apiKey: String, additionalHeaders: Map<String, String> = emptyMap()) {
        API.baseUrl = baseUrl
        API.apiKey = apiKey
        API.additionalHeaders = additionalHeaders
    }

    fun addHeader(key: String, value: String) {
        val currentHeaders = additionalHeaders.toMutableMap()
        currentHeaders[key] = value
        additionalHeaders = currentHeaders
    }

    fun addAuthorizationHeader(accessToken: String) {
        addHeader(AUTHORIZATION_HEADER, "Bearer $accessToken")
    }
}
