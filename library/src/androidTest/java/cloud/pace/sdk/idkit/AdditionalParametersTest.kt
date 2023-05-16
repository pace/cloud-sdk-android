package cloud.pace.sdk.idkit

import androidx.test.annotation.UiThreadTest
import androidx.test.platform.app.InstrumentationRegistry
import cloud.pace.sdk.PACECloudSDK
import cloud.pace.sdk.appkit.persistence.SharedPreferencesImpl
import cloud.pace.sdk.appkit.persistence.SharedPreferencesModel
import cloud.pace.sdk.idkit.authorization.SessionHolder
import cloud.pace.sdk.idkit.model.CustomOIDConfiguration
import cloud.pace.sdk.utils.Configuration
import cloud.pace.sdk.utils.Environment
import cloud.pace.sdk.utils.MigrationHelper
import io.mockk.mockk
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.KoinTest
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

@UiThreadTest
class AdditionalParametersTest : KoinTest {

    @get:Rule
    var folder = TemporaryFolder()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val clientAppName = "PACECloudSDKTest"
    private val defaultParams = mapOf("utm_source" to clientAppName)

    @Before
    fun setup() {
        val module = module {
            single {
                context
            }

            single {
                mockk<SessionHolder>(relaxed = true)
            }

            single<SharedPreferencesModel> {
                SharedPreferencesImpl(get(), get())
            }

            single {
                MigrationHelper(get(), get())
            }
        }

        startKoin {
            modules(module)
        }
    }

    @After
    fun reset() {
        stopKoin()

        PACECloudSDK.additionalQueryParams = emptyMap()
        IDKit.setAdditionalParameters(null)
    }

    private fun setupSDK(sdkParams: Map<String, String>? = null, idKitParams: Map<String, String>? = null) {
        if (sdkParams != null) {
            PACECloudSDK.additionalQueryParams = sdkParams
        }

        PACECloudSDK.setup(
            context,
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
    fun defaultParamsAreAlwaysSetInSDKAndIDKit() {
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
    fun customParamsOfSDKAreSetInSDKAndIDKit() {
        val additionalParameters = mapOf("param1" to "value1", "param2" to "value2")
        setupSDK(sdkParams = additionalParameters)

        assertEquals(additionalParameters + defaultParams, PACECloudSDK.additionalQueryParams)
        assertEquals(additionalParameters + defaultParams, IDKit.getAdditionalParameters())
        assertEquals(PACECloudSDK.additionalQueryParams, IDKit.getAdditionalParameters())
    }

    @Test
    fun customParamsAreOnlySetInIDKitAndNotInSDK() {
        val additionalParameters = mapOf("param1" to "value1", "param2" to "value2")
        setupSDK(idKitParams = additionalParameters)

        assertEquals(defaultParams, PACECloudSDK.additionalQueryParams)
        assertEquals(additionalParameters + defaultParams, IDKit.getAdditionalParameters())
        assertNotEquals(PACECloudSDK.additionalQueryParams, IDKit.getAdditionalParameters())
    }

    @Test
    fun defaultParamIsOverwrittenWithCustomValueInSDKAndIDKit() {
        val additionalParameters = mapOf("param1" to "value1", "utm_source" to "bar")
        setupSDK(sdkParams = additionalParameters)

        assertEquals(additionalParameters, PACECloudSDK.additionalQueryParams)
        assertEquals(additionalParameters, IDKit.getAdditionalParameters())
        assertEquals(PACECloudSDK.additionalQueryParams, IDKit.getAdditionalParameters())
    }

    @Test
    fun sdkParamsHavePrecedenceOverIDKitParams() {
        val sdkParams = mapOf("param1" to "newValue", "param2" to "value2")
        val idKitParams = mapOf("param1" to "value1", "utm_source" to "bar")
        setupSDK(sdkParams, idKitParams)

        assertEquals(sdkParams + defaultParams, PACECloudSDK.additionalQueryParams)
        assertEquals(sdkParams + defaultParams, IDKit.getAdditionalParameters())
        assertEquals(PACECloudSDK.additionalQueryParams, IDKit.getAdditionalParameters())
    }

    @Test
    fun paramsAreOverwrittenAfterSetupInSDKAndIDKit() {
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
    fun idKitParamsAreOverwrittenAfterSetupOnlyInIDKit() {
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
