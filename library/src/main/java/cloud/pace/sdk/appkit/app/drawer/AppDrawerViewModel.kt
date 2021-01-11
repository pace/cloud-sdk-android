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

    abstract val title: LiveData<String>
    abstract val subtitle: LiveData<String>
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

    override val title = MutableLiveData<String>()
    override val subtitle = MutableLiveData<String>()
    override val background = MutableLiveData<Int>()
    override val iconBackground = MutableLiveData<Int>()
    override val textColor = MutableLiveData<Int>()
    override val logo = MutableLiveData<Bitmap>()
    override val closeEvent = MutableLiveData<Event<Unit>>()

    private var url: String? = null
    private var initialTitle: String? = null
    private var initialSubtitle: String? = null

    private val invalidAppsObserver = Observer<List<String>> {
        if (it.contains(url)) {
            closeEvent.value = Event(Unit)
        }
    }

    private val disabledHostObserver = Observer<String> {
        try {
            val host = URL(url).host
            if (it == host) {
                closeEvent.value = Event(Unit)
            }
        } catch (e: MalformedURLException) {
            return@Observer
        }
    }

    private val buttonChangedObserver = Observer<AppEventManager.AppDrawerInfo> {
        if (url != it.url) return@Observer

        title.value = it.title ?: initialTitle
        subtitle.value = it.subtitle ?: initialSubtitle
    }

    override fun init(app: App, darkBackground: Boolean) {
        url = app.url
        initialTitle = app.name
        initialSubtitle = app.description
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

        when {
            textBackgroundColorInt != null && textColorInt != null -> {
                background.value = textBackgroundColorInt
                textColor.value = textColorInt
            }
            darkBackground -> {
                background.value = Color.BLACK
                textColor.value = Color.WHITE
            }
            else -> {
                background.value = Color.WHITE
                textColor.value = Color.BLACK
            }
        }
    }

    override fun onCreate() {
        eventManager.invalidApps.observeForever(invalidAppsObserver)
        eventManager.appDrawerInfo.observeForever(buttonChangedObserver)
        eventManager.disabledHost.observeForever(disabledHostObserver)
    }

    override fun onDestroy() {
        eventManager.invalidApps.removeObserver(invalidAppsObserver)
        eventManager.appDrawerInfo.removeObserver(buttonChangedObserver)
        eventManager.disabledHost.removeObserver(disabledHostObserver)
    }
}
