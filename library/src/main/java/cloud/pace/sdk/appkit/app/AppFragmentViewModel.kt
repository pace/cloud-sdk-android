package cloud.pace.sdk.appkit.app

import android.content.Context
import android.content.Intent
import androidx.browser.customtabs.CustomTabsService.ACTION_CUSTOM_TABS_CONNECTION
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import cloud.pace.sdk.appkit.communication.AppEventManager
import cloud.pace.sdk.appkit.communication.AppModel
import cloud.pace.sdk.utils.Event

abstract class AppFragmentViewModel : ViewModel() {

    abstract val closeEvent: LiveData<Event<Pair<Boolean, List<String>?>>>
    abstract val openUrlInNewTab: LiveData<Event<String>>
    abstract val redirectEvent: LiveData<Event<String>>

    abstract fun isChromeCustomTabsSupported(context: Context): Boolean
}

class AppFragmentViewModelImpl(
    private val eventManager: AppEventManager,
    private val appModel: AppModel
) : AppFragmentViewModel() {

    override val closeEvent = MutableLiveData<Event<Pair<Boolean, List<String>?>>>()
    override val redirectEvent = MutableLiveData<Event<String>>()
    override val openUrlInNewTab = MutableLiveData<Event<String>>()

    private val closeObserver = Observer<Pair<Boolean, List<String>?>> {
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

    override fun isChromeCustomTabsSupported(context: Context): Boolean {
        val serviceIntent = Intent(ACTION_CUSTOM_TABS_CONNECTION)
        serviceIntent.setPackage(CHROME_PACKAGE_NAME)
        val resolveInfos = context.packageManager.queryIntentServices(serviceIntent, 0)
        return resolveInfos.isNotEmpty()
    }


    override fun onCleared() {
        super.onCleared()

        appModel.close.removeObserver(closeObserver)
        appModel.openUrlInNewTab.removeObserver(openUrlInNewTabObserver)
        eventManager.redirectUrl.removeObserver(redirectObserver)
    }

    companion object {
        const val CHROME_PACKAGE_NAME = "com.android.chrome"
    }
}
