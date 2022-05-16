package cloud.pace.sdk.appkit

import android.content.Context
import android.graphics.Bitmap
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import cloud.pace.sdk.appkit.app.drawer.AppDrawerViewModel
import cloud.pace.sdk.appkit.app.drawer.AppDrawerViewModelImpl
import cloud.pace.sdk.appkit.model.App
import cloud.pace.sdk.appkit.utils.TestAppEventManager
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class AppDrawerViewModelTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    private val eventManager = TestAppEventManager()
    private val viewModel = AppDrawerViewModelImpl(eventManager)

    @Mock
    private lateinit var mockContext: Context

    @Mock
    private lateinit var logo: Bitmap

    @Before
    fun init() {
        startKoin {
            androidContext(mockContext)
            modules(
                module {
                    single { eventManager }
                    viewModel<AppDrawerViewModel> { viewModel }
                }
            )
        }
    }

    @After
    fun onFinished() = stopKoin()

    @Test
    fun `set app`() {
        val app = App(name = "Jetzt tanken", shortName = "Connected Fueling", description = "Tanke Emma", url = "https://pace.tanke.emma.net", logo = null)
        viewModel.init(app, false)

        Assert.assertEquals(app.name, viewModel.title.value)
        Assert.assertEquals(app.description, viewModel.subtitle.value)
        Assert.assertNull(viewModel.background.value)
        Assert.assertNull(viewModel.textColor.value)
        Assert.assertNull(viewModel.iconBackground.value)
        Assert.assertNull(viewModel.logo.value)
    }

    @Test
    fun `set app with icon`() {
        val app = App(name = "Jetzt tanken", shortName = "Connected Fueling", description = "Tanke Emma", url = "https://pace.tanke.emma.net", logo = logo)
        viewModel.init(app, true)

        Assert.assertEquals(app.logo, viewModel.logo.value)
    }

    @Test
    fun `set app with icon background`() {
        val app = App(name = "Jetzt tanken", shortName = "Connected Fueling", description = "Tanke Emma", url = "https://pace.tanke.emma.net", logo = null, iconBackgroundColor = "#ffffff")
        viewModel.init(app, false)

        Assert.assertNotNull(viewModel.iconBackground.value)
    }

    @Test
    fun `close app`() {
        val url = "https://pace.tanke.emma.net"
        viewModel.init(App(name = "Jetzt tanken", shortName = "Connected Fueling", description = "Tanke Emma", url = url, logo = null), false)

        viewModel.onCreate()

        eventManager.setInvalidApps(listOf(url))

        Assert.assertNotNull(viewModel.closeEvent.value)

        viewModel.onDestroy()
    }

    @Test
    fun `do not close app`() {
        viewModel.init(App(name = "Jetzt tanken", shortName = "Connected Fueling", description = "Tanke Emma", url = "https://pace.tanke.emma.net", logo = null), false)

        viewModel.onCreate()

        eventManager.setInvalidApps(listOf("https://app.test.net"))

        Assert.assertNull(viewModel.closeEvent.value)

        viewModel.onDestroy()
    }

    @Test
    fun `disable app`() {
        val host = "pace.tanke.emma.net"
        val url = "https://$host"
        viewModel.init(App(name = "Jetzt tanken", shortName = "Connected Fueling", description = "Tanke Emma", url = url, logo = null), false)

        viewModel.onCreate()

        Assert.assertNull(viewModel.closeEvent.value?.getContentIfNotHandled())
        eventManager.setDisabledHost(host)
        Assert.assertEquals(Unit, viewModel.closeEvent.value?.getContentIfNotHandled())

        viewModel.onDestroy()
    }
}
