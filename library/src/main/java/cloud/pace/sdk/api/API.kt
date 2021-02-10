package cloud.pace.sdk.api

import cloud.pace.sdk.utils.Environment

object API {

    var baseUrl: String = Environment.PRODUCTION.apiUrl
    var apiKey: String = ""
    var additionalHeaders: Map<String, String> = emptyMap()

    fun setup(baseUrl: String, apiKey: String, additionalHeaders: Map<String, String> = emptyMap()) {
        API.baseUrl = baseUrl
        API.apiKey = apiKey
        API.additionalHeaders = additionalHeaders
    }
}
