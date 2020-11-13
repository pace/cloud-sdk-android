package cloud.pace.sdk.appkit.app.api

import android.net.Uri
import cloud.pace.sdk.utils.isUrn
import cloud.pace.sdk.utils.resourceUuid
import java.util.regex.Pattern

interface UriManager {

    fun getStartUrls(baseUrl: String, manifestUrl: String, sdkStartUrl: String?, references: List<String>?): Map<String?, String>
    fun getURI(baseUrl: String, parameters: Map<String, String>): String
    fun buildUrl(baseUrl: String, path: String): String
}

class UriManagerImpl : UriManager {

    override fun getStartUrls(baseUrl: String, manifestUrl: String, sdkStartUrl: String?, references: List<String>?): Map<String?, String> {
        val validBaseUrl = baseUrl.replace("/$".toRegex(), "")
        val validManifestUrl = manifestUrl.replace("/$".toRegex(), "")

        val urlString = when {
            sdkStartUrl == null -> validBaseUrl
            sdkStartUrl.matches(Pattern.compile("^/.*").toRegex()) -> "$validBaseUrl$sdkStartUrl"
            sdkStartUrl.matches(Pattern.compile("^\\..*").toRegex()) -> "$validManifestUrl/${sdkStartUrl.substring(1)}"
            else -> sdkStartUrl
        }

        val url = Uri.parse(urlString)
        val parameters = url.queryParameterNames.filter { it.contains(PARAM_REFERENCES) }

        if (parameters.isNotEmpty()) {
            val urls = references
                ?.filter {
                    it.isUrn()
                }
                ?.map {
                    val builder = url.buildUpon().clearQuery().appendQueryParameter(PARAM_R, it)
                    it.resourceUuid to builder.build().toString()
                }

            if (!urls.isNullOrEmpty()) {
                return urls.toMap()
            }
        }

        return mapOf(null to url.buildUpon().clearQuery().build().toString())
    }

    override fun buildUrl(baseUrl: String, path: String): String {
        val baseUri = Uri.parse(baseUrl).buildUpon()
        return baseUri.appendPath(path).build().toString()
    }

    override fun getURI(baseUrl: String, parameters: Map<String, String>): String {
        val uriBuilder = Uri.parse(baseUrl).buildUpon()
        for (parameter in parameters) {
            uriBuilder.appendQueryParameter(parameter.key, parameter.value)
        }
        return uriBuilder.build().toString()
    }

    companion object {
        const val PARAM_REFERENCES = "REFERENCES"
        const val PARAM_R = "r"
    }
}
