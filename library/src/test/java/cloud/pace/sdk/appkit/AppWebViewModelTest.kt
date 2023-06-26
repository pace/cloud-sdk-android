package cloud.pace.sdk.appkit

import android.content.Context
import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import cloud.pace.sdk.PACECloudSDK
import cloud.pace.sdk.appkit.app.webview.AppWebViewModel
import cloud.pace.sdk.appkit.app.webview.AppWebViewModelImpl
import cloud.pace.sdk.appkit.communication.AppCallbackImpl
import cloud.pace.sdk.appkit.communication.AppModelImpl
import cloud.pace.sdk.appkit.communication.generated.model.request.DisableRequest
import cloud.pace.sdk.appkit.communication.generated.model.request.GetSecureDataRequest
import cloud.pace.sdk.appkit.communication.generated.model.request.GetTOTPRequest
import cloud.pace.sdk.appkit.communication.generated.model.request.LogEventRequest
import cloud.pace.sdk.appkit.communication.generated.model.request.SetSecureDataRequest
import cloud.pace.sdk.appkit.communication.generated.model.request.SetTOTPRequest
import cloud.pace.sdk.appkit.communication.generated.model.request.SetUserPropertyRequest
import cloud.pace.sdk.appkit.communication.generated.model.response.GetBiometricStatusResponse
import cloud.pace.sdk.appkit.communication.generated.model.response.GetBiometricStatusResult
import cloud.pace.sdk.appkit.communication.generated.model.response.GetSecureDataResponse
import cloud.pace.sdk.appkit.communication.generated.model.response.GetSecureDataResult
import cloud.pace.sdk.appkit.communication.generated.model.response.GetTOTPError
import cloud.pace.sdk.appkit.communication.generated.model.response.GetTOTPResponse
import cloud.pace.sdk.appkit.communication.generated.model.response.GetTOTPResult
import cloud.pace.sdk.appkit.pay.PayAuthenticationManager
import cloud.pace.sdk.appkit.persistence.SharedPreferencesImpl
import cloud.pace.sdk.appkit.persistence.SharedPreferencesImpl.Companion.getTotpSecretPreferenceKey
import cloud.pace.sdk.appkit.persistence.SharedPreferencesModel
import cloud.pace.sdk.appkit.persistence.TotpSecret
import cloud.pace.sdk.appkit.persistence.UserRelatedConstants
import cloud.pace.sdk.appkit.utils.CoroutineTestRule
import cloud.pace.sdk.appkit.utils.EncryptionUtils
import cloud.pace.sdk.appkit.utils.TestAppEventManager
import cloud.pace.sdk.utils.Configuration
import cloud.pace.sdk.utils.Environment
import cloud.pace.sdk.utils.LocationProvider
import io.mockk.every
import io.mockk.mockkObject
import io.mockk.slot
import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertNull
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.O_MR1])
class AppWebViewModelTest {

    @get:Rule
    val coroutineTestRule = CoroutineTestRule()

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val context = mock(Context::class.java)
    private val sharedPreferencesModel = mock(SharedPreferencesModel::class.java)
    private val payManager = mock(PayAuthenticationManager::class.java)
    private var disabled = ""
    private val host = "pace.cloud"
    private val startUrl = "https://$host"
    private val payHost = "pay.$host"
    private val eventManager = object : TestAppEventManager() {
        override fun setDisabledHost(host: String) {
            disabled = host
        }
    }
    private val appCallback = mock(AppCallbackImpl::class.java)
    private val appModel = AppModelImpl(context, coroutineTestRule.testDispatcherProvider)
    private val viewModel = AppWebViewModelImpl(context, sharedPreferencesModel, eventManager, payManager, appModel, mock(LocationProvider::class.java))

    @Before
    fun init() {
        PACECloudSDK.configuration = Configuration("", "", "", "", environment = Environment.DEVELOPMENT, oidConfiguration = null)

        appModel.callback = appCallback
        disabled = ""

        // mock encryption so that it does not do anything
        mockkObject(EncryptionUtils)
        val slot = slot<String>()
        val slot2 = slot<String>()
        every { EncryptionUtils.encrypt(capture(slot)) } answers { slot.captured }
        every { EncryptionUtils.decrypt(capture(slot2)) } answers { slot2.captured }

        viewModel.init(startUrl)
        viewModel.currentUrl.value = "https://$payHost"
    }

    @Test
    fun `open app with passed URL`() {
        assertEquals(startUrl, viewModel.init.value?.getContentIfNotHandled())
    }

