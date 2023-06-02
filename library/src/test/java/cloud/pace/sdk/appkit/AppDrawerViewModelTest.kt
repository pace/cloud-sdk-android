package cloud.pace.sdk.appkit

import android.location.Location
import cloud.pace.sdk.appkit.app.api.UriManager
import cloud.pace.sdk.appkit.app.drawer.ui.AppDrawerViewModel
import cloud.pace.sdk.appkit.app.drawer.ui.AppDrawerViewModelImpl
import cloud.pace.sdk.appkit.communication.AppEventManager
import cloud.pace.sdk.appkit.model.App
import cloud.pace.sdk.appkit.utils.CoroutineTestRule
import cloud.pace.sdk.appkit.utils.TestAppEventManager
import cloud.pace.sdk.appkit.utils.TestLocationProvider
import cloud.pace.sdk.appkit.utils.TestUriUtils
import cloud.pace.sdk.utils.Failure
import cloud.pace.sdk.utils.LocationProvider
import cloud.pace.sdk.utils.Success
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.inject
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class AppDrawerViewModelTest : KoinTest {

    @get:Rule
    val coroutineTestRule = CoroutineTestRule()

    private val host = "pace.tanke.emma.net"
    private val app1 = App(
        name = "Jetzt tanken",
        shortName = "Connected Fueling",
        description = "Tanke Emma",
        url = "https://$host",
        iconUrl = "https://icon.pace.tanke.emma.net",
        distance = 42
    )
    private val app2 = App(
        name = "Jetzt tanken",
        shortName = "Connected Fueling",
        description = "Tanke Emma 2",
        url = "https://pace.tanke.emma2.net",
        iconUrl = "https://icon.pace.tanke.emma2.net",
        distance = 52
    )
    private val apps = listOf(app1, app2)

    private val location = Location("").apply {
        latitude = 49.012440
        longitude = 8.426530
    }

    private val locationProvider = TestLocationProvider(location)
    private val appManager = mockk<AppManager>(relaxed = true)
    private val eventManager = TestAppEventManager()
    private val uriManager = TestUriUtils()
    private val viewModel: AppDrawerViewModel by inject()

    private val testModule = module {
        single<LocationProvider> { locationProvider }
        single { appManager }
        single<AppEventManager> { eventManager }
        single<UriManager> { uriManager }
        viewModel<AppDrawerViewModel> {
            AppDrawerViewModelImpl(get(), get(), get(), get())
        }
    }

    @Before
    fun setup() {
        startKoin {
            modules(testModule)
        }
    }

    @Test
    fun `apps are set if local available apps are available`() = runTest {
        coEvery { appManager.requestLocalApps(any<Location>()) } returns Success(apps)

        // Create an empty collector for the StateFlow
        backgroundScope.launch(coroutineTestRule.testDispatcher) {
            viewModel.apps.collect()
        }

        assertEquals(apps, viewModel.apps.value)
    }

    @Test
    fun `apps are empty if no apps are available`() = runTest {
        coEvery { appManager.requestLocalApps(any<Location>()) } returns Success(emptyList())

        // Create an empty collector for the StateFlow
        backgroundScope.launch(coroutineTestRule.testDispatcher) {
            viewModel.apps.collect()
        }

        assertEquals(emptyList(), viewModel.apps.value)
    }

    @Test
    fun `apps are empty if request fails`() = runTest {
        coEvery { appManager.requestLocalApps(any<Location>()) } returns Failure(Exception())

        // Create an empty collector for the StateFlow
        backgroundScope.launch(coroutineTestRule.testDispatcher) {
            viewModel.apps.collect()
        }

        assertEquals(emptyList(), viewModel.apps.value)
    }

    @Test
    fun `disabled app is removed`() = runTest {
        coEvery { appManager.requestLocalApps(any<Location>()) } returns Success(apps)

        // Create an empty collector for the StateFlow
        backgroundScope.launch(coroutineTestRule.testDispatcher) {
            viewModel.apps.collect()
        }

        assertEquals(apps, viewModel.apps.value)

        eventManager.setDisabledHost(host)

        assertEquals(listOf(app2), viewModel.apps.value)
    }

    @After
    fun tearDown() {
        stopKoin()
    }
}
