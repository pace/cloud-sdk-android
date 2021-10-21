package cloud.pace.sdk.appkit

import android.content.Context
import android.location.Location
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import cloud.pace.sdk.PACECloudSDK
import cloud.pace.sdk.appkit.app.api.AppRepository
import cloud.pace.sdk.appkit.communication.AppEventManager
import cloud.pace.sdk.appkit.communication.AppModel
import cloud.pace.sdk.appkit.communication.AppModelImpl
import cloud.pace.sdk.appkit.model.App
import cloud.pace.sdk.appkit.persistence.SharedPreferencesModel
import cloud.pace.sdk.appkit.utils.*
import cloud.pace.sdk.utils.*
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.After
import org.junit.Assert.assertEquals
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

    @Before
    fun init() {
        `when`(mockLocation.latitude).then { 49.012722 }
        `when`(mockLocation.longitude).then { 8.427326 }
        `when`(mockLocation.speed).then { 3f }

        PACECloudSDK.configuration = Configuration("", "", "", "", environment = Environment.DEVELOPMENT, oidConfiguration = null)
    }

    @After
    fun onFinished() = stopKoin()

    @Test
    fun `no app due to invalid speed`() {
        `when`(mockLocation.speed).then { 20f }

        val app1 = App(
            url = "http://test1",
            name = "App #1",
            shortName = "Connected Fueling",
            description = "Subtitle app #1",
            logo = null
        )

        val appRepository = object : TestAppRepository() {
            override suspend fun getLocationBasedApps(latitude: Double, longitude: Double): Completion<List<App>> {
                return Success(listOf(app1))
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
                AppModelImpl(mockContext)
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
            override suspend fun getLocationBasedApps(latitude: Double, longitude: Double): Completion<List<App>> {
                return Failure(Exception())
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
        val testModule = module {
            single<LocationProvider> {
                TestLocationProvider(mockLocation)
            }

            single<AppRepository> {
                TestAppRepository()
            }

            single<SharedPreferencesModel> {
                TestSharedPreferencesModel()
            }

            single<AppEventManager> {
                TestAppEventManager()
            }

            single<AppModel> {
                AppModelImpl(mockContext)
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
    fun `old app not being removed although speed is invalid`() {
        val location = mockk<Location>()

        val app1 = App(
            url = "http://test1",
            name = "App #1",
            shortName = "Connected Fueling",
            description = "Subtitle app #1",
            logo = null
        )

        val appRepository = object : TestAppRepository() {
            override suspend fun getLocationBasedApps(latitude: Double, longitude: Double): Completion<List<App>> {
                return Success(listOf(app1))
            }
        }

        every { location.latitude } returns 49.012722
        every { location.longitude } returns 8.427326
        every { location.speed } returns 3f

        val testModule = module {
            single<LocationProvider> {
                TestLocationProvider(location)
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
                AppModelImpl(mockContext)
            }
        }

        setupKoinForTests(testModule)

        val appFuture = CompletableFuture<List<App>>()

        appManager.requestLocalApps {
            if (it is Success) {
                appFuture.complete(it.result)
            }
        }

        val response1 = appFuture.get()[0]
        assertEquals(app1.name, response1.name)
        assertEquals(app1.description, response1.description)

        every { location.speed } returns 20f

        val oldAppFuture = CompletableFuture<List<App>>()

        appManager.requestLocalApps {
            if (it is Success) {
                oldAppFuture.complete(it.result)
            }
        }

        val oldApp = oldAppFuture.get()[0]
        assertEquals(app1.name, oldApp.name)
        assertEquals(app1.description, oldApp.description)
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
            override suspend fun getLocationBasedApps(latitude: Double, longitude: Double): Completion<List<App>> {
                return Success(listOf(app1, app2, app3))
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
                AppModelImpl(mockContext)
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

    private fun setupKoinForTests(module: Module) {
        KoinConfig.setupForTests(mockContext, module)
    }
}
