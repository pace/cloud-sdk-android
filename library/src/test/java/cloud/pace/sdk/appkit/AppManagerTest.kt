package cloud.pace.sdk.appkit

import android.content.Context
import android.location.Location
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import cloud.pace.sdk.PACECloudSDK
import cloud.pace.sdk.api.geo.GeoAPIFeature
import cloud.pace.sdk.api.geo.GeoGasStation
import cloud.pace.sdk.api.geo.Polygon
import cloud.pace.sdk.appkit.app.api.AppAPI
import cloud.pace.sdk.appkit.app.api.AppRepository
import cloud.pace.sdk.appkit.app.api.AppRepositoryImpl
import cloud.pace.sdk.appkit.app.api.UriManager
import cloud.pace.sdk.appkit.communication.AppEventManager
import cloud.pace.sdk.appkit.communication.AppModel
import cloud.pace.sdk.appkit.communication.AppModelImpl
import cloud.pace.sdk.appkit.geo.GeoAPIManager
import cloud.pace.sdk.appkit.model.App
import cloud.pace.sdk.appkit.persistence.CacheModel
import cloud.pace.sdk.appkit.persistence.SharedPreferencesModel
import cloud.pace.sdk.appkit.utils.*
import cloud.pace.sdk.utils.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.koin.core.module.Module
import org.koin.dsl.module
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner
import java.util.concurrent.CompletableFuture

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class AppManagerTest : CloudSDKKoinComponent {

    @get:Rule
    var coroutineTestRule = CoroutineTestRule()

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @Mock
    private lateinit var mockContext: Context

    @Mock
    private lateinit var mockLocation: Location

    private val appManager = AppManager(coroutineTestRule.testDispatcherProvider)
    private val polygon = listOf(
        listOf(8.427429, 49.01304015764206),
        listOf(8.427166935031618, 49.013005967255644),
        listOf(8.426944768026093, 49.012908601401435),
        listOf(8.426796322288334, 49.01276288345869),
        listOf(8.426744196943954, 49.01259099797384),
        listOf(8.426796326657078, 49.01241911308242),
        listOf(8.42694477420443, 49.0122733965724),
        listOf(8.427166939400362, 49.0121760321509),
        listOf(8.427429, 49.012141842357934),
        listOf(8.427691060599638, 49.0121760321509),
        listOf(8.42791322579557, 49.0122733965724),
        listOf(8.42806167334292, 49.01241911308242),
        listOf(8.428113803056045, 49.01259099797384),
        listOf(8.428061677711664, 49.01276288345869),
        listOf(8.427913231973907, 49.012908601401435),
        listOf(8.427691064968382, 49.013005967255644),
        listOf(8.427429, 49.01304015764206)
    )

    @Before
    fun init() {
        `when`(mockLocation.latitude).then { 49.012722 }
        `when`(mockLocation.longitude).then { 8.427326 }
        `when`(mockLocation.speed).then { 3f }

        PACECloudSDK.configuration = Configuration("", "", "", "", environment = Environment.DEVELOPMENT)
    }

    @After
    fun onFinished() = stopKoin()

    @Test
    fun `no app due to invalid speed`() {
        `when`(mockLocation.speed).then { 20f }

        val testModule = module {
            single<LocationProvider> {
                TestLocationProvider(mockLocation)
            }
        }

        setupKoinForTests(testModule)

        val future = CompletableFuture<Throwable>()
        appManager.requestLocalApps {
            if (it is Failure) {
                future.complete(it.throwable)
            }
        }

        assertEquals(InvalidSpeed, future.get())
    }

    @Test
    fun `no app due to missing location permissions`() {
        val testModule = module {
            single<LocationProvider> {
                TestLocationProvider(throwable = PermissionDenied)
            }
        }

        setupKoinForTests(testModule)

        val future = CompletableFuture<Throwable>()
        appManager.requestLocalApps {
            if (it is Failure) {
                future.complete(it.throwable)
            }
        }

        assertEquals(PermissionDenied, future.get())
    }

    @Test
    fun `no app due to missing location`() {
        val testModule = module {
            single<LocationProvider> {
                TestLocationProvider(throwable = NoLocationFound)
            }
        }

        setupKoinForTests(testModule)

        val future = CompletableFuture<Throwable>()
        appManager.requestLocalApps {
            if (it is Failure) {
                future.complete(it.throwable)
            }
        }

        assertEquals(NoLocationFound, future.get())
    }

    @Test
    fun `no app due to network error`() {
        val appRepository = object : TestAppRepository() {
            override fun getLocationBasedApps(context: Context, latitude: Double, longitude: Double, completion: (Result<List<App>>) -> Unit) {
                completion(Result.failure(Exception()))
            }
        }

        val testModule = module {
            single<LocationProvider> {
                TestLocationProvider(throwable = NetworkError)
            }

            single<AppRepository> {
                appRepository
            }
        }

        setupKoinForTests(testModule)

        val future = CompletableFuture<Throwable>()
        appManager.requestLocalApps {
            if (it is Failure) {
                future.complete(it.throwable)
            }
        }

        assertEquals(NetworkError, future.get())
    }

    @Test
    fun `no app available`() {
        val appRepository = object : TestAppRepository() {
            override fun getLocationBasedApps(context: Context, latitude: Double, longitude: Double, completion: (Result<List<App>>) -> Unit) {
                completion(Result.success(emptyList()))
            }
        }

        val testModule = module {
            single<LocationProvider> {
                TestLocationProvider(mockLocation)
            }

            single<AppRepository> {
                appRepository
            }

            single<SharedPreferencesModel> {
                TestSharedPreferencesModel()
            }

            single<AppEventManager> {
                TestAppEventManager()
            }

            single<AppModel> {
                AppModelImpl()
            }
        }

        setupKoinForTests(testModule)

        val future = CompletableFuture<List<App>>()
        appManager.requestLocalApps {
            if (it is Success) {
                future.complete(it.result)
            }
        }
        assertEquals(0, future.get().size)
    }

    @Test
    fun `apps available`() {
        val app1 = App(
            url = "http://test1",
            name = "App #1",
            shortName = "Connected Fueling",
            description = "Subtitle app #1",
            logo = null
        )

        val app2 = App(
            url = "http://test2",
            name = "App #2",
            shortName = "Connected Fueling",
            description = "Subtitle app #2",
            logo = null
        )

        val app3 = App(
            url = "http://test3",
            name = "App #3",
            shortName = "Connected Fueling",
            description = "Subtitle app #3",
            logo = null
        )

        val appRepository = object : TestAppRepository() {
            override fun getLocationBasedApps(context: Context, latitude: Double, longitude: Double, completion: (Result<List<App>>) -> Unit) {
                completion(Result.success(listOf(app1, app2, app3)))
            }
        }

        val testModule = module {
            single<LocationProvider> {
                TestLocationProvider(mockLocation)
            }

            single<AppRepository> {
                appRepository
            }

            single<SharedPreferencesModel> {
                TestSharedPreferencesModel()
            }

            single<AppEventManager> {
                TestAppEventManager()
            }

            single<AppModel> {
                AppModelImpl()
            }
        }

        setupKoinForTests(testModule)

        val future = CompletableFuture<List<App>>()
        appManager.requestLocalApps {
            if (it is Success) {
                future.complete(it.result)
            }
        }

        val response1 = future.get()[0]
        assertEquals(app1.name, response1.name)
        assertEquals(app1.description, response1.description)

        val response2 = future.get()[1]
        assertEquals(app2.name, response2.name)
        assertEquals(app2.description, response2.description)

        val response3 = future.get()[2]
        assertEquals(app3.name, response3.name)
        assertEquals(app3.description, response3.description)
    }

    @Test
    fun `poi id is in range`() = runBlocking {
        val id = "e3211b77-03f0-4d49-83aa-4adaa46d95ae"

        val geoApiManager = object : TestGeoAPIManager() {
            override fun apps(latitude: Double, longitude: Double, completion: (Result<List<GeoGasStation>>) -> Unit) {
                completion(Result.success(listOf(GeoGasStation(id, listOf("https://app.test")))))
            }

            override fun features(poiId: String, latitude: Double, longitude: Double, completion: (Result<List<GeoAPIFeature>>) -> Unit) {
                completion(Result.success(listOf(getGeoAPIFeature(id))))
            }
        }

        val testModule = module {
            single<CacheModel> {
                TestCacheModel()
            }

            single<AppAPI> {
                TestAppAPI()
            }

            single<UriManager> {
                TestUriUtils()
            }

            single<LocationProvider> {
                TestLocationProvider(mockLocation)
            }

            single<GeoAPIManager> {
                geoApiManager
            }

            single<AppRepository> {
                AppRepositoryImpl(mockContext, get(), get(), get(), get())
            }
        }

        setupKoinForTests(testModule)

        assertTrue(appManager.isPoiInRange(id))
    }

    @Test
    fun `poi id is not in range`() = runBlocking {
        val id1 = "e3211b77-03f0-4d49-83aa-4adaa46d95ae"
        val id2 = "992b77b6-5982-4848-88fe-ae2633308279"

        val geoApiManager = object : TestGeoAPIManager() {
            override fun apps(latitude: Double, longitude: Double, completion: (Result<List<GeoGasStation>>) -> Unit) {
                completion(Result.success(listOf(GeoGasStation(id1, listOf("https://app.test")))))
            }

            override fun features(poiId: String, latitude: Double, longitude: Double, completion: (Result<List<GeoAPIFeature>>) -> Unit) {
                completion(Result.success(listOf(getGeoAPIFeature(id2))))
            }
        }

        val testModule = module {
            single<CacheModel> {
                TestCacheModel()
            }

            single<AppAPI> {
                TestAppAPI()
            }

            single<UriManager> {
                TestUriUtils()
            }

            single<LocationProvider> {
                TestLocationProvider(mockLocation)
            }

            single<GeoAPIManager> {
                geoApiManager
            }

            single<AppRepository> {
                AppRepositoryImpl(mockContext, get(), get(), get(), get())
            }
        }

        setupKoinForTests(testModule)

        assertFalse(appManager.isPoiInRange(id1))
    }

    private fun getGeoAPIFeature(id: String): GeoAPIFeature {
        return GeoAPIFeature(
            id,
            "Feature",
            Polygon(listOf(polygon)),
            mapOf(
                "apps" to listOf(
                    "type" to "fueling",
                    "url" to "https://fueling.app.test"
                )
            )
        )
    }

    private fun setupKoinForTests(module: Module) {
        KoinConfig.setupForTests(mockContext, module)
    }
}
