package cloud.pace.sdk.appkit.app

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import cloud.pace.sdk.appkit.app.webview.AppWebViewModel
import cloud.pace.sdk.appkit.communication.AppModel
import cloud.pace.sdk.appkit.communication.LogoutResponse
import cloud.pace.sdk.appkit.communication.generated.model.request.OpenURLInNewTabRequest
import cloud.pace.sdk.utils.Completion
import cloud.pace.sdk.utils.Event

abstract class AppActivityViewModel : ViewModel() {

    abstract val closeEvent: LiveData<Event<Unit>>
    abstract val openUrlInNewTab: LiveData<Event<OpenURLInNewTabRequest>>
    abstract val biometricRequest: LiveData<Event<AppWebViewModel.BiometricRequest>>
    abstract val authorize: LiveData<Event<AppModel.Result<Completion<String?>>>>
    abstract val endSession: LiveData<Event<AppModel.Result<LogoutResponse>>>

    abstract fun onLogin(context: Context, result: Completion<String?>)
}

class AppActivityViewModelImpl(private val appModel: AppModel) : AppActivityViewModel() {

    override val closeEvent = MutableLiveData<Event<Unit>>()
    override val openUrlInNewTab = MutableLiveData<Event<OpenURLInNewTabRequest>>()
    override val biometricRequest = MutableLiveData<Event<AppWebViewModel.BiometricRequest>>()
    override val authorize = MutableLiveData<Event<AppModel.Result<Completion<String?>>>>()
    override val endSession = MutableLiveData<Event<AppModel.Result<LogoutResponse>>>()

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

    init {
        appModel.reset()
        appModel.close.observeForever(closeObserver)
        appModel.openUrlInNewTab.observeForever(openUrlInNewTabObserver)
        appModel.biometricRequest.observeForever(biometricRequestObserver)
        appModel.authorize.observeForever(authorizeObserver)
        appModel.endSession.observeForever(endSessionObserver)
    }

    override fun onCleared() {
        super.onCleared()

        appModel.close.removeObserver(closeObserver)
        appModel.openUrlInNewTab.removeObserver(openUrlInNewTabObserver)
        appModel.biometricRequest.removeObserver(biometricRequestObserver)
        appModel.authorize.removeObserver(authorizeObserver)
        appModel.endSession.removeObserver(endSessionObserver)
    }

    override fun onLogin(context: Context, result: Completion<String?>) {
        appModel.onLogin(context, result)
    }
}
