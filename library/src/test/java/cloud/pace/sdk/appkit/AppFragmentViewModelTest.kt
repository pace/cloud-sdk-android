package cloud.pace.sdk.appkit

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import cloud.pace.sdk.appkit.app.AppFragmentViewModel
import cloud.pace.sdk.appkit.app.AppFragmentViewModelImpl
import cloud.pace.sdk.appkit.communication.AppEventManager
import cloud.pace.sdk.appkit.communication.AppEventManagerImpl
import cloud.pace.sdk.appkit.communication.AppModel
import cloud.pace.sdk.appkit.communication.AppModelImpl
import cloud.pace.sdk.appkit.communication.generated.model.request.OpenURLInNewTabRequest
import cloud.pace.sdk.appkit.utils.CoroutineTestRule
import junit.framework.Assert.assertEquals
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.inject
import org.mockito.Mockito.mock
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class AppFragmentViewModelTest : KoinTest {

    @get:Rule
    var coroutineTestRule = CoroutineTestRule()

    @get:Rule
    val rule = InstantTaskExecutorRule()

    private val mockContext = mock(Context::class.java)
    private val eventManager = AppEventManagerImpl()
    private val appModel = AppModelImpl(mockContext)
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
    fun `app is closed`() {
        appModel.close()
        assertEquals(Unit, viewModel.closeEvent.value?.peekContent())
    }

    @Test
    fun `open url in new tab`() {
        val url = "https://app.net"
        val openURLInNewTabRequest = OpenURLInNewTabRequest(url, "cancelUrl")
        appModel.openUrlInNewTab(openURLInNewTabRequest)
        assertEquals(openURLInNewTabRequest, viewModel.openUrlInNewTab.value?.peekContent())
    }

    @After
    fun tearDown() {
        stopKoin()
    }
}
