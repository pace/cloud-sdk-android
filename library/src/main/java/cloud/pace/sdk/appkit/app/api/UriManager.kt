package cloud.pace.sdk.appkit.app.api

import android.net.Uri
import cloud.pace.sdk.utils.resourceUuid

interface UriManager {

    fun getStartUrls(baseUrl: String, references: List<String>?): Map<String?, String>
    fun getStartUrl(baseUrl: String, reference: String?): String
    fun appendPath(baseUrl: String, path: String): String
    fun appendQueryParameter(baseUrl: String, key: String, value: String): String
}

class UriManagerImpl : UriManager {

    override fun getStartUrls(baseUrl: String, references: List<String>?): Map<String?, String> {
        if (references.isNullOrEmpty()) {
            // Return baseUrl without reference ID
            return mapOf(null to getStartUrl(baseUrl, null))
        }

        return references.associate {
            // Return map of reference ID to URL with reference parameter
            it.resourceUuid to getStartUrl(baseUrl, it)
        }
    }

    override fun getStartUrl(baseUrl: String, reference: String?): String {
        return if (reference != null) {
            appendQueryParameter(baseUrl, PARAM_R, reference)
        } else {
            baseUrl
        }
    }

    override fun appendPath(baseUrl: String, path: String) =
        Uri.parse(baseUrl).buildUpon().appendPath(path).build().toString()

    override fun appendQueryParameter(baseUrl: String, key: String, value: String) =
        Uri.parse(baseUrl).buildUpon().clearQuery().appendQueryParameter(key, value).build().toString()

    companion object {
        const val PARAM_R = "r"
    }
}
