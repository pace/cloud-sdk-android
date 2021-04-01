package cloud.pace.sdk.appkit

import android.content.Context
import android.os.Build
import androidx.lifecycle.Observer
import cloud.pace.sdk.PACECloudSDK
import cloud.pace.sdk.appkit.app.webview.AppWebViewModel
import cloud.pace.sdk.appkit.app.webview.AppWebViewModelImpl
import cloud.pace.sdk.appkit.app.webview.StatusCode
import cloud.pace.sdk.appkit.communication.AppCallbackImpl
import cloud.pace.sdk.appkit.communication.AppModelImpl
import cloud.pace.sdk.appkit.location.AppLocationManager
import cloud.pace.sdk.appkit.pay.PayAuthenticationManager
import cloud.pace.sdk.appkit.persistence.SharedPreferencesImpl
import cloud.pace.sdk.appkit.persistence.SharedPreferencesImpl.Companion.getTotpSecretPreferenceKey
import cloud.pace.sdk.appkit.persistence.SharedPreferencesModel
import cloud.pace.sdk.appkit.persistence.TotpSecret
import cloud.pace.sdk.appkit.utils.EncryptionUtils
import cloud.pace.sdk.appkit.utils.TestAppEventManager
import cloud.pace.sdk.utils.Configuration
import cloud.pace.sdk.utils.Environment
import cloud.pace.sdk.utils.Event
import com.google.gson.Gson
import io.mockk.every
import io.mockk.mockkObject
import io.mockk.slot
import junit.framework.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.*
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.O_MR1])
class AppWebViewModelTest {

    private val context = mock(Context::class.java)
    private val sharedPreferencesModel = mock(SharedPreferencesModel::class.java)
    private val payManager = mock(PayAuthenticationManager::class.java)
    private var disabled = ""
    private val host = "pace.cloud"
    private val url = "https://$host"
    private val eventManager = object : TestAppEventManager() {
        override fun setDisabledHost(host: String) {
            disabled = host
        }
    }
    private val appCallback = mock(AppCallbackImpl::class.java)
    private val appModel = AppModelImpl()
    private val viewModel = AppWebViewModelImpl(context, sharedPreferencesModel, eventManager, payManager, appModel, mock(AppLocationManager::class.java))

    @Before
    fun init() {
        PACECloudSDK.configuration = Configuration("", "", "", "", environment = Environment.DEVELOPMENT, domainACL = listOf(host))

        appModel.callback = appCallback
        disabled = ""

        // mock encryption so that it does not do anything
        mockkObject(EncryptionUtils)
        val slot = slot<String>()
        val slot2 = slot<String>()
        every { EncryptionUtils.encrypt(capture(slot)) } answers { slot.captured }
        every { EncryptionUtils.decrypt(capture(slot2)) } answers { slot2.captured }

        viewModel.init(url)
    }

    @Test
    fun `open app with passed URL`() {
        assertEquals(url, viewModel.url.value?.getContentIfNotHandled())
    }

    @Test
    fun `close app`() {
        viewModel.handleClose()

        verify(appCallback, times(1)).onClose()
    }

    @Test
    fun `app loading successful`() {
        viewModel.onSwitchErrorState(isError = false, isHttpError = false)

        assertEquals(false, viewModel.isInErrorState.value?.getContentIfNotHandled())
    }

    @Test
    fun `app loading failed`() {
        viewModel.onSwitchErrorState(isError = true, isHttpError = false)

        assertEquals(true, viewModel.isInErrorState.value?.getContentIfNotHandled())
    }

    @Test
    fun `has biometrics`() {
        `when`(payManager.isFingerprintAvailable()).thenReturn(true)

        viewModel.handleGetBiometricStatus(Gson().toJson(AppWebViewModel.MessageBundle<String>("id", "")))

        assertEquals("id", viewModel.isBiometricAvailable.value?.peekContent()?.id)
        assertEquals(true, viewModel.isBiometricAvailable.value?.getContentIfNotHandled()?.message)
    }

    @Test
    fun `does not have biometrics`() {
        `when`(payManager.isFingerprintAvailable()).thenReturn(false)

        viewModel.handleGetBiometricStatus(Gson().toJson(AppWebViewModel.MessageBundle<String>("id", "")))

        assertEquals("id", viewModel.isBiometricAvailable.value?.peekContent()?.id)
        assertEquals(false, viewModel.isBiometricAvailable.value?.getContentIfNotHandled()?.message)
    }

    @Test
    fun `secret is saved`() {
        val secret = "KRUGS4ZANFZSAYJAOZSXE6JAONSWG4TFOQQHGZLDOJSXIIJB"
        val key = "fueling-app"
        val period = 3600
        val digits = 14
        val algorithm = "SHA1"

        val totpRequest = Gson().toJson(AppWebViewModel.MessageBundle("id", AppWebViewModel.SetTOTPRequest(secret, period, digits, algorithm, key)))
        viewModel.handleSetTOTPSecret(totpRequest)

        verify(sharedPreferencesModel, times(1)).setTotpSecret(host, key, TotpSecret(secret, digits, period, algorithm))
        assertEquals("id", viewModel.statusCode.value?.peekContent()?.id)
        assertEquals(AppWebViewModel.StatusCodeResponse.Success, viewModel.statusCode.value?.getContentIfNotHandled()?.message)
    }

