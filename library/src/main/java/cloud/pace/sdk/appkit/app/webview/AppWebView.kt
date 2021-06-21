package cloud.pace.sdk.appkit.app.webview

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.webkit.CookieManager
import android.webkit.JavascriptInterface
import android.webkit.WebView.setWebContentsDebuggingEnabled
import android.widget.RelativeLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import cloud.pace.sdk.R
import cloud.pace.sdk.appkit.AppKit
import cloud.pace.sdk.appkit.communication.generated.CommunicationManager
import cloud.pace.sdk.appkit.communication.generated.model.request.*
import cloud.pace.sdk.appkit.communication.generated.model.response.*
import cloud.pace.sdk.appkit.utils.BiometricUtils
import cloud.pace.sdk.utils.CloudSDKKoinComponent
import cloud.pace.sdk.utils.Event
import kotlinx.android.synthetic.main.app_web_view.view.*
import org.koin.core.inject
import org.koin.core.parameter.parametersOf

class AppWebView(context: Context, attributeSet: AttributeSet) : RelativeLayout(context, attributeSet), CloudSDKKoinComponent {

    private val webViewModel: AppWebViewModel by inject { parametersOf(context) }
    private val communicationManager: CommunicationManager
    private var fragment: Fragment? = null
    private val loadingIndicatorRunnable = Runnable {
        loadingIndicator?.visibility = View.VISIBLE
    }

    private val openUrlObserver = Observer<Event<String>> {
        val newUrl = it.getContentIfNotHandled() ?: return@Observer

        val appWebViewClient = AppWebViewClient(newUrl, webViewModel, context)
        webView.webViewClient = appWebViewClient
        webView.webChromeClient = appWebViewClient.chromeClient

        webView?.loadUrl(newUrl)
    }

    private val isInErrorStateObserver = Observer<Event<Boolean>> {
        val isInErrorState = it.getContentIfNotHandled() ?: return@Observer

        if (isInErrorState) {
            webView?.visibility = View.GONE
            failureView?.visibility = View.VISIBLE
        } else {
            failureView?.visibility = View.GONE
            webView?.visibility = View.VISIBLE
        }
    }

    private val showLoadingIndicatorObserver = Observer<Event<Boolean>> {
        val showLoadingIndicator = it.getContentIfNotHandled() ?: return@Observer
        loadingIndicator?.apply {
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
            if (webView.canGoBack()) {
                webView.goBack()
            } else {
                webViewModel.closeApp()
            }
        }
    }

    init {
        addView(View.inflate(context, R.layout.app_web_view, null))

        CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true)

        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            setSupportMultipleWindows(true)

            // Set user agent
            userAgentString = AppKit.userAgent
        }

        communicationManager = CommunicationManager(webViewModel) {
            webView.evaluateJavascript("window.postMessage('$it', window.origin)") {}
        }
        webView.addJavascriptInterface(CommunicationHandler(), "pace_native_api")

        failureView.setButtonClickListener {
            webView.reload()
        }
    }

    /**
     * Sets the app with fragment as parent
     */
    fun loadApp(parent: Fragment, url: String) {
        setWebContentsDebuggingEnabled(true)

        fragment = parent
        webViewModel.init(url)
    }

    fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        }
    }

    fun onDestroy() {
        webView.destroy()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        val lifecycleOwner = context as? LifecycleOwner
            ?: throw RuntimeException("lifecycle owner not found ")

        // TODO: should this be moved to "onResume" and "onPause" and replaced with "observeForever"?
        webViewModel.loadUrl.observe(lifecycleOwner, openUrlObserver)
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
