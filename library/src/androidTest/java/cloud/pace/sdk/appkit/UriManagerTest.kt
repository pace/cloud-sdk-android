package cloud.pace.sdk.appkit

import androidx.test.runner.AndroidJUnit4
import cloud.pace.sdk.appkit.app.api.UriManagerImpl
import cloud.pace.sdk.appkit.app.api.UriManagerImpl.Companion.PARAM_R
import cloud.pace.sdk.appkit.app.api.UriManagerImpl.Companion.PARAM_REFERENCES
import cloud.pace.sdk.appkit.model.Configuration
import cloud.pace.sdk.utils.Environment
import junit.framework.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UriManagerTest {

    private val uriManager = UriManagerImpl()

    @Before
    fun init() {
        AppKit.configuration = Configuration("", "", "", "", false, environment = Environment.DEVELOPMENT)
    }

    @Test
    fun getUrlWithManifestLocation() {
        val baseUrl = "https://app.test.net"
        val manifestUrl = "https://app.test.net/manifest"
        val sdkStartUrl = ".startUrl"
        val references = listOf<String>()

        val urls = uriManager.getStartUrls(baseUrl, manifestUrl, sdkStartUrl, references)
        assertEquals(mapOf(null to "https://app.test.net/manifest/startUrl"), urls)
    }

    @Test
    fun baseUrlContainsTrailingSplash() {
        val baseUrl = "https://app.test.net/"
        val manifestUrl = ""
        val sdkStartUrl = "/startUrl"
        val references = listOf<String>()

        val urls = uriManager.getStartUrls(baseUrl, manifestUrl, sdkStartUrl, references)
        assertEquals(mapOf(null to "https://app.test.net/startUrl"), urls)
    }

    @Test
    fun startUrl() {
        val baseUrl = "https://app.test.net/"
        val manifestUrl = ""
        val sdkStartUrl = "https://app.start.url.net/"
        val references = listOf<String>()

        val urls = uriManager.getStartUrls(baseUrl, manifestUrl, sdkStartUrl, references)
        assertEquals(mapOf(null to sdkStartUrl), urls)
    }

    @Test
    fun urnParser() {
        val id1 = "c34a78bc-de0a-4daa-9f5e-8cc7103cf55e"
        val id2 = "2069125c-b65b-4514-81f9-3d09779b175f"
        val baseUrl = "https://app.test.net/"
        val manifestUrl = ""
        val sdkStartUrl = "/?{$PARAM_REFERENCES}"
        val references = listOf("prn:poi:gas-stations:$id1", "fV§d:ds2%$:$id2")

        val urls = uriManager.getStartUrls(baseUrl, manifestUrl, sdkStartUrl, references)
        assertEquals(1, urls.size)
        assertEquals(mapOf(id1 to "$baseUrl?$PARAM_R=prn%3Apoi%3Agas-stations%3A$id1"), urls)
    }
}
