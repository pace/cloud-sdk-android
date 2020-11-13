package cloud.pace.sdk.appkit.communication

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import cloud.pace.sdk.utils.Event
import cloud.pace.sdk.utils.Log

interface AppEventManager {

    val invalidApps: LiveData<List<String>>
    val appDrawerInfo: LiveData<AppDrawerInfo>
    val disabledHost: LiveData<String>
    val redirectUrl: LiveData<Event<String>>

    fun onAppDrawerChanged(url: String, title: String?, subtitle: String?)
    fun setInvalidApps(list: List<String>)
    fun setDisabledHost(host: String)
    fun onReceivedRedirect(url: String)

    data class AppDrawerInfo(val url: String, val title: String?, val subtitle: String?)
}

class AppEventManagerImpl : AppEventManager {

    override val invalidApps = MutableLiveData<List<String>>()
    override val appDrawerInfo = MutableLiveData<AppEventManager.AppDrawerInfo>()
    override val disabledHost = MutableLiveData<String>()
    override val redirectUrl = MutableLiveData<Event<String>>()

    override fun onAppDrawerChanged(url: String, title: String?, subtitle: String?) {
        appDrawerInfo.value = AppEventManager.AppDrawerInfo(url, title, subtitle)
    }

    override fun setInvalidApps(list: List<String>) {
        Log.d("Remove outdated AppDrawers: $list")
        invalidApps.value = list
    }

    override fun setDisabledHost(host: String) {
        Log.d("Remove disabled AppDrawer: $host")
        disabledHost.value = host
    }

    override fun onReceivedRedirect(url: String) {
        redirectUrl.value = Event(url)
    }
}
