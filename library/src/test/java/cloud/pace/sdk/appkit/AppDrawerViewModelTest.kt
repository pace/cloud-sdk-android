package cloud.pace.sdk.appkit

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import cloud.pace.sdk.appkit.app.drawer.AppDrawerViewModel
import cloud.pace.sdk.appkit.app.drawer.AppDrawerViewModelImpl
import cloud.pace.sdk.appkit.model.App
import cloud.pace.sdk.appkit.utils.TestAppEventManager
import org.junit.*
import org.junit.runner.RunWith
import org.koin.android.ext.koin.androidContext
import org.koin.android.viewmodel.dsl.viewModel
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
            modules(module {
                single { eventManager }
                viewModel<AppDrawerViewModel> { viewModel }
            })
        }
    }

    @After
    fun onFinished() = stopKoin()

    @Test
    fun `set app`() {
        val app = App(name = "Jetzt tanken", shortName = "Tanke Emma", url = "https://pace.tanke.emma.net", logo = null)
        viewModel.init(app, false)

        Assert.assertEquals(app.name, viewModel.title.value)
        Assert.assertEquals(app.shortName, viewModel.subtitle.value)
        Assert.assertEquals(Color.WHITE, viewModel.background.value)
        Assert.assertEquals(Color.BLACK, viewModel.textColor.value)
        Assert.assertNull(viewModel.iconBackground.value)
        Assert.assertNull(viewModel.logo.value)
    }

    @Test
    fun `set app with icon`() {
        val app = App(name = "Jetzt tanken", shortName = "Tanke Emma", url = "https://pace.tanke.emma.net", logo = logo)
        viewModel.init(app, true)

        Assert.assertEquals(app.logo, viewModel.logo.value)
    }

    @Test
    fun `set app with dark theme`() {
        val app = App(name = "Jetzt tanken", shortName = "Tanke Emma", url = "https://pace.tanke.emma.net", logo = null)
        viewModel.init(app, true)

        Assert.assertEquals(Color.BLACK, viewModel.background.value)
        Assert.assertEquals(Color.WHITE, viewModel.textColor.value)
    }

    @Test
    fun `set app with icon background`() {
        val app = App(name = "Jetzt tanken", shortName = "Tanke Emma", url = "https://pace.tanke.emma.net", logo = null, iconBackgroundColor = "#ffffff")
        viewModel.init(app, false)

        Assert.assertNotNull(viewModel.iconBackground.value)
    }

    @Test
    fun `change app infos`() {
        val url = "https://pace.tanke.emma.net"
        viewModel.init(App(name = "Jetzt tanken", shortName = "Tanke Emma", url = url, logo = null), false)

        viewModel.onCreate()

        val newName = "Jetzt weiter tanken"
        val newShortName = "Tanken unterbrochen"
        eventManager.onAppDrawerChanged(url, newName, newShortName)

        Assert.assertEquals(newName, viewModel.title.value)
        Assert.assertEquals(newShortName, viewModel.subtitle.value)

        viewModel.onDestroy()
    }

    @Test
    fun `do not change app infos`() {
        val url = "https://pace.tanke.emma.net"
        val name = "Jetzt tanken"
        val shortName = "Tanke Emma"
        viewModel.init(App(name = name, shortName = shortName, url = url, logo = null), false)

        viewModel.onCreate()

        eventManager.onAppDrawerChanged("https://app.test.net", "Jetzt weiter tanken", "Tanken unterbrochen")

        Assert.assertEquals(name, viewModel.title.value)
        Assert.assertEquals(shortName, viewModel.subtitle.value)

        viewModel.onDestroy()
    }

    @Test
    fun `only title changed`() {
        val url = "https://pace.tanke.emma.net"
        val oldName = "Jetzt tanken"
        val oldShortName = "Tanke Emma"
        viewModel.init(App(name = oldName, shortName = oldShortName, url = url, logo = null), false)

        viewModel.onCreate()

        val newName = "Jetzt weiter tanken"
        eventManager.onAppDrawerChanged(url, newName, null)

        Assert.assertEquals(newName, viewModel.title.value)
        Assert.assertEquals(oldShortName, viewModel.subtitle.value)

        viewModel.onDestroy()
    }

    @Test
    fun `only subtitle changed`() {
        val url = "https://pace.tanke.emma.net"
        val oldName = "Jetzt tanken"
        val oldShortName = "Tanke Emma"
        viewModel.init(App(name = oldName, shortName = oldShortName, url = url, logo = null), false)

        viewModel.onCreate()

        val newShortName = "Jetzt weiter tanken"
        eventManager.onAppDrawerChanged(url, null, newShortName)

        Assert.assertEquals(oldName, viewModel.title.value)
        Assert.assertEquals(newShortName, viewModel.subtitle.value)

        viewModel.onDestroy()
    }

    @Test
    fun `close app`() {
        val url = "https://pace.tanke.emma.net"
        viewModel.init(App(name = "Jetzt tanken", shortName = "Tanke Emma", url = url, logo = null), false)

        viewModel.onCreate()

        eventManager.setInvalidApps(listOf(url))

        Assert.assertNotNull(viewModel.closeEvent.value)

        viewModel.onDestroy()
    }

    @Test
    fun `do not close app`() {
        viewModel.init(App(name = "Jetzt tanken", shortName = "Tanke Emma", url = "https://pace.tanke.emma.net", logo = null), false)

        viewModel.onCreate()

        eventManager.setInvalidApps(listOf("https://app.test.net"))

        Assert.assertNull(viewModel.closeEvent.value)

        viewModel.onDestroy()
    }

    @Test
    fun `reset title and subtitle with close action`() {
        val url = "https://pace.tanke.emma.net"
        val oldName = "Jetzt tanken"
        val oldShortName = "Tanke Emma"
        viewModel.init(App(name = oldName, shortName = oldShortName, url = url, logo = null), false)

        viewModel.onCreate()

        val newName = "Jetzt weiter tanken"
        val newShortName = "Tanken unterbrochen"
        eventManager.onAppDrawerChanged(url, newName, newShortName)
        Assert.assertEquals(newName, viewModel.title.value)
        Assert.assertEquals(newShortName, viewModel.subtitle.value)

        eventManager.onAppDrawerChanged(url, null, null)

        Assert.assertEquals(oldName, viewModel.title.value)
        Assert.assertEquals(oldShortName, viewModel.subtitle.value)

        viewModel.onDestroy()
    }

    @Test
    fun `disable app`() {
        val host = "pace.tanke.emma.net"
        val url = "https://$host"
        viewModel.init(App(name = "Jetzt tanken", shortName = "Tanke Emma", url = url, logo = null), false)

        viewModel.onCreate()

        Assert.assertNull(viewModel.closeEvent.value?.getContentIfNotHandled())
        eventManager.setDisabledHost(host)
        Assert.assertEquals(Unit, viewModel.closeEvent.value?.getContentIfNotHandled())

        viewModel.onDestroy()
    }
}
