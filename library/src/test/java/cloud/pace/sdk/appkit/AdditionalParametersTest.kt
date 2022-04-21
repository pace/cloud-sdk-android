package cloud.pace.sdk.appkit

import android.content.Context
import android.os.Build
import androidx.preference.PreferenceManager
import cloud.pace.sdk.PACECloudSDK
import cloud.pace.sdk.idkit.IDKit
import cloud.pace.sdk.idkit.model.CustomOIDConfiguration
import cloud.pace.sdk.utils.Configuration
import cloud.pace.sdk.utils.Environment
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.mockito.Mockito.`when`
import org.mockito.kotlin.mock
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.O_MR1])
class AdditionalParametersTest {

    @get:Rule
    var folder = TemporaryFolder()

    private val mockContext = mock<Context>()
    private val clientAppName = "PACECloudSDKTest"
    private val defaultParams = mapOf("utm_source" to clientAppName)

    @Before
    fun setup() {
        val mockCache = folder.newFolder("mockCache")
        `when`(mockContext.cacheDir).thenReturn(mockCache)
        `when`(mockContext.packageManager).thenReturn(mock())
        `when`(PreferenceManager.getDefaultSharedPreferences(mockContext)).thenReturn(mock())
    }

    @After
    fun reset() {
        PACECloudSDK.additionalQueryParams = emptyMap()
        IDKit.setAdditionalParameters(null)
    }

    private fun setupSDK(sdkParams: Map<String, String>? = null, idKitParams: Map<String, String>? = null) {
        if (sdkParams != null) {
            PACECloudSDK.additionalQueryParams = sdkParams
        }

        PACECloudSDK.setup(
            mockContext,
            Configuration(
                clientAppName = clientAppName,
                clientAppVersion = "1",
                clientAppBuild = "1",
                apiKey = "YOUR_API_KEY",
                environment = Environment.DEVELOPMENT,
                oidConfiguration = CustomOIDConfiguration(clientId = "cloud-sdk-example-app", redirectUri = "cloud-sdk-example://callback", additionalParameters = idKitParams)
            )
        )
    }

    @Test
    fun `default params are always set in sdk and idkit`() {
        setupSDK()

        assertEquals(defaultParams, PACECloudSDK.additionalQueryParams)
        assertEquals(defaultParams, IDKit.getAdditionalParameters())
        assertEquals(PACECloudSDK.additionalQueryParams, IDKit.getAdditionalParameters())

        PACECloudSDK.additionalQueryParams = emptyMap()

        assertEquals(defaultParams, PACECloudSDK.additionalQueryParams)
        assertEquals(defaultParams, IDKit.getAdditionalParameters())
        assertEquals(PACECloudSDK.additionalQueryParams, IDKit.getAdditionalParameters())

        IDKit.setAdditionalParameters(null)

        assertEquals(defaultParams, PACECloudSDK.additionalQueryParams)
        assertEquals(defaultParams, IDKit.getAdditionalParameters())
        assertEquals(PACECloudSDK.additionalQueryParams, IDKit.getAdditionalParameters())
    }

    @Test
    fun `custom params of sdk are set in sdk and idkit`() {
        val additionalParameters = mapOf("param1" to "value1", "param2" to "value2")
        setupSDK(sdkParams = additionalParameters)

        assertEquals(additionalParameters + defaultParams, PACECloudSDK.additionalQueryParams)
        assertEquals(additionalParameters + defaultParams, IDKit.getAdditionalParameters())
        assertEquals(PACECloudSDK.additionalQueryParams, IDKit.getAdditionalParameters())
    }

    @Test
    fun `custom params are only set in idkit and not in sdk`() {
        val additionalParameters = mapOf("param1" to "value1", "param2" to "value2")
        setupSDK(idKitParams = additionalParameters)

        assertEquals(defaultParams, PACECloudSDK.additionalQueryParams)
        assertEquals(additionalParameters + defaultParams, IDKit.getAdditionalParameters())
        assertNotEquals(PACECloudSDK.additionalQueryParams, IDKit.getAdditionalParameters())
    }

    @Test
    fun `default param is overwritten with custom value in sdk and idkit`() {
        val additionalParameters = mapOf("param1" to "value1", "utm_source" to "bar")
        setupSDK(sdkParams = additionalParameters)

        assertEquals(additionalParameters, PACECloudSDK.additionalQueryParams)
        assertEquals(additionalParameters, IDKit.getAdditionalParameters())
        assertEquals(PACECloudSDK.additionalQueryParams, IDKit.getAdditionalParameters())
    }

    @Test
    fun `sdk params have precedence over idkit params`() {
        val sdkParams = mapOf("param1" to "newValue", "param2" to "value2")
        val idKitParams = mapOf("param1" to "value1", "utm_source" to "bar")
        setupSDK(sdkParams, idKitParams)

        assertEquals(sdkParams + defaultParams, PACECloudSDK.additionalQueryParams)
        assertEquals(sdkParams + defaultParams, IDKit.getAdditionalParameters())
        assertEquals(PACECloudSDK.additionalQueryParams, IDKit.getAdditionalParameters())
    }

    @Test
    fun `params are overwritten after setup in sdk and idkit`() {
        val sdkParams = mapOf("param1" to "value1", "param2" to "value2")
        val idKitParams = mapOf("param3" to "value3")
        setupSDK(sdkParams, idKitParams)

        assertEquals(sdkParams + defaultParams, PACECloudSDK.additionalQueryParams)
        assertEquals(idKitParams + sdkParams + defaultParams, IDKit.getAdditionalParameters())
        assertNotEquals(PACECloudSDK.additionalQueryParams, IDKit.getAdditionalParameters())

        val newSdkParams = mapOf("utm_source" to "bar", "param4" to "value4")
        PACECloudSDK.additionalQueryParams = newSdkParams

        val newIdKitParams = mapOf("param5" to "value5")
        IDKit.setAdditionalParameters(newIdKitParams)

        assertEquals(newSdkParams, PACECloudSDK.additionalQueryParams)
        assertEquals(newIdKitParams + newSdkParams, IDKit.getAdditionalParameters())
        assertNotEquals(PACECloudSDK.additionalQueryParams, IDKit.getAdditionalParameters())
    }

    @Test
    fun `idkit params are overwritten after setup only in idkit`() {
        val sdkParams = mapOf("param1" to "value1", "param2" to "value2")
        val idKitParams = mapOf("param3" to "value3")
        setupSDK(sdkParams, idKitParams)

        val newIdKitParams = mapOf("param5" to "value5")
        IDKit.setAdditionalParameters(newIdKitParams)

        assertEquals(sdkParams + defaultParams, PACECloudSDK.additionalQueryParams)
        assertEquals(newIdKitParams + sdkParams + defaultParams, IDKit.getAdditionalParameters())
        assertNotEquals(PACECloudSDK.additionalQueryParams, IDKit.getAdditionalParameters())
    }
}
