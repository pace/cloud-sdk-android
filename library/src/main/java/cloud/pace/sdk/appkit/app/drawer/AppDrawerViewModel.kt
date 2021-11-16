package cloud.pace.sdk.appkit.app.drawer

import android.graphics.Bitmap
import android.graphics.Color
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import cloud.pace.sdk.appkit.communication.AppEventManager
import cloud.pace.sdk.appkit.model.App
import cloud.pace.sdk.utils.Event
import java.net.MalformedURLException
import java.net.URL

abstract class AppDrawerViewModel : ViewModel() {

    abstract val url: LiveData<String>
    abstract val title: LiveData<String>
    abstract val subtitle: LiveData<String?>
    abstract val background: LiveData<Int>
    abstract val iconBackground: LiveData<Int>
    abstract val textColor: LiveData<Int>
    abstract val logo: LiveData<Bitmap>
    abstract val closeEvent: LiveData<Event<Unit>>

    abstract fun init(app: App, darkBackground: Boolean)
    abstract fun onCreate()
    abstract fun onDestroy()
}

class AppDrawerViewModelImpl(private val eventManager: AppEventManager) : AppDrawerViewModel() {

    override val url = MutableLiveData<String>()
    override val title = MutableLiveData<String>()
    override val subtitle = MutableLiveData<String?>()
    override val background = MutableLiveData<Int>()
    override val iconBackground = MutableLiveData<Int>()
    override val textColor = MutableLiveData<Int>()
    override val logo = MutableLiveData<Bitmap>()
    override val closeEvent = MutableLiveData<Event<Unit>>()

    private val invalidAppsObserver = Observer<List<String>> {
        if (it.contains(url.value)) {
            closeEvent.value = Event(Unit)
        }
    }

    private val disabledHostObserver = Observer<String> {
        try {
            val host = URL(url.value).host
            if (it == host) {
                closeEvent.value = Event(Unit)
            }
        } catch (e: MalformedURLException) {
            return@Observer
        }
    }

    override fun init(app: App, darkBackground: Boolean) {
        url.value = app.url
        title.value = app.name
        subtitle.value = app.description

        app.iconBackgroundColor?.let {
            try {
                val backgroundColor = Color.parseColor(it)
                iconBackground.value = backgroundColor
            } catch (e: IllegalArgumentException) {
            }
        }

        app.logo?.let { logo.value = it }

        var textBackgroundColorInt: Int? = null
        var textColorInt: Int? = null

        if (app.textBackgroundColor != null && app.textColor != null) {
            try {
                textBackgroundColorInt = Color.parseColor(app.textBackgroundColor)
                textColorInt = Color.parseColor(app.textColor)
            } catch (e: IllegalArgumentException) {
            }
        }

        background.value = textBackgroundColorInt ?: if (darkBackground) Color.BLACK else Color.WHITE
        textColor.value = textColorInt ?: if (darkBackground) Color.WHITE else Color.BLACK
    }

    override fun onCreate() {
        eventManager.invalidApps.observeForever(invalidAppsObserver)
        eventManager.disabledHost.observeForever(disabledHostObserver)
    }

    override fun onDestroy() {
        eventManager.invalidApps.removeObserver(invalidAppsObserver)
        eventManager.disabledHost.removeObserver(disabledHostObserver)
    }
}
