package cloud.pace.sdk.appkit

import android.content.Context
import android.location.Location
import cloud.pace.sdk.PACECloudSDK
import cloud.pace.sdk.appkit.app.api.AppRepositoryImpl
import cloud.pace.sdk.appkit.app.api.ManifestClient
import cloud.pace.sdk.appkit.app.api.UriManagerImpl.Companion.PARAM_R
import cloud.pace.sdk.appkit.model.AppManifest
import cloud.pace.sdk.appkit.utils.TestAppAPI
import cloud.pace.sdk.appkit.utils.TestGeoAPIManager
import cloud.pace.sdk.appkit.utils.TestUriUtils
import cloud.pace.sdk.poikit.geo.CofuGasStation
import cloud.pace.sdk.poikit.geo.ConnectedFuelingStatus
import cloud.pace.sdk.poikit.geo.GeoAPIManagerImpl.Companion.FUELING_TYPE
import cloud.pace.sdk.poikit.geo.GeoGasStation
import cloud.pace.sdk.utils.Configuration
import cloud.pace.sdk.utils.Environment
import cloud.pace.sdk.utils.Failure
import cloud.pace.sdk.utils.Success
import cloud.pace.sdk.utils.URL
import com.google.android.gms.maps.model.LatLng
import io.mockk.coEvery
import io.mockk.mockk
import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertNull
import junit.framework.Assert.assertTrue
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.util.concurrent.CompletableFuture

class AppRepositoryTest {

    private val context = mockk<Context>()
    private val locationWithApp = Location("").also {
        it.latitude = 48.0
        it.longitude = 8.0
    }
    private val id = "ed82a1d5-edd3-4bd7-9ad1-f1a501f23555"
    private val urlLocationBasedApp = "https://app.test"
    private val startUrl = "https://app.test.start"
    private val manifest = AppManifest(
        name = "Tanke Emma",
        shortName = "Connected Fueling",
        description = "Jetzt tanken",
        startUrl = "",
        sdkStartUrl = "",
        display = "",
        icons = emptyList(),
        backgroundColor = "",
        themeColor = "",
        textColor = ""
    )

    private val appApi = TestAppAPI()

    private val uriUtil = object : TestUriUtils() {
        override fun getStartUrls(baseUrl: String, references: List<String>?): Map<String?, String> {
            return mapOf(null to startUrl)
        }

        override fun getStartUrl(baseUrl: String, reference: String?): String {
            return if (reference != null) "$baseUrl?$PARAM_R=$reference" else baseUrl
        }
    }

    private val geoApiManager = object : TestGeoAPIManager() {
        override suspend fun apps(latitude: Double, longitude: Double): Result<List<GeoGasStation>> {
            return if (latitude == locationWithApp.latitude && longitude == locationWithApp.longitude) {
                Result.success(listOf(GeoGasStation(id, mapOf(FUELING_TYPE to listOf(urlLocationBasedApp)), LatLng(0.0, 0.0))))
            } else {
                Result.success(emptyList())
            }
        }
    }

    private val manifestClient = mockk<ManifestClient>().also {
        coEvery { it.getManifest(urlLocationBasedApp) } returns manifest
    }

    private val appRepository = AppRepositoryImpl(context, appApi, uriUtil, geoApiManager, manifestClient)

    @Before
    fun init() {
        PACECloudSDK.configuration = Configuration("", "", "", "", "", environment = Environment.DEVELOPMENT, oidConfiguration = null)
    }

    @Test
    fun `get local available apps`() = runBlocking {
        val apps = (appRepository.getLocationBasedApps(locationWithApp.latitude, locationWithApp.longitude) as Success).result
        assertEquals(1, apps.size)

        val app = apps[0]
        assertEquals(manifest.name, app.name)
        assertEquals(manifest.description, app.description)
        assertEquals(manifest.backgroundColor, app.iconBackgroundColor)
        assertEquals(startUrl, app.url)
        assertNull(app.iconUrl)
    }

    @Test
    fun `no local apps available`() = runBlocking {
        val apps = appRepository.getLocationBasedApps(40.0, 7.0)
        assertEquals(0, (apps as Success).result.size)
    }

    @Test
    fun `also return app when manifest is missing`() = runBlocking {
        coEvery { manifestClient.getManifest(any()) } throws Exception()

        val apps = appRepository.getLocationBasedApps(locationWithApp.latitude, locationWithApp.longitude)
        assertEquals(1, (apps as Success).result.size)
    }

    @Test
    fun `pass error when something failed`() = runBlocking {
        val geoApiManager = object : TestGeoAPIManager() {
            override suspend fun apps(latitude: Double, longitude: Double): Result<List<GeoGasStation>> {
                return Result.failure(RuntimeException())
            }
        }
        val appRepository = AppRepositoryImpl(context, appApi, uriUtil, geoApiManager, manifestClient)
        val exception = appRepository.getLocationBasedApps(locationWithApp.latitude, locationWithApp.longitude)
        assertTrue((exception as Failure).throwable is RuntimeException)
    }

    @Test
    fun `return default fueling url when fueling url is not in geojson`() = runBlocking {
        val geoApiManager = object : TestGeoAPIManager() {
            override fun cofuGasStations(completion: (Result<List<CofuGasStation>>) -> Unit) {
                completion(Result.success(listOf(CofuGasStation(id, LatLng(52.5563160654065, 13.4150576591492), ConnectedFuelingStatus.ONLINE, emptyMap()))))
            }
        }
        val appRepository = AppRepositoryImpl(context, appApi, uriUtil, geoApiManager, manifestClient)

        val future = CompletableFuture<String>()
        appRepository.getFuelingUrl(id) {
            future.complete(it)
        }

        Assert.assertEquals("${URL.fueling}?$PARAM_R=$id", future.get())
    }

    @Test
    fun `return default fueling url when geojson fails`() = runBlocking {
        val geoApiManager = object : TestGeoAPIManager() {
            override fun cofuGasStations(completion: (Result<List<CofuGasStation>>) -> Unit) {
                completion(Result.failure(RuntimeException()))
            }
        }
        val appRepository = AppRepositoryImpl(context, appApi, uriUtil, geoApiManager, manifestClient)

        val future = CompletableFuture<String>()
        appRepository.getFuelingUrl(id) {
            future.complete(it)
        }

        Assert.assertEquals("${URL.fueling}?$PARAM_R=$id", future.get())
    }
}
