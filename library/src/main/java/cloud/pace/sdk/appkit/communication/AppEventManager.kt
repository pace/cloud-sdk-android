package cloud.pace.sdk.appkit.communication

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import cloud.pace.sdk.utils.onMainThread
import timber.log.Timber

interface AppEventManager {

    val invalidApps: LiveData<List<String>>
    val disabledHost: LiveData<String>

    fun setInvalidApps(list: List<String>)
    fun setDisabledHost(host: String)
}

class AppEventManagerImpl : AppEventManager {

    override val invalidApps = MutableLiveData<List<String>>()
    override val disabledHost = MutableLiveData<String>()

    override fun setInvalidApps(list: List<String>) {
        Timber.d("Remove outdated AppDrawers: $list")
        onMainThread {
            invalidApps.value = list
        }
    }

    override fun setDisabledHost(host: String) {
        Timber.d("Remove disabled AppDrawer: $host")
        onMainThread {
            disabledHost.value = host
        }
    }
}
