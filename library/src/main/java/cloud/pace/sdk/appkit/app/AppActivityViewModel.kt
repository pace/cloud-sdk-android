package cloud.pace.sdk.appkit.app

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import cloud.pace.sdk.appkit.app.webview.AppWebViewModel
import cloud.pace.sdk.appkit.communication.AppModel
import cloud.pace.sdk.appkit.communication.LogoutResponse
import cloud.pace.sdk.appkit.communication.generated.model.request.GooglePayAvailabilityCheckRequest
import cloud.pace.sdk.appkit.communication.generated.model.request.GooglePayPaymentRequest
import cloud.pace.sdk.appkit.communication.generated.model.request.OpenURLInNewTabRequest
import cloud.pace.sdk.appkit.communication.generated.model.response.GooglePayAvailabilityCheckResponse
import cloud.pace.sdk.appkit.communication.generated.model.response.GooglePayPaymentResponse
import cloud.pace.sdk.utils.Completion
import cloud.pace.sdk.utils.Event

abstract class AppActivityViewModel : ViewModel() {

    abstract val closeEvent: LiveData<Event<Unit>>
    abstract val openUrlInNewTab: LiveData<Event<OpenURLInNewTabRequest>>
    abstract val biometricRequest: LiveData<Event<AppWebViewModel.BiometricRequest>>
    abstract val authorize: LiveData<Event<AppModel.Result<Completion<String?>>>>
    abstract val endSession: LiveData<Event<AppModel.Result<LogoutResponse>>>
    abstract val googlePayAvailabilityCheck: LiveData<Event<Pair<GooglePayAvailabilityCheckRequest, (Completion<GooglePayAvailabilityCheckResponse>) -> Unit>>>
    abstract val googlePayPayment: LiveData<Event<Pair<GooglePayPaymentRequest, (Completion<GooglePayPaymentResponse>) -> Unit>>>

    abstract fun onLogin(context: Context, result: Completion<String?>)
}

class AppActivityViewModelImpl(private val appModel: AppModel) : AppActivityViewModel() {

    override val closeEvent = MutableLiveData<Event<Unit>>()
    override val openUrlInNewTab = MutableLiveData<Event<OpenURLInNewTabRequest>>()
    override val biometricRequest = MutableLiveData<Event<AppWebViewModel.BiometricRequest>>()
    override val authorize = MutableLiveData<Event<AppModel.Result<Completion<String?>>>>()
    override val endSession = MutableLiveData<Event<AppModel.Result<LogoutResponse>>>()
    override val googlePayAvailabilityCheck = MutableLiveData<Event<Pair<GooglePayAvailabilityCheckRequest, (Completion<GooglePayAvailabilityCheckResponse>) -> Unit>>>()
    override val googlePayPayment = MutableLiveData<Event<Pair<GooglePayPaymentRequest, (Completion<GooglePayPaymentResponse>) -> Unit>>>()

    private val closeObserver = Observer<Unit> {
        closeEvent.value = Event(it)
    }

    private val openUrlInNewTabObserver = Observer<OpenURLInNewTabRequest> {
        openUrlInNewTab.value = Event(it)
    }

    private val biometricRequestObserver = Observer<AppWebViewModel.BiometricRequest> {
        biometricRequest.value = Event(it)
    }

    private val authorizeObserver = Observer<AppModel.Result<Completion<String?>>> {
        authorize.value = Event(it)
    }

    private val endSessionObserver = Observer<AppModel.Result<LogoutResponse>> {
        endSession.value = Event(it)
    }

    private val googlePayAvailabilityCheckObserver = Observer<Pair<GooglePayAvailabilityCheckRequest, (Completion<GooglePayAvailabilityCheckResponse>) -> Unit>> {
        googlePayAvailabilityCheck.value = Event(it)
    }

    private val googlePayPaymentObserver = Observer<Pair<GooglePayPaymentRequest, (Completion<GooglePayPaymentResponse>) -> Unit>> {
        googlePayPayment.value = Event(it)
    }

    init {
        appModel.reset()
        appModel.close.observeForever(closeObserver)
        appModel.openUrlInNewTab.observeForever(openUrlInNewTabObserver)
        appModel.biometricRequest.observeForever(biometricRequestObserver)
        appModel.authorize.observeForever(authorizeObserver)
        appModel.endSession.observeForever(endSessionObserver)
        appModel.googlePayAvailabilityCheckRequest.observeForever(googlePayAvailabilityCheckObserver)
        appModel.googlePayPayment.observeForever(googlePayPaymentObserver)
    }

    override fun onCleared() {
        super.onCleared()

        appModel.close.removeObserver(closeObserver)
        appModel.openUrlInNewTab.removeObserver(openUrlInNewTabObserver)
        appModel.biometricRequest.removeObserver(biometricRequestObserver)
        appModel.authorize.removeObserver(authorizeObserver)
        appModel.endSession.removeObserver(endSessionObserver)
        appModel.googlePayAvailabilityCheckRequest.removeObserver(googlePayAvailabilityCheckObserver)
        appModel.googlePayPayment.removeObserver(googlePayPaymentObserver)
    }

    override fun onLogin(context: Context, result: Completion<String?>) {
        appModel.onLogin(context, result)
    }
}
