package cloud.pace.sdk.appkit

import androidx.test.runner.AndroidJUnit4
import cloud.pace.sdk.PACECloudSDK
import cloud.pace.sdk.appkit.app.api.UriManagerImpl
import cloud.pace.sdk.appkit.app.api.UriManagerImpl.Companion.PARAM_R
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
    fun oneStartUrl() {
        val baseUrl = "https://app.test.net"
        val id = "e3211b77-03f0-4d49-83aa-4adaa46d95ae"
        val references = listOf(id)

        val urls = uriManager.getStartUrls(baseUrl, references)
        assertEquals(mapOf(id to "$baseUrl?$PARAM_R=$id"), urls)
    }

    @Test
    fun multipleStartUrls() {
        val baseUrl = "https://app.test.net"
        val id1 = "e3211b77-03f0-4d49-83aa-4adaa46d95ae"
        val id2 = "ed82a1d5-edd3-4bd7-9ad1-f1a501f23555"
        val references = listOf(id1, id2)

        val urls = uriManager.getStartUrls(baseUrl, references)
        assertEquals(mapOf(id1 to "$baseUrl?$PARAM_R=$id1", id2 to "$baseUrl?$PARAM_R=$id2"), urls)
    }

    @Test
    fun startUrlWithReference() {
        val baseUrl = "https://app.test.net"
        val id = "e3211b77-03f0-4d49-83aa-4adaa46d95ae"

        val url = uriManager.getStartUrl(baseUrl, id)
        assertEquals("$baseUrl?$PARAM_R=$id", url)
    }

    @Test
    fun startUrlIsBaseUrl() {
        val baseUrl = "https://app.test.net"

        val url = uriManager.getStartUrl(baseUrl, null)
        assertEquals(baseUrl, url)
    }

    @Test
    fun getIconUrlRelativePath() {
        val baseUrl = "https://app.test.net"
        val iconUrl = "notification_logo.png"

        val url = uriManager.getIconUrl(baseUrl, iconUrl)
        assertEquals("https://app.test.net/notification_logo.png", url)
    }

    @Test
    fun getIconUrlAbsolutePath() {
        val baseUrl = "https://app.test.net"
        val iconUrl = "https://cdn.pace.cloud/brands/mybrand/notification_logo.png"

        val url = uriManager.getIconUrl(baseUrl, iconUrl)
        assertEquals(iconUrl, url)
    }
}