    @Test
    fun `secret is fetched`() {
        val secret = "KRUGS4ZANFZSAYJAOZSXE6JAONSWG4TFOQQHGZLDOJSXIIJB"
        val key = "fueling-app"
        val period = 3600
        val digits = 14
        val algorithm = "SHA1"

        val observer: Observer<Event<AppWebViewModel.BiometricRequest>> = Observer {
            val event = it.getContentIfNotHandled() ?: return@Observer
            event.onSuccess()
        }
        viewModel.biometricRequest.observeForever(observer)

        `when`(payManager.isFingerprintAvailable()).thenReturn(true)
        `when`(sharedPreferencesModel.getString(getTotpSecretPreferenceKey(SharedPreferencesImpl.SECRET, host, key))).thenReturn(secret)
        `when`(sharedPreferencesModel.getString(getTotpSecretPreferenceKey(SharedPreferencesImpl.ALGORITHM, host, key))).thenReturn(algorithm)
        `when`(sharedPreferencesModel.getInt(getTotpSecretPreferenceKey(SharedPreferencesImpl.DIGITS, host, key))).thenReturn(digits)
        `when`(sharedPreferencesModel.getInt(getTotpSecretPreferenceKey(SharedPreferencesImpl.PERIOD, host, key))).thenReturn(period)
        `when`(sharedPreferencesModel.getTotpSecret(host, key)).thenReturn(TotpSecret(secret, digits, period, algorithm))

        val totpRequest = Gson().toJson(AppWebViewModel.MessageBundle("id", AppWebViewModel.GetTOTPRequest(1611158191, key)))
        viewModel.handleGetTOTP(totpRequest)

        assertEquals("00000865350714", viewModel.totpResponse.value?.peekContent()?.message?.totp)
        assertEquals(AppWebViewModel.BiometryMethod.OTHER.value, viewModel.totpResponse.value?.peekContent()?.message?.biometryMethod)

        viewModel.biometricRequest.removeObserver(observer)
    }

    @Test
    fun `biometric authentication failed`() {
        val secret = "KRUGS4ZANFZSAYJAOZSXE6JAONSWG4TFOQQHGZLDOJSXIIJB"
        val key = "fueling-app"
        val period = 3600
        val digits = 14
        val algorithm = "SHA1"
        val errorCode = 500
        val errString = "What a terrible failure!"

        val observer: Observer<Event<AppWebViewModel.BiometricRequest>> = Observer {
            val event = it.getContentIfNotHandled() ?: return@Observer
            event.onFailure(errorCode, errString)
        }
        viewModel.biometricRequest.observeForever(observer)

        `when`(payManager.isFingerprintAvailable()).thenReturn(true)
        `when`(sharedPreferencesModel.getString(getTotpSecretPreferenceKey(SharedPreferencesImpl.SECRET, host, key))).thenReturn(secret)
        `when`(sharedPreferencesModel.getString(getTotpSecretPreferenceKey(SharedPreferencesImpl.ALGORITHM, host, key))).thenReturn(algorithm)
        `when`(sharedPreferencesModel.getInt(getTotpSecretPreferenceKey(SharedPreferencesImpl.DIGITS, host, key))).thenReturn(digits)
        `when`(sharedPreferencesModel.getInt(getTotpSecretPreferenceKey(SharedPreferencesImpl.PERIOD, host, key))).thenReturn(period)
        `when`(sharedPreferencesModel.getTotpSecret(host, key)).thenReturn(TotpSecret(secret, period, digits, algorithm))

        val totpRequest = Gson().toJson(AppWebViewModel.MessageBundle("id", AppWebViewModel.GetTOTPRequest(1611158191, key)))
        viewModel.handleGetTOTP(totpRequest)

        assertEquals("id", viewModel.statusCode.value?.peekContent()?.id)
        assertEquals(
            "Biometric authentication failed: errorCode was $errorCode, errString was $errString",
            (viewModel.statusCode.value?.peekContent()?.message as? AppWebViewModel.StatusCodeResponse.Failure)?.error
        )
        assertEquals(StatusCode.Unauthorized.code, viewModel.statusCode.value?.peekContent()?.message?.statusCode)

        viewModel.biometricRequest.removeObserver(observer)
    }

