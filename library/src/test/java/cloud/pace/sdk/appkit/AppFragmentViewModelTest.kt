package cloud.pace.sdk.appkit

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import cloud.pace.sdk.appkit.app.AppFragmentViewModel
import cloud.pace.sdk.appkit.app.AppFragmentViewModelImpl
import cloud.pace.sdk.appkit.communication.AppEventManager
import cloud.pace.sdk.appkit.communication.AppEventManagerImpl
import cloud.pace.sdk.appkit.communication.AppModel
import cloud.pace.sdk.appkit.communication.AppModelImpl
import cloud.pace.sdk.appkit.utils.CoroutineTestRule
import junit.framework.Assert.assertEquals
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.inject
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class AppFragmentViewModelTest : KoinTest {

    @get:Rule
    var coroutineTestRule = CoroutineTestRule()

    @get:Rule
    val rule = InstantTaskExecutorRule()

    private val eventManager = AppEventManagerImpl()
    private val appModel = AppModelImpl()
    private val viewModel: AppFragmentViewModel by inject()

    private val testModule = module {
        single<AppEventManager> {
            eventManager
        }

        single<AppModel> {
            appModel
        }

        viewModel<AppFragmentViewModel> {
            AppFragmentViewModelImpl(get(), get())
        }
    }

    @Before
    fun init() {
        startKoin {
            modules(testModule)
        }
    }

    @Test
    fun `force close`() {
        appModel.close(true)
        assertEquals(true to null, viewModel.closeEvent.value?.peekContent())
    }

    @Test
    fun `no force close`() {
        appModel.close()
        assertEquals(false to null, viewModel.closeEvent.value?.peekContent())
    }

    @Test
    fun `open url in new tab`() {
        val url = "https://app.net"
        appModel.openUrlInNewTab(url)
        assertEquals(url, viewModel.openUrlInNewTab.value?.peekContent())
    }

    @Test
    fun `redirect app`() {
        val url = "https://app.net"
        eventManager.onReceivedRedirect(url)
        assertEquals(url, viewModel.redirectEvent.value?.peekContent())
    }

    @After
    fun tearDown() {
        stopKoin()
    }
}