    @Test
    fun `close app`() = runBlocking {
        val result = viewModel.close(5000)

        verify(appCallback, times(1)).onClose()
        assertEquals(204, result.status)
        assertNull(result.body)
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
    fun `has biometrics`() = runBlocking {
        `when`(payManager.isFingerprintAvailable()).thenReturn(true)

        val result = viewModel.getBiometricStatus(5000)
        assertEquals(200, result.status)
        assertEquals(GetBiometricStatusResult.Success(GetBiometricStatusResponse(true)).response, result.body)
    }

    @Test
    fun `does not have biometrics`() = runBlocking {
        `when`(payManager.isFingerprintAvailable()).thenReturn(false)

        viewModel.getBiometricStatus(5000)

        val result = viewModel.getBiometricStatus(5000)
        assertEquals(200, result.status)
        assertEquals(GetBiometricStatusResult.Success(GetBiometricStatusResponse(false)).response, result.body)
    }

    @Test
    fun `secret is saved`() = runBlocking {
        val secret = "KRUGS4ZANFZSAYJAOZSXE6JAONSWG4TFOQQHGZLDOJSXIIJB"
        val key = "fueling-app"
        val period = 3600
        val digits = 14
        val algorithm = "SHA1"

        val result = viewModel.setTOTP(120000, SetTOTPRequest(secret, period, digits, algorithm, key))

        verify(sharedPreferencesModel, times(1)).setTotpSecret(payHost, key, TotpSecret(secret, digits, period, algorithm))
        assertEquals(200, result.status)
        assertNull(result.body)
    }

    @Test
    fun `secret is fetched`() = runBlocking {
        val secret = "KRUGS4ZANFZSAYJAOZSXE6JAONSWG4TFOQQHGZLDOJSXIIJB"
        val key = "fueling-app"
        val period = 3600
        val digits = 14
        val algorithm = "SHA1"

        val observer: Observer<AppWebViewModel.BiometricRequest> = Observer {
            it.onSuccess()
        }
        appModel.biometricRequest.observeForever(observer)

        `when`(payManager.isFingerprintAvailable()).thenReturn(true)
        `when`(sharedPreferencesModel.getString(getTotpSecretPreferenceKey(UserRelatedConstants.SECRET.value, payHost, key))).thenReturn(secret)
        `when`(sharedPreferencesModel.getString(getTotpSecretPreferenceKey(UserRelatedConstants.ALGORITHM.value, payHost, key))).thenReturn(algorithm)
        `when`(sharedPreferencesModel.getInt(getTotpSecretPreferenceKey(UserRelatedConstants.DIGITS.value, payHost, key))).thenReturn(digits)
        `when`(sharedPreferencesModel.getInt(getTotpSecretPreferenceKey(UserRelatedConstants.PERIOD.value, payHost, key))).thenReturn(period)
        `when`(sharedPreferencesModel.getTotpSecret(payHost, key)).thenReturn(TotpSecret(secret, digits, period, algorithm))

        val result = viewModel.getTOTP(120000, GetTOTPRequest(1611158191.0, key))
        assertEquals(200, result.status)
        assertEquals(GetTOTPResult.Success(GetTOTPResponse("00000865350714", AppWebViewModel.BiometryMethod.OTHER.value)).response, result.body)

        appModel.biometricRequest.removeObserver(observer)
    }

    @Test
    fun `biometric authentication failed`() = runBlocking {
        val secret = "KRUGS4ZANFZSAYJAOZSXE6JAONSWG4TFOQQHGZLDOJSXIIJB"
        val key = "fueling-app"
        val period = 3600
        val digits = 14
        val algorithm = "SHA1"
        val errorCode = 500
        val errString = "What a terrible failure!"

        val observer: Observer<AppWebViewModel.BiometricRequest> = Observer {
            it.onFailure(errorCode, errString)
        }
        appModel.biometricRequest.observeForever(observer)

        `when`(payManager.isFingerprintAvailable()).thenReturn(true)
        `when`(sharedPreferencesModel.getString(getTotpSecretPreferenceKey(UserRelatedConstants.SECRET.value, payHost, key))).thenReturn(secret)
        `when`(sharedPreferencesModel.getString(getTotpSecretPreferenceKey(UserRelatedConstants.ALGORITHM.value, payHost, key))).thenReturn(algorithm)
        `when`(sharedPreferencesModel.getInt(getTotpSecretPreferenceKey(UserRelatedConstants.DIGITS.value, payHost, key))).thenReturn(digits)
        `when`(sharedPreferencesModel.getInt(getTotpSecretPreferenceKey(UserRelatedConstants.PERIOD.value, payHost, key))).thenReturn(period)
        `when`(sharedPreferencesModel.getTotpSecret(payHost, key)).thenReturn(TotpSecret(secret, period, digits, algorithm))

        val result = viewModel.getTOTP(120000, GetTOTPRequest(1611158191.0, key))
        assertEquals(401, result.status)
        assertEquals(
            GetTOTPResult.Failure(GetTOTPResult.Failure.StatusCode.Unauthorized, GetTOTPError("Biometric authentication failed: errorCode was $errorCode, errString was $errString")).response,
            result.body
        )

        appModel.biometricRequest.removeObserver(observer)
    }

    @Test
    fun `secret exists but biometry not available`() = runBlocking {
        val observer: Observer<AppWebViewModel.BiometricRequest> = Observer {
            it.onSuccess()
        }
        appModel.biometricRequest.observeForever(observer)

        `when`(payManager.isFingerprintAvailable()).thenReturn(false)

        val result = viewModel.getTOTP(120000, GetTOTPRequest(1611158191.0, "fueling-app"))
        assertEquals(405, result.status)
        assertEquals(
            GetTOTPResult.Failure(GetTOTPResult.Failure.StatusCode.MethodNotAllowed, GetTOTPError("No biometric authentication is available or none has been set.")).response,
            result.body
        )

        appModel.biometricRequest.removeObserver(observer)
    }

    @Test
    fun `secret not found`() = runBlocking {
        val key = "fueling-app"
        val period = 3600
        val digits = 14
        val algorithm = "SHA1"

        val observer: Observer<AppWebViewModel.BiometricRequest> = Observer {
            it.onSuccess()
        }
        appModel.biometricRequest.observeForever(observer)

        `when`(payManager.isFingerprintAvailable()).thenReturn(true)
        `when`(sharedPreferencesModel.getString(getTotpSecretPreferenceKey(UserRelatedConstants.SECRET.value, payHost, key))).thenReturn(null)
        `when`(sharedPreferencesModel.getString(getTotpSecretPreferenceKey(UserRelatedConstants.ALGORITHM.value, payHost, key))).thenReturn(algorithm)
        `when`(sharedPreferencesModel.getInt(getTotpSecretPreferenceKey(UserRelatedConstants.DIGITS.value, payHost, key))).thenReturn(digits)
        `when`(sharedPreferencesModel.getInt(getTotpSecretPreferenceKey(UserRelatedConstants.PERIOD.value, payHost, key))).thenReturn(period)
        `when`(sharedPreferencesModel.getTotpSecret(payHost, key)).thenReturn(null)

        val result = viewModel.getTOTP(120000, GetTOTPRequest(1611158191.0, key))
        assertEquals(404, result.status)
        assertEquals(
            GetTOTPResult.Failure(GetTOTPResult.Failure.StatusCode.NotFound, GetTOTPError("No biometric data found for host $payHost was found in the SharedPreferences.")).response,
            result.body
        )

        appModel.biometricRequest.removeObserver(observer)
    }

    @Test
    fun `save secure data`() = runBlocking {
        val key = "fueling-app"
        val value = "encryptedValue"

        val result = viewModel.setSecureData(5000, SetSecureDataRequest(key, value))
        verify(sharedPreferencesModel, times(1)).putString(SharedPreferencesImpl.getSecureDataPreferenceKey(payHost, key), value)
        assertEquals(200, result.status)
        assertNull(result.body)
    }

    @Test
    fun `get secure data`() = runBlocking {
        val key = "fueling-app"
        val value = "encryptedValue"

        val observer: Observer<AppWebViewModel.BiometricRequest> = Observer {
            it.onSuccess()
        }
        appModel.biometricRequest.observeForever(observer)

        `when`(payManager.isFingerprintAvailable()).thenReturn(true)
        `when`(sharedPreferencesModel.getString(SharedPreferencesImpl.getSecureDataPreferenceKey(payHost, key))).thenReturn(value)

        val result = viewModel.getSecureData(120000, GetSecureDataRequest(key))
        assertEquals(200, result.status)
        assertEquals(GetSecureDataResult.Success(GetSecureDataResponse(value)).response, result.body)

        appModel.biometricRequest.removeObserver(observer)
    }

    @Test
    fun `set disable timestamp`() = runBlocking {
        val until = 1597143588148L

        val result = viewModel.disable(5000, DisableRequest(until.toDouble()))

        verify(sharedPreferencesModel, times(1)).putLong(SharedPreferencesImpl.getDisableTimePreferenceKey(payHost), until)
        assertEquals(payHost, disabled)
        assertEquals(204, result.status)
        assertNull(result.body)
    }

    @Test
    fun `set user property`() = runBlocking {
        val key = "foo"
        val value = "bar"

        val result = viewModel.setUserProperty(5000, SetUserPropertyRequest(key, value, false))

        verify(appCallback, times(1)).setUserProperty(key, value, false)
        assertEquals(204, result.status)
        assertNull(result.body)
    }

    @Test
    fun `log event`() = runBlocking {
        val key = "foo"
        val parameters = mapOf("string" to "value", "number" to 1.0, "boolean" to true, "list" to listOf("element1", 3.0, false))

        val result = viewModel.logEvent(5000, LogEventRequest(key, parameters))

        verify(appCallback, times(1)).logEvent(key, parameters)
        assertEquals(204, result.status)
        assertNull(result.body)
    }
}
