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
import cloud.pace.sdk.appkit.communication.generated.model.request.OpenURLInNewTabRequest
import cloud.pace.sdk.utils.Event

abstract class AppFragmentViewModel : ViewModel() {

    abstract val closeEvent: LiveData<Event<Unit>>
    abstract val openUrlInNewTab: LiveData<Event<OpenURLInNewTabRequest>>

    abstract fun isChromeCustomTabsSupported(context: Context): Boolean
}

class AppFragmentViewModelImpl(
    private val eventManager: AppEventManager,
    private val appModel: AppModel
) : AppFragmentViewModel() {

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
    }

    companion object {
        const val CHROME_PACKAGE_NAME = "com.android.chrome"
    }
}
