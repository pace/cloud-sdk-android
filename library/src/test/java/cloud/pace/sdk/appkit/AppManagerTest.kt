package cloud.pace.sdk.appkit

import android.content.Context
import android.location.Location
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import cloud.pace.sdk.appkit.app.api.AppRepository
import cloud.pace.sdk.appkit.communication.AppEventManager
import cloud.pace.sdk.appkit.communication.AppModel
import cloud.pace.sdk.appkit.communication.AppModelImpl
import cloud.pace.sdk.appkit.location.AppLocationManager
import cloud.pace.sdk.appkit.model.App
import cloud.pace.sdk.appkit.persistence.SharedPreferencesModel
import cloud.pace.sdk.appkit.utils.*
import cloud.pace.sdk.utils.AppKitKoinComponent
import cloud.pace.sdk.utils.Failure
import cloud.pace.sdk.utils.KoinConfig
import cloud.pace.sdk.utils.Success
import org.junit.After
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.koin.core.module.Module
import org.koin.dsl.module
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import java.util.concurrent.CompletableFuture

@RunWith(MockitoJUnitRunner::class)
class AppManagerTest : AppKitKoinComponent {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @Mock
    private lateinit var mockContext: Context

    @Mock
    private lateinit var mockLocation: Location

    @After
    fun onFinished() = stopKoin()

    @Test
    fun `no app due to missing location permissions`() {
        val testModule = module {
            single<AppLocationManager> {
                TestAppLocationManager(throwable = PermissionDenied)
            }
        }

        setupKoinForTests(testModule)

        val future = CompletableFuture<Throwable>()
        AppManager().requestLocalApps {
            if (it is Failure) {
                future.complete(it.throwable)
            }
        }

        assertEquals(PermissionDenied, future.get())
    }

    @Test
    fun `no app due to missing location`() {
        val testModule = module {
            single<AppLocationManager> {
                TestAppLocationManager(throwable = NoLocationFound)
            }
        }

        setupKoinForTests(testModule)

        val future = CompletableFuture<Throwable>()
        AppManager().requestLocalApps {
            if (it is Failure) {
                future.complete(it.throwable)
            }
        }

        assertEquals(NoLocationFound, future.get())
    }

    @Test
    fun `no app due to network error`() {
        val appRepository = object : TestAppRepository() {
            override fun getLocationBasedApps(context: Context, latitude: Double, longitude: Double, retry: Boolean, completion: (Result<List<App>>) -> Unit) {
                completion(Result.failure(Exception()))
            }
        }

        val testModule = module {
            single<AppLocationManager> {
                TestAppLocationManager(throwable = NetworkError)
            }

            single<AppRepository> {
                appRepository
            }
        }

        setupKoinForTests(testModule)

        val future = CompletableFuture<Throwable>()
        AppManager().requestLocalApps {
            if (it is Failure) {
                future.complete(it.throwable)
            }
        }

        assertEquals(NetworkError, future.get())
    }

    @Test
    fun `no app available`() {
        val appRepository = object : TestAppRepository() {
            override fun getLocationBasedApps(context: Context, latitude: Double, longitude: Double, retry: Boolean, completion: (Result<List<App>>) -> Unit) {
                completion(Result.success(emptyList()))
            }
        }

        val testModule = module {
            single<AppLocationManager> {
                TestAppLocationManager(location = mockLocation)
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
        AppManager().requestLocalApps {
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
            shortName = "Subtitle app #1",
            logo = null
        )

        val app2 = App(
            url = "http://test2",
            name = "App #2",
            shortName = "Subtitle app #2",
            logo = null
        )

        val app3 = App(
            url = "http://test3",
            name = "App #3",
            shortName = "Subtitle app #3",
            logo = null
        )

        val appRepository = object : TestAppRepository() {
            override fun getLocationBasedApps(context: Context, latitude: Double, longitude: Double, retry: Boolean, completion: (Result<List<App>>) -> Unit) {
                completion(Result.success(listOf(app1, app2, app3)))
            }
        }

        val testModule = module {
            single<AppLocationManager> {
                TestAppLocationManager(mockLocation)
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
        }

        setupKoinForTests(testModule)

        val future = CompletableFuture<List<App>>()
        AppManager().requestLocalApps {
            if (it is Success) {
                future.complete(it.result)
            }
        }

        val response1 = future.get()[0]
        assertEquals(app1.name, response1.name)
        assertEquals(app1.shortName, response1.shortName)

        val response2 = future.get()[1]
        assertEquals(app2.name, response2.name)
        assertEquals(app2.shortName, response2.shortName)

        val response3 = future.get()[2]
        assertEquals(app3.name, response3.name)
        assertEquals(app3.shortName, response3.shortName)
    }

    @Test
    fun `poi id is in range`() {
        val id = "e3211b77-03f0-4d49-83aa-4adaa46d95ae"
        val app = App(
            url = "http://test1",
            name = "App #1",
            shortName = "Subtitle app #1",
            logo = null,
            gasStationId = id
        )

        val appRepository = object : TestAppRepository() {
            override fun getLocationBasedApps(context: Context, latitude: Double, longitude: Double, retry: Boolean, completion: (Result<List<App>>) -> Unit) {
                completion(Result.success(listOf(app)))
            }
        }

        val testModule = module {
            single<AppLocationManager> {
                TestAppLocationManager(mockLocation)
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
        }

        setupKoinForTests(testModule)

        val future = CompletableFuture<Boolean>()
        AppManager().isPoiInRange(id) {
            future.complete(it)
        }

        assertTrue(future.get())
    }

    @Test
    fun `poi id is not in range`() {
        val id1 = "e3211b77-03f0-4d49-83aa-4adaa46d95ae"
        val id2 = "992b77b6-5982-4848-88fe-ae2633308279"
        val app = App(
            url = "http://test1",
            name = "App #1",
            shortName = "Subtitle app #1",
            logo = null,
            gasStationId = id1
        )

        val appRepository = object : TestAppRepository() {
            override fun getLocationBasedApps(context: Context, latitude: Double, longitude: Double, retry: Boolean, completion: (Result<List<App>>) -> Unit) {
                completion(Result.success(listOf(app)))
            }
        }

        val testModule = module {
            single<AppLocationManager> {
                TestAppLocationManager(mockLocation)
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
        }

        setupKoinForTests(testModule)

        val future = CompletableFuture<Boolean>()
        AppManager().isPoiInRange(id2) {
            future.complete(it)
        }

        assertFalse(future.get())
    }

    private fun setupKoinForTests(module: Module) {
        KoinConfig.setupForTests(mockContext, module)
    }
}
