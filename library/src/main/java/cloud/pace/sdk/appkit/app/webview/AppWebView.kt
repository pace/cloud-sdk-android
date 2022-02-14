package cloud.pace.sdk.appkit.app.webview

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.webkit.CookieManager
import android.webkit.JavascriptInterface
import android.webkit.WebView.setWebContentsDebuggingEnabled
import android.widget.RelativeLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.findViewTreeLifecycleOwner
import cloud.pace.sdk.appkit.AppKit
import cloud.pace.sdk.appkit.communication.generated.CommunicationManager
import cloud.pace.sdk.appkit.utils.BiometricUtils
import cloud.pace.sdk.databinding.AppWebViewBinding
import cloud.pace.sdk.utils.CloudSDKKoinComponent
import cloud.pace.sdk.utils.Event
import org.koin.core.component.inject
import org.koin.core.parameter.parametersOf

class AppWebView(context: Context, attributeSet: AttributeSet) : RelativeLayout(context, attributeSet), CloudSDKKoinComponent {

    private val binding = AppWebViewBinding.inflate(LayoutInflater.from(context), this, true)
    private val webViewModel: AppWebViewModel by inject { parametersOf(context) }
    private val communicationManager: CommunicationManager
    private var fragment: Fragment? = null
    private val loadingIndicatorRunnable = Runnable {
        binding.loadingIndicator.visibility = View.VISIBLE
    }

    private val initObserver = Observer<Event<String>> {
        val url = it.getContentIfNotHandled() ?: return@Observer

        val appWebViewClient = AppWebViewClient(url, webViewModel, context)
        binding.webView.webViewClient = appWebViewClient
        binding.webView.webChromeClient = appWebViewClient.chromeClient

        loadUrl(url)
    }

    private val loadUrlObserver = Observer<Event<String>> {
        val url = it.getContentIfNotHandled() ?: return@Observer
        loadUrl(url)
    }

    private val isInErrorStateObserver = Observer<Event<Boolean>> {
        val isInErrorState = it.getContentIfNotHandled() ?: return@Observer

        if (isInErrorState) {
            binding.webView.visibility = View.GONE
            binding.failureView.visibility = View.VISIBLE
        } else {
            binding.failureView.visibility = View.GONE
            binding.webView.visibility = View.VISIBLE
        }
    }

    private val showLoadingIndicatorObserver = Observer<Event<Boolean>> {
        val showLoadingIndicator = it.getContentIfNotHandled() ?: return@Observer
        binding.loadingIndicator.apply {
            if (showLoadingIndicator) {
                postDelayed(loadingIndicatorRunnable, 500)
            } else {
                removeCallbacks(loadingIndicatorRunnable)
                visibility = View.GONE
            }
        }
    }

    private val biometricRequestObserver = Observer<Event<AppWebViewModel.BiometricRequest>> {
        val callback = it.getContentIfNotHandled() ?: return@Observer
        fragment?.let {
            BiometricUtils.requestAuthentication(it, resources.getString(callback.title), onSuccess = callback.onSuccess, onFailure = callback.onFailure)
        }
    }

    private val goBackObserver = Observer<Event<Unit>> {
        it.getContentIfNotHandled()?.let {
            if (binding.webView.canGoBack()) {
                binding.webView.goBack()
            } else {
                webViewModel.closeApp()
            }
        }
    }

    init {
        CookieManager.getInstance().setAcceptThirdPartyCookies(binding.webView, true)

        binding.webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            setSupportMultipleWindows(true)

            // Set user agent
            userAgentString = AppKit.userAgent
        }

        communicationManager = CommunicationManager(webViewModel) {
            binding.webView.evaluateJavascript("window.postMessage('$it', window.origin)") {}
        }
        binding.webView.addJavascriptInterface(CommunicationHandler(), "pace_native_api")

        binding.failureView.setButtonClickListener {
            binding.webView.reload()
        }
    }

    /**
     * Initializes [AppWebView] with [AppWebViewClient] and loads the [url] in the WebView with the passed [fragment][parent] as parent.
     */
    fun init(parent: Fragment, url: String) {
        setWebContentsDebuggingEnabled(true)

        fragment = parent
        webViewModel.init(url)
    }

    /**
     * Loads the [url] in the WebView.
     * Also finishes the [cloud.pace.sdk.appkit.app.AppActivity] if the url is [AppWebViewClient.CLOSE_URI].
     */
    fun loadUrl(url: String) {
        if (url == AppWebViewClient.CLOSE_URI) {
            webViewModel.closeApp()
        } else {
            binding.webView.loadUrl(url)
        }
    }

    fun onBackPressed() {
        if (binding.webView.canGoBack()) {
            binding.webView.goBack()
        }
    }

    fun onDestroy() {
        binding.webView.destroy()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        val lifecycleOwner = findViewTreeLifecycleOwner() ?: throw RuntimeException("lifecycle owner not found ")
        webViewModel.init.observe(lifecycleOwner, initObserver)
        webViewModel.loadUrl.observe(lifecycleOwner, loadUrlObserver)
        webViewModel.isInErrorState.observe(lifecycleOwner, isInErrorStateObserver)
        webViewModel.showLoadingIndicator.observe(lifecycleOwner, showLoadingIndicatorObserver)
        webViewModel.biometricRequest.observe(lifecycleOwner, biometricRequestObserver)
        webViewModel.goBack.observe(lifecycleOwner, goBackObserver)
    }

    inner class CommunicationHandler {
        @JavascriptInterface
        fun postMessage(message: String) {
            communicationManager.handleMessage(message)
        }
    }
}
