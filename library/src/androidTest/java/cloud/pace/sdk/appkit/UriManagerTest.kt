package cloud.pace.sdk.appkit

import androidx.test.runner.AndroidJUnit4
import cloud.pace.sdk.PACECloudSDK
import cloud.pace.sdk.appkit.app.api.UriManagerImpl
import cloud.pace.sdk.utils.Configuration
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
        PACECloudSDK.configuration = Configuration("", "", "", "", environment = Environment.DEVELOPMENT, oidConfiguration = null)
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
}