    @Test
    fun `secret exists but biometry not available`() {
        val observer: Observer<Event<AppWebViewModel.BiometricRequest>> = Observer {
            val event = it.getContentIfNotHandled() ?: return@Observer
            event.onSuccess()
        }
        viewModel.biometricRequest.observeForever(observer)

        `when`(payManager.isFingerprintAvailable()).thenReturn(false)

        val totpRequest = Gson().toJson(AppWebViewModel.MessageBundle("id", AppWebViewModel.GetTOTPRequest(1611158191, "fueling-app")))
        viewModel.handleGetTOTP(totpRequest)

        assertEquals("No biometric authentication is available or none has been set.", (viewModel.statusCode.value?.peekContent()?.message as? AppWebViewModel.StatusCodeResponse.Failure)?.error)
        assertEquals(StatusCode.NotAllowed.code, viewModel.statusCode.value?.peekContent()?.message?.statusCode)

        viewModel.biometricRequest.removeObserver(observer)
    }

    @Test
    fun `secret not found`() {
        val key = "fueling-app"
        val period = 3600
        val digits = 14
        val algorithm = "SHA1"

        val observer: Observer<Event<AppWebViewModel.BiometricRequest>> = Observer {
            val event = it.getContentIfNotHandled() ?: return@Observer
            event.onSuccess()
        }
        viewModel.biometricRequest.observeForever(observer)

        `when`(payManager.isFingerprintAvailable()).thenReturn(true)
        `when`(sharedPreferencesModel.getString(getTotpSecretPreferenceKey(SharedPreferencesImpl.SECRET, host, key))).thenReturn(null)
        `when`(sharedPreferencesModel.getString(getTotpSecretPreferenceKey(SharedPreferencesImpl.ALGORITHM, host, key))).thenReturn(algorithm)
        `when`(sharedPreferencesModel.getInt(getTotpSecretPreferenceKey(SharedPreferencesImpl.DIGITS, host, key))).thenReturn(digits)
        `when`(sharedPreferencesModel.getInt(getTotpSecretPreferenceKey(SharedPreferencesImpl.PERIOD, host, key))).thenReturn(period)
        `when`(sharedPreferencesModel.getTotpSecret(host, key)).thenReturn(null)

        val totpRequest = Gson().toJson(AppWebViewModel.MessageBundle("id", AppWebViewModel.GetTOTPRequest(1611158191, key)))
        viewModel.handleGetTOTP(totpRequest)

        assertEquals("id", viewModel.statusCode.value?.peekContent()?.id)
        assertEquals("No biometric data found in the SharedPreferences.", (viewModel.statusCode.value?.peekContent()?.message as? AppWebViewModel.StatusCodeResponse.Failure)?.error)
        assertEquals(StatusCode.NotFound.code, viewModel.statusCode.value?.peekContent()?.message?.statusCode)

        viewModel.biometricRequest.removeObserver(observer)
    }

    @Test
    fun `save secure data`() {
        val key = "fueling-app"
        val value = "encryptedValue"

        val secureDataRequest = Gson().toJson(AppWebViewModel.MessageBundle("id", AppWebViewModel.SetSecureDataRequest(key, value)))
        viewModel.handleSetSecureData(secureDataRequest)

        verify(sharedPreferencesModel, times(1)).putString(SharedPreferencesImpl.getSecureDataPreferenceKey(host, key), value)
        assertEquals(AppWebViewModel.StatusCodeResponse.Success, viewModel.statusCode.value?.getContentIfNotHandled()?.message)
    }

    @Test
    fun `get secure data`() {
        val key = "fueling-app"
        val value = "encryptedValue"

        val observer: Observer<Event<AppWebViewModel.BiometricRequest>> = Observer {
            val event = it.getContentIfNotHandled() ?: return@Observer
            event.onSuccess()
        }
        viewModel.biometricRequest.observeForever(observer)

        `when`(payManager.isFingerprintAvailable()).thenReturn(true)
        `when`(sharedPreferencesModel.getString(SharedPreferencesImpl.getSecureDataPreferenceKey(host, key))).thenReturn(value)

        viewModel.handleGetSecureData(Gson().toJson(AppWebViewModel.MessageBundle("id", mapOf("key" to key))))

        assertEquals(mapOf("value" to value), viewModel.secureData.value?.getContentIfNotHandled()?.message)

        viewModel.biometricRequest.removeObserver(observer)
    }

    @Test
    fun `set disable timestamp`() {
        val until = 1597143588148L

        viewModel.handleDisable(Gson().toJson(AppWebViewModel.MessageBundle("id", mapOf("until" to until))))

        verify(sharedPreferencesModel, times(1)).putLong(SharedPreferencesImpl.getDisableTimePreferenceKey(host), until)
        assertEquals(host, disabled)
        assertEquals(AppWebViewModel.StatusCodeResponse.Success, viewModel.statusCode.value?.getContentIfNotHandled()?.message)
    }

    @Test
    fun `cancel url of open in new tab is set`() {
        val redirectUri = "https://app.pay.redirect.net"
        val cancelUrl = "https://cancel.url.net"

        val openURLInNewTabRequest = Gson().toJson(AppWebViewModel.MessageBundle("id", AppWebViewModel.OpenURLInNewTabRequest(redirectUri, cancelUrl)))
        viewModel.handleOpenURLInNewTab(openURLInNewTabRequest)

        assertEquals(cancelUrl, viewModel.url.value?.getContentIfNotHandled())
    }
}
