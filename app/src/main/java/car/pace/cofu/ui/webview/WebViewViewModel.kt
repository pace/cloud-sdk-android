package car.pace.cofu.ui.webview

import android.webkit.WebChromeClient
import android.webkit.WebView
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import car.pace.cofu.core.events.FragmentEvent
import car.pace.cofu.core.mvvm.BaseViewModel

class WebViewViewModel : BaseViewModel() {
    val swipeRefreshLayoutRefreshing = ObservableBoolean(true)
    val title = ObservableField("")
    val url = ObservableField<String>()

    val webChromeClient = object : WebChromeClient() {
        override fun onProgressChanged(view: WebView?, newProgress: Int) {
            super.onProgressChanged(view, newProgress)

            if (newProgress < 100) {
                swipeRefreshLayoutRefreshing.set(true)
            } else {
                swipeRefreshLayoutRefreshing.set(false)
            }
        }

        override fun onReceivedTitle(view: WebView?, title: String?) {
            this@WebViewViewModel.title.set(title)
        }
    }

    fun reload() {
        handleEvent(ReloadWebView())
    }

    class ReloadWebView : FragmentEvent()
}
