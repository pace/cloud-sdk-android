package cloud.pace.sdk.appkit

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import cloud.pace.sdk.appkit.app.AppActivityViewModel
import cloud.pace.sdk.appkit.app.AppActivityViewModelImpl
import cloud.pace.sdk.appkit.app.webview.AppWebViewModel
import cloud.pace.sdk.appkit.communication.AppModel
import cloud.pace.sdk.appkit.communication.AppModelImpl
import cloud.pace.sdk.appkit.communication.LogoutResponse
import cloud.pace.sdk.appkit.communication.generated.model.request.GooglePayAvailabilityCheckRequest
import cloud.pace.sdk.appkit.communication.generated.model.request.GooglePayPaymentRequest
import cloud.pace.sdk.appkit.communication.generated.model.request.OpenURLInNewTabRequest
import cloud.pace.sdk.appkit.communication.generated.model.response.GooglePayAvailabilityCheckResponse
import cloud.pace.sdk.appkit.communication.generated.model.response.GooglePayPaymentResponse
import cloud.pace.sdk.appkit.utils.CoroutineTestRule
import cloud.pace.sdk.utils.Completion
import cloud.pace.sdk.utils.Event
import io.mockk.mockk
import junit.framework.Assert.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
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

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(MockitoJUnitRunner::class)
class AppActivityViewModelTest : KoinTest {

    @get:Rule
    var coroutineTestRule = CoroutineTestRule()

    @get:Rule
    val rule = InstantTaskExecutorRule()

    private val mockContext = mock(Context::class.java)
    private val appModel = AppModelImpl(mockContext)
    private val viewModel: AppActivityViewModel by inject()

    private val testModule = module {
        single<AppModel> {
            appModel
        }

        viewModel<AppActivityViewModel> {
            AppActivityViewModelImpl(get())
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
        val observer = Observer<Event<Unit>> {}
        viewModel.closeEvent.observeForever(observer)

        appModel.close()
        assertEquals(Unit, viewModel.closeEvent.value?.peekContent())

        viewModel.closeEvent.removeObserver(observer)
    }

    @Test
    fun `open url in new tab`() {
        val url = "https://app.net"
        val openURLInNewTabRequest = OpenURLInNewTabRequest(url, "cancelUrl", false)

        val observer = Observer<Event<OpenURLInNewTabRequest>> {}
        viewModel.openUrlInNewTab.observeForever(observer)

        appModel.openUrlInNewTab(openURLInNewTabRequest)
        assertEquals(openURLInNewTabRequest, viewModel.openUrlInNewTab.value?.peekContent())

        viewModel.openUrlInNewTab.removeObserver(observer)
    }

    @Test
    fun `biometry is requested`() {
        val biometricRequest = AppWebViewModel.BiometricRequest(0, {}, { _, _ -> })

        val observer = Observer<Event<AppWebViewModel.BiometricRequest>> {}
        viewModel.biometricRequest.observeForever(observer)

        appModel.setBiometricRequest(biometricRequest)
        assertEquals(biometricRequest, viewModel.biometricRequest.value?.peekContent())

        viewModel.biometricRequest.removeObserver(observer)
    }

    @Test
    fun `authorize is requested`() {
        val onResult: (Completion<String?>) -> Unit = {}

        val observer = Observer<Event<AppModel.Result<Completion<String?>>>> {}
        viewModel.authorize.observeForever(observer)

        appModel.authorize(onResult)
        assertEquals(onResult, viewModel.authorize.value?.peekContent()?.onResult)

        viewModel.authorize.removeObserver(observer)
    }

    @Test
    fun `end session is requested`() {
        val onResult: (LogoutResponse) -> Unit = {}

        val observer = Observer<Event<AppModel.Result<LogoutResponse>>> {}
        viewModel.endSession.observeForever(observer)

        appModel.endSession(onResult)
        assertEquals(onResult, viewModel.endSession.value?.peekContent()?.onResult)

        viewModel.endSession.removeObserver(observer)
    }

    @Test
    fun `google pay availability check is requested`() {
        val onResult: (Completion<GooglePayAvailabilityCheckResponse>) -> Unit = {}
        val googlePayAvailabilityCheckRequest = GooglePayAvailabilityCheckRequest(2, 0, listOf())

        val observer = Observer<Event<Pair<GooglePayAvailabilityCheckRequest, (Completion<GooglePayAvailabilityCheckResponse>) -> Unit>>> {}
        viewModel.googlePayAvailabilityCheck.observeForever(observer)

        appModel.onGooglePayAvailabilityRequest(googlePayAvailabilityCheckRequest, onResult)
        assertEquals(Pair(googlePayAvailabilityCheckRequest, onResult), viewModel.googlePayAvailabilityCheck.value?.peekContent())

        viewModel.googlePayAvailabilityCheck.removeObserver(observer)
    }

    @Test
    fun `google pay payment is requested`() {
        val onResult: (Completion<GooglePayPaymentResponse>) -> Unit = {}
        val googlePayPaymentRequest = GooglePayPaymentRequest(2, 0, null, listOf(), mockk())

        val observer = Observer<Event<Pair<GooglePayPaymentRequest, (Completion<GooglePayPaymentResponse>) -> Unit>>> {}
        viewModel.googlePayPayment.observeForever(observer)

        appModel.onGooglePayPayment(googlePayPaymentRequest, onResult)
        assertEquals(Pair(googlePayPaymentRequest, onResult), viewModel.googlePayPayment.value?.peekContent())

        viewModel.googlePayPayment.removeObserver(observer)
    }

    @After
    fun tearDown() {
        stopKoin()
    }
}
