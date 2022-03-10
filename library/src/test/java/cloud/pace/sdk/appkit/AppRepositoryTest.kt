package cloud.pace.sdk.appkit

import android.content.Context
import android.location.Location
import cloud.pace.sdk.PACECloudSDK
import cloud.pace.sdk.api.poi.generated.model.LocationBasedAppWithRefs
import cloud.pace.sdk.appkit.app.api.AppRepositoryImpl
import cloud.pace.sdk.appkit.model.AppManifest
import cloud.pace.sdk.appkit.utils.TestAppAPI
import cloud.pace.sdk.appkit.utils.TestCacheModel
import cloud.pace.sdk.appkit.utils.TestGeoAPIManager
import cloud.pace.sdk.appkit.utils.TestUriUtils
import cloud.pace.sdk.poikit.geo.GeoGasStation
import cloud.pace.sdk.utils.Configuration
import cloud.pace.sdk.utils.Environment
import cloud.pace.sdk.utils.Failure
import cloud.pace.sdk.utils.Success
import junit.framework.Assert.*
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock

class AppRepositoryTest {

    private val context = mock(Context::class.java)
    private val locationWithApp = Location("").also {
        it.latitude = 48.0
        it.longitude = 8.0
    }
    private val id = "ed82a1d5-edd3-4bd7-9ad1-f1a501f23555"
    private val urlLocationBasedApp = "https://app.test"
    private val startUrl = "https://app.test.start"
    private val locationBasedApp = LocationBasedAppWithRefs().apply { pwaUrl = urlLocationBasedApp }
    private val manifest = AppManifest(
        name = "Tanke Emma",
        shortName = "Connected Fueling",
        description = "Jetzt tanken",
        startUrl = "",
        sdkStartUrl = "",
        display = "",
        icons = arrayOf(),
        backgroundColor = "",
        themeColor = "",
        textColor = ""
    )

    private val appApi = TestAppAPI()

    private val uriUtil = object : TestUriUtils() {
        override fun getStartUrls(baseUrl: String, manifestUrl: String, sdkStartUrl: String?, references: List<String>?): Map<String?, String> {
            return mapOf(null to startUrl)
        }
    }

    private val cache = object : TestCacheModel() {
        override fun getManifest(context: Context, url: String, completion: (Result<AppManifest>) -> Unit) {
            if (url == urlLocationBasedApp) completion(Result.success(manifest))
        }
    }

    private val geoApiManager = object : TestGeoAPIManager() {
        override fun apps(latitude: Double, longitude: Double, completion: (Result<List<GeoGasStation>>) -> Unit) {
            if (latitude == locationWithApp.latitude && longitude == locationWithApp.longitude) {
                completion(Result.success(listOf(GeoGasStation(id, listOf(urlLocationBasedApp)))))
            } else {
                completion(Result.success(emptyList()))
            }
        }
    }

    private val appRepository = AppRepositoryImpl(context, cache, appApi, uriUtil, geoApiManager)

    @Before
    fun init() {
        PACECloudSDK.configuration = Configuration("", "", "", "", environment = Environment.DEVELOPMENT, oidConfiguration = null)
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
        assertNull(app.logo)
    }

    @Test
    fun `no local apps available`() = runBlocking {
        val apps = appRepository.getLocationBasedApps(40.0, 7.0)
        assertEquals(0, (apps as Success).result.size)
    }

    @Test
    fun `do not return app when manifest is missing`() = runBlocking {
        val cacheModel = object : TestCacheModel() {
            override fun getManifest(context: Context, url: String, completion: (Result<AppManifest>) -> Unit) {
                completion(Result.failure(Exception()))
            }
        }

        val appRepository = AppRepositoryImpl(context, cacheModel, appApi, uriUtil, geoApiManager)
        val apps = appRepository.getLocationBasedApps(locationWithApp.latitude, locationWithApp.longitude)
        assertEquals(0, (apps as Success).result.size)
    }

    @Test
    fun `pass error when something failed`() = runBlocking {
        val geoApiManager = object : TestGeoAPIManager() {
            override fun apps(latitude: Double, longitude: Double, completion: (Result<List<GeoGasStation>>) -> Unit) {
                completion(Result.failure(RuntimeException()))
            }
        }
        val appRepository = AppRepositoryImpl(context, cache, appApi, uriUtil, geoApiManager)
        val exception = appRepository.getLocationBasedApps(locationWithApp.latitude, locationWithApp.longitude)
        assertTrue((exception as Failure).throwable is RuntimeException)
    }
}
