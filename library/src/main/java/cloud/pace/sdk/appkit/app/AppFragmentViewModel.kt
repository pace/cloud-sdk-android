package cloud.pace.sdk.appkit.app

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import cloud.pace.sdk.appkit.communication.AppModel
import cloud.pace.sdk.appkit.communication.generated.model.request.OpenURLInNewTabRequest
import cloud.pace.sdk.utils.Event

abstract class AppFragmentViewModel : ViewModel() {

    abstract val closeEvent: LiveData<Event<Unit>>
    abstract val openUrlInNewTab: LiveData<Event<OpenURLInNewTabRequest>>
}

class AppFragmentViewModelImpl(private val appModel: AppModel) : AppFragmentViewModel() {

    override val closeEvent = MutableLiveData<Event<Unit>>()
    override val openUrlInNewTab = MutableLiveData<Event<OpenURLInNewTabRequest>>()

    private val closeObserver = Observer<Unit> {
        closeEvent.value = Event(it)
    }

    private val openUrlInNewTabObserver = Observer<OpenURLInNewTabRequest> {
        openUrlInNewTab.value = Event(it)
    }

    init {
        appModel.close.observeForever(closeObserver)
        appModel.openUrlInNewTab.observeForever(openUrlInNewTabObserver)
    }

    override fun onCleared() {
        super.onCleared()

        appModel.close.removeObserver(closeObserver)
        appModel.openUrlInNewTab.removeObserver(openUrlInNewTabObserver)
    }

    companion object {
        const val CHROME_PACKAGE_NAME = "com.android.chrome"
    }
}
