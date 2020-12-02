package cloud.pace.sdk.appkit.app

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import cloud.pace.sdk.appkit.communication.AppEventManager
import cloud.pace.sdk.appkit.communication.AppModel
import cloud.pace.sdk.utils.Event

abstract class AppFragmentViewModel : ViewModel() {

    abstract val closeEvent: LiveData<Event<Boolean>>
    abstract val openUrlInNewTab: LiveData<Event<String>>
    abstract val redirectEvent: LiveData<Event<String>>
}

class AppFragmentViewModelImpl(
    private val eventManager: AppEventManager,
    private val appModel: AppModel
) : AppFragmentViewModel() {

    override val closeEvent = MutableLiveData<Event<Boolean>>()
    override val redirectEvent = MutableLiveData<Event<String>>()
    override val openUrlInNewTab = MutableLiveData<Event<String>>()

    private val closeObserver = Observer<Boolean> {
        closeEvent.value = Event(it)
    }

    private val openUrlInNewTabObserver = Observer<String> {
        openUrlInNewTab.value = Event(it)
    }

    private val redirectObserver = Observer<Event<String>> {
        it.getContentIfNotHandled()?.let { redirectUrl ->
            redirectEvent.value = Event(redirectUrl)
        }
    }

    init {
        appModel.close.observeForever(closeObserver)
        appModel.openUrlInNewTab.observeForever(openUrlInNewTabObserver)
        eventManager.redirectUrl.observeForever(redirectObserver)
    }

    override fun onCleared() {
        super.onCleared()

        appModel.close.removeObserver(closeObserver)
        appModel.openUrlInNewTab.removeObserver(openUrlInNewTabObserver)
        eventManager.redirectUrl.removeObserver(redirectObserver)
    }
}
