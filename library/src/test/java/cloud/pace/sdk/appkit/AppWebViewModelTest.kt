package cloud.pace.sdk.appkit

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import cloud.pace.sdk.appkit.app.webview.AppWebViewClient
import cloud.pace.sdk.appkit.app.webview.AppWebViewModel
import cloud.pace.sdk.appkit.app.webview.AppWebViewModelImpl
import cloud.pace.sdk.appkit.communication.AppCallbackImpl
import cloud.pace.sdk.appkit.communication.AppModelImpl
import cloud.pace.sdk.appkit.location.AppLocationManager
import cloud.pace.sdk.appkit.pay.PayAuthenticationManager
import cloud.pace.sdk.appkit.persistence.SharedPreferencesImpl
import cloud.pace.sdk.appkit.persistence.SharedPreferencesModel
import cloud.pace.sdk.appkit.utils.EncryptionUtils
import cloud.pace.sdk.appkit.utils.TestAppEventManager
import cloud.pace.sdk.appkit.utils.TestUriUtils
import cloud.pace.sdk.utils.Event
import cloud.pace.sdk.utils.random
import com.nhaarman.mockitokotlin2.anyOrNull
import io.mockk.every
import io.mockk.mockkObject
import io.mockk.slot
import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertTrue
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class AppWebViewModelTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    private val sharedPreferencesModel = mock(SharedPreferencesModel::class.java)
    private val payManager = mock(PayAuthenticationManager::class.java)
    private var onAppDrawerChangedCalled = false
    private var disabled = ""
    private val eventManager = object : TestAppEventManager() {
        override fun onAppDrawerChanged(url: String, title: String?, subtitle: String?) {
            onAppDrawerChangedCalled = true
        }

        override fun setDisabledHost(host: String) {
            disabled = "fueling-app"
        }
    }
    private val uriUtils = TestUriUtils()
    private val appCallback = mock(AppCallbackImpl::class.java)
    private val appModel = AppModelImpl()
    private val viewModel = AppWebViewModelImpl(sharedPreferencesModel, uriUtils, eventManager, payManager, appModel, mock(AppLocationManager::class.java))

    @Before
    fun init() {
        appModel.callback = appCallback
        onAppDrawerChangedCalled = false
        disabled = ""

        // mock encryption so that it does not do anything
        mockkObject(EncryptionUtils)
        val slot = slot<String>()
        val slot2 = slot<String>()
        every { EncryptionUtils.encrypt(capture(slot)) } answers { slot.captured }
        every { EncryptionUtils.decrypt(capture(slot2)) } answers { slot2.captured }
    }

    @Test
    fun `open app with passed URL`() {
        val url = "https://app.test.net"
        viewModel.init(url)

        assertEquals(url, viewModel.url.value?.getContentIfNotHandled())
    }

    @Test
    fun `reopen app with stored state`() {
        val url = "https://app.test.net"
        val reopenUrl = "https://app.continue.net"
        val state = String.random(8)

        `when`(sharedPreferencesModel.getAppStates()).thenReturn(listOf(SharedPreferencesModel.AppState(url, reopenUrl, state)))
        viewModel.init(url)

        assertEquals("$reopenUrl/state=$state", viewModel.url.value?.getContentIfNotHandled())
    }

    @Test
    fun `close without reopening request`() {
        val url = "https://app.test.net"
        viewModel.init(url)

        viewModel.close(null)

        verify(appCallback, times(1)).onClose()
        verify(sharedPreferencesModel, times(0)).saveAppState(anyOrNull())
        verify(sharedPreferencesModel, times(1)).deleteAppState(url)
    }

    @Test
    fun `close with reopening request`() {
        val url = "https://app.test.net"
        viewModel.init(url)

        val reopenUrl = "https://app.continue.net"
        val state = String.random(8)
        val title = "Reopen"
        val subtitle = "Continue"
        viewModel.close(AppWebViewClient.WebClientCallback.ReopenRequest(reopenUrl, state, title, subtitle))

        verify(appCallback, times(1)).onClose()
        assertTrue(onAppDrawerChangedCalled)
        verify(sharedPreferencesModel, times(1)).saveAppState(anyOrNull())
    }

    @Test
    fun `App loading successful`() {
        viewModel.onSwitchErrorState(false, false)

        assertEquals(false, viewModel.isInErrorState.value?.getContentIfNotHandled())
    }

    @Test
    fun `App loading failed`() {
        viewModel.onSwitchErrorState(true, false)

        assertEquals(true, viewModel.isInErrorState.value?.getContentIfNotHandled())
    }

    @Test
    fun `has biometrics`() {
        val redirectUri = "https://app.pay.redirect.net"
        val state = String.random(8)

        `when`(payManager.isFingerprintAvailable()).thenReturn(true)

        viewModel.getBiometricStatus(redirectUri, state)

        assertEquals("$redirectUri/status_code=200&state=$state", viewModel.url.value?.getContentIfNotHandled())
    }

    @Test
    fun `does not have biometrics`() {
        val redirectUri = "https://app.pay.redirect.net"
        val state = String.random(8)

        `when`(payManager.isFingerprintAvailable()).thenReturn(false)

        viewModel.getBiometricStatus(redirectUri, state)

        assertEquals("$redirectUri/status_code=404&state=$state", viewModel.url.value?.getContentIfNotHandled())
    }

    @Test
    fun `secret is saved`() {
        val host = "fueling-app"
        val redirectUri = "https://app.pay.redirect.net"
        val state = String.random(8)

        viewModel.saveTotpSecret(AppWebViewClient.WebClientCallback.TotpSecretRequest(host, "secret", "foo", 30, 8, "SHA1", redirectUri, state))

        verify(sharedPreferencesModel, times(1)).putString(ArgumentMatchers.contains("foo"), ArgumentMatchers.contains("secret"))
        verify(sharedPreferencesModel, times(1)).putString(ArgumentMatchers.contains(host), ArgumentMatchers.contains("secret"))
        verify(sharedPreferencesModel, times(1)).putString(ArgumentMatchers.anyString(), ArgumentMatchers.contains("SHA1"))
        verify(sharedPreferencesModel, times(1)).putInt(ArgumentMatchers.anyString(), ArgumentMatchers.intThat { it == 30 })
        verify(sharedPreferencesModel, times(1)).putInt(ArgumentMatchers.anyString(), ArgumentMatchers.intThat { it == 8 })
        assertEquals("$redirectUri/status_code=200&state=$state", viewModel.url.value?.getContentIfNotHandled())
    }

    @Test
    fun `secret is fetched`() {
        val host = "fueling-app"
        val redirectUri = "https://app.pay.redirect.net"
        val state = String.random(8)

        val observer: Observer<Event<AppWebViewModel.BiometricRequest>> = Observer {
            val event = it.getContentIfNotHandled() ?: return@Observer
            event.onSuccess()
        }
        viewModel.biometricRequest.observeForever(observer)

        `when`(payManager.isFingerprintAvailable()).thenReturn(true)
        `when`(sharedPreferencesModel.getString(ArgumentMatchers.contains(SharedPreferencesImpl.SECRET), any())).thenReturn("secret")
        `when`(sharedPreferencesModel.getString(ArgumentMatchers.contains(SharedPreferencesImpl.ALGORITHM), any())).thenReturn("SHA1")
        `when`(sharedPreferencesModel.getInt(ArgumentMatchers.contains(SharedPreferencesImpl.DIGITS), any())).thenReturn(8)
        `when`(sharedPreferencesModel.getInt(ArgumentMatchers.contains(SharedPreferencesImpl.PERIOD), any())).thenReturn(30)

        viewModel.getTotp(host, "foo", 0, redirectUri, state)

        verify(sharedPreferencesModel, times(2)).getString(ArgumentMatchers.contains("foo"), any())
        verify(sharedPreferencesModel, times(2)).getInt(ArgumentMatchers.contains("foo"), any())
        assertEquals("$redirectUri/totp=48857148&biometric_method=other&state=$state", viewModel.url.value?.getContentIfNotHandled())

        viewModel.biometricRequest.removeObserver(observer)
    }

    @Test
    fun `biometric authentication failed`() {
        val host = "fueling-app"
        val redirectUri = "https://app.pay.redirect.net"
        val state = String.random(8)

        val observer: Observer<Event<AppWebViewModel.BiometricRequest>> = Observer {
            val event = it.getContentIfNotHandled() ?: return@Observer
            event.onFailure()
        }
        viewModel.biometricRequest.observeForever(observer)

        `when`(payManager.isFingerprintAvailable()).thenReturn(true)
        `when`(sharedPreferencesModel.getString(ArgumentMatchers.contains(SharedPreferencesImpl.SECRET), any())).thenReturn("secret")
        `when`(sharedPreferencesModel.getString(ArgumentMatchers.contains(SharedPreferencesImpl.ALGORITHM), any())).thenReturn("SHA1")
        `when`(sharedPreferencesModel.getInt(ArgumentMatchers.contains(SharedPreferencesImpl.DIGITS), any())).thenReturn(8)
        `when`(sharedPreferencesModel.getInt(ArgumentMatchers.contains(SharedPreferencesImpl.PERIOD), any())).thenReturn(30)

        viewModel.getTotp(host, "foo", 0, redirectUri, state)

        assertEquals("$redirectUri/status_code=401&state=$state", viewModel.url.value?.getContentIfNotHandled())

        viewModel.biometricRequest.removeObserver(observer)
    }

    @Test
    fun `secret exists but biometry not available`() {
        val host = "fueling-app"
        val redirectUri = "https://app.pay.redirect.net"
        val state = String.random(8)

        val observer: Observer<Event<AppWebViewModel.BiometricRequest>> = Observer {
            val event = it.getContentIfNotHandled() ?: return@Observer
            event.onSuccess()
        }
        viewModel.biometricRequest.observeForever(observer)

        `when`(payManager.isFingerprintAvailable()).thenReturn(false)

        viewModel.getTotp(host, "foo", 0, redirectUri, state)

        verify(sharedPreferencesModel, times(0)).getString(ArgumentMatchers.contains("foo"), any())
        verify(sharedPreferencesModel, times(0)).getInt(ArgumentMatchers.contains("foo"), any())
        assertEquals("$redirectUri/status_code=405&state=$state", viewModel.url.value?.getContentIfNotHandled())

        viewModel.biometricRequest.removeObserver(observer)
    }

    @Test
    fun `secret not found`() {
        val host = "fueling-app"
        val redirectUri = "https://app.pay.redirect.net"
        val state = String.random(8)

        val observer: Observer<Event<AppWebViewModel.BiometricRequest>> = Observer {
            val event = it.getContentIfNotHandled() ?: return@Observer
            event.onSuccess()
        }
        viewModel.biometricRequest.observeForever(observer)

        `when`(payManager.isFingerprintAvailable()).thenReturn(true)
        `when`(sharedPreferencesModel.getString(ArgumentMatchers.contains(SharedPreferencesImpl.SECRET), any())).thenReturn(null)
        `when`(sharedPreferencesModel.getString(ArgumentMatchers.contains(SharedPreferencesImpl.ALGORITHM), any())).thenReturn("SHA1")
        `when`(sharedPreferencesModel.getInt(ArgumentMatchers.contains(SharedPreferencesImpl.DIGITS), any())).thenReturn(8)
        `when`(sharedPreferencesModel.getInt(ArgumentMatchers.contains(SharedPreferencesImpl.PERIOD), any())).thenReturn(30)

        viewModel.getTotp(host, "foo", 0, redirectUri, state)

        assertEquals("$redirectUri/status_code=404&state=$state", viewModel.url.value?.getContentIfNotHandled())

        viewModel.biometricRequest.removeObserver(observer)
    }

    @Test
    fun `save secure data`() {
        val host = "fueling-app"
        val redirectUri = "https://app.pay.redirect.net"
        val state = String.random(8)

        viewModel.setSecureData(host, "foo", "bar", redirectUri, state)

        verify(sharedPreferencesModel, times(1)).putString(ArgumentMatchers.contains("foo"), ArgumentMatchers.contains("bar"))
        assertEquals("$redirectUri/status_code=200&state=$state", viewModel.url.value?.getContentIfNotHandled())
    }

    @Test
    fun `get secure data`() {
        val host = "fueling-app"
        val redirectUri = "https://app.pay.redirect.net"
        val state = String.random(8)

        val observer: Observer<Event<AppWebViewModel.BiometricRequest>> = Observer {
            val event = it.getContentIfNotHandled() ?: return@Observer
            event.onSuccess()
        }
        viewModel.biometricRequest.observeForever(observer)

        `when`(payManager.isFingerprintAvailable()).thenReturn(true)
        `when`(sharedPreferencesModel.getString(ArgumentMatchers.contains(SharedPreferencesImpl.SECURE_DATA), any())).thenReturn("bar")

        viewModel.getSecureData(host, "foo", redirectUri, state)

        verify(sharedPreferencesModel).getString(ArgumentMatchers.contains("foo"), any())
        verify(sharedPreferencesModel).getString(ArgumentMatchers.contains(host), any())
        assertEquals("$redirectUri/value=bar&state=$state", viewModel.url.value?.getContentIfNotHandled())

        viewModel.biometricRequest.removeObserver(observer)
    }

    @Test
    fun `set disable timestamp`() {
        val host = "fueling-app"
        val until = 1597143588148L

        viewModel.setDisableTime(host, until)

        verify(sharedPreferencesModel, times(1)).putLong(ArgumentMatchers.contains(host), ArgumentMatchers.anyLong())
        Assert.assertEquals(host, disabled)
    }

    @Test
    fun `cancel url of open in new tab is set`() {
        val redirectUri = "https://app.pay.redirect.net"
        val cancelUrl = "https://cancel.url.net"

        viewModel.openInNewTab(redirectUri, cancelUrl)
        assertEquals(cancelUrl, viewModel.url.value?.getContentIfNotHandled())
    }
}
