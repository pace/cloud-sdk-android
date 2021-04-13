package cloud.pace.sdk.appkit.communication

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import cloud.pace.sdk.utils.Event
import timber.log.Timber

interface AppEventManager {

    val invalidApps: LiveData<List<String>>
    val disabledHost: LiveData<String>
    val redirectUrl: LiveData<Event<String>>

    fun setInvalidApps(list: List<String>)
    fun setDisabledHost(host: String)
    fun onReceivedRedirect(url: String)
}

class AppEventManagerImpl : AppEventManager {

    override val invalidApps = MutableLiveData<List<String>>()
    override val disabledHost = MutableLiveData<String>()
    override val redirectUrl = MutableLiveData<Event<String>>()

    override fun setInvalidApps(list: List<String>) {
        Timber.d("Remove outdated AppDrawers: $list")
        invalidApps.value = list
    }

    override fun setDisabledHost(host: String) {
        Timber.d("Remove disabled AppDrawer: $host")
        disabledHost.value = host
    }

    override fun onReceivedRedirect(url: String) {
        redirectUrl.value = Event(url)
    }
}
