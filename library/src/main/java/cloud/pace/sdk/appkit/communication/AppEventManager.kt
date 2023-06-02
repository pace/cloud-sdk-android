package cloud.pace.sdk.appkit.communication

import cloud.pace.sdk.utils.Event
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import timber.log.Timber

interface AppEventManager {

    val disabledHost: StateFlow<Event<String>?>

    fun setDisabledHost(host: String)
}

class AppEventManagerImpl : AppEventManager {

    override val disabledHost = MutableStateFlow<Event<String>?>(null)

    override fun setDisabledHost(host: String) {
        Timber.d("Remove disabled AppDrawer: $host")
        disabledHost.value = Event(host)
    }
}
