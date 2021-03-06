package cloud.pace.sdk.appkit

import android.content.Context
import android.location.Location
import cloud.pace.sdk.PACECloudSDK
import cloud.pace.sdk.api.geo.GeoGasStation
import cloud.pace.sdk.api.poi.generated.model.LocationBasedAppWithRefs
import cloud.pace.sdk.api.poi.generated.model.LocationBasedAppsWithRefs
import cloud.pace.sdk.appkit.app.api.AppRepositoryImpl
import cloud.pace.sdk.appkit.model.App
import cloud.pace.sdk.appkit.model.AppManifest
import cloud.pace.sdk.appkit.utils.TestAppAPI
import cloud.pace.sdk.appkit.utils.TestCacheModel
import cloud.pace.sdk.appkit.utils.TestGeoAPIManager
import cloud.pace.sdk.appkit.utils.TestUriUtils
import cloud.pace.sdk.utils.CompletableFutureCompat
import cloud.pace.sdk.utils.Configuration
import cloud.pace.sdk.utils.Environment
import junit.framework.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import java.util.concurrent.TimeUnit

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

    private val appApi = object : TestAppAPI() {
        override fun getLocationBasedApps(latitude: Double, longitude: Double, completion: (Result<LocationBasedAppsWithRefs>) -> Unit) {
            if (latitude == locationWithApp.latitude && longitude == locationWithApp.longitude) {
                completion(Result.success(listOf(locationBasedApp)))
            } else {
                completion(Result.success(emptyList()))
            }
        }
    }

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
        PACECloudSDK.configuration = Configuration("", "", "", "", environment = Environment.DEVELOPMENT)
    }

    @Test
    fun `get local available apps`() {
        val appsFuture = CompletableFutureCompat<List<App>>()
        appRepository.getLocationBasedApps(mock(Context::class.java), locationWithApp.latitude, locationWithApp.longitude) {
            it.onSuccess { appsFuture.complete(it) }
            it.onFailure { throw it }
        }

        val apps = appsFuture.get(2, TimeUnit.SECONDS)
        assertEquals(1, apps.size)
        val app = apps.get(0)
        assertEquals(manifest.name, app.name)
        assertEquals(manifest.description, app.description)
        assertEquals(manifest.backgroundColor, app.iconBackgroundColor)
        assertEquals(startUrl, app.url)
        assertNull(app.logo)
    }

    @Test
    fun `no local apps available`() {
        val appsFuture = CompletableFutureCompat<List<App>>()
        appRepository.getLocationBasedApps(mock(Context::class.java), 40.0, 7.0) {
            it.onSuccess { appsFuture.complete(it) }
            it.onFailure { throw it }
        }

        val apps = appsFuture.get(2, TimeUnit.SECONDS)
        assertEquals(0, apps.size)
    }

    @Test
    fun `do not return app when manifest is missing`() {
        val cacheModel = object : TestCacheModel() {
            override fun getManifest(context: Context, url: String, completion: (Result<AppManifest>) -> Unit) {
                completion(Result.failure(Exception()))
            }
        }

        val appRepository = AppRepositoryImpl(context, cacheModel, appApi, uriUtil, geoApiManager)
        val appsFuture = CompletableFutureCompat<List<App>>()
        appRepository.getLocationBasedApps(mock(Context::class.java), locationWithApp.latitude, locationWithApp.longitude) {
            it.onSuccess { appsFuture.complete(it) }
            it.onFailure { throw it }
        }

        val apps = appsFuture.get(2, TimeUnit.SECONDS)
        assertEquals(0, apps.size)
    }

    @Test
    fun `pass error when something failed`() {
        val geoApiManager = object : TestGeoAPIManager() {
            override fun apps(latitude: Double, longitude: Double, completion: (Result<List<GeoGasStation>>) -> Unit) {
                completion(Result.failure(RuntimeException()))
            }
        }

        val appApi = object : TestAppAPI() {
            override fun getLocationBasedApps(latitude: Double, longitude: Double, completion: (Result<LocationBasedAppsWithRefs>) -> Unit) {
                completion(Result.failure(RuntimeException()))
            }
        }
        val appRepository = AppRepositoryImpl(context, cache, appApi, uriUtil, geoApiManager)
        val exceptionFuture = CompletableFutureCompat<Throwable?>()
        appRepository.getLocationBasedApps(mock(Context::class.java), locationWithApp.latitude, locationWithApp.longitude) {
            it.onSuccess { exceptionFuture.complete(null) }
            it.onFailure { exceptionFuture.complete(it) }
        }

        assertNotNull(exceptionFuture.get(2, TimeUnit.SECONDS))
    }

    @Test
    fun `return two apps when one app references two pois`() {
        val id1 = "c34a78bc-de0a-4daa-9f5e-8cc7103cf55e"
        val id2 = "2069125c-b65b-4514-81f9-3d09779b175f"
        val locationBasedApp = LocationBasedAppWithRefs().apply {
            pwaUrl = urlLocationBasedApp
            references = listOf(id1, id2)
        }
        val appApi = object : TestAppAPI() {
            override fun getLocationBasedApps(latitude: Double, longitude: Double, completion: (Result<LocationBasedAppsWithRefs>) -> Unit) {
                if (latitude == locationWithApp.latitude && longitude == locationWithApp.longitude) {
                    completion(Result.success(listOf(locationBasedApp)))
                } else {
                    completion(Result.success(emptyList()))
                }
            }
        }
        val uriUtil = object : TestUriUtils() {
            override fun getStartUrls(baseUrl: String, manifestUrl: String, sdkStartUrl: String?, references: List<String>?): Map<String?, String> {
                return mapOf(
                    id1 to "$startUrl/?references=$id1",
                    id2 to "$startUrl/?references=$id2"
                )
            }
        }
        val appRepository = AppRepositoryImpl(context, cache, appApi, uriUtil, geoApiManager)
        val appsFuture = CompletableFutureCompat<List<App>>()
        appRepository.getLocationBasedApps(mock(Context::class.java), locationWithApp.latitude, locationWithApp.longitude) {
            it.onSuccess { appsFuture.complete(it) }
            it.onFailure { throw it }
        }

        val apps = appsFuture.get(2, TimeUnit.SECONDS)
        assertEquals(2, apps.size)

        val app1 = apps.get(0)
        assertEquals(manifest.name, app1.name)
        assertEquals(manifest.description, app1.description)
        assertEquals(manifest.backgroundColor, app1.iconBackgroundColor)
        assertEquals(id1, app1.poiId)
        assertEquals("$startUrl/?references=$id1", app1.url)
        assertNull(app1.logo)

        val app2 = apps.get(1)
        assertEquals(manifest.name, app2.name)
        assertEquals(manifest.description, app2.description)
        assertEquals(manifest.backgroundColor, app2.iconBackgroundColor)
        assertEquals(id2, app2.poiId)
        assertEquals("$startUrl/?references=$id2", app2.url)
        assertNull(app2.logo)
    }
}
