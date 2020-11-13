package cloud.pace.sdk.appkit.app.webview

import android.content.Context
import android.content.Intent
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
import cloud.pace.sdk.appkit.model.AuthenticationMode
import cloud.pace.sdk.appkit.utils.BiometricUtils
import cloud.pace.sdk.utils.CustomKoinComponent
import cloud.pace.sdk.utils.Event
import kotlinx.android.synthetic.main.app_web_view.view.*
import org.koin.core.inject

class AppWebView(context: Context, attributeSet: AttributeSet) : RelativeLayout(context, attributeSet), CustomKoinComponent {

    private val webViewModel: AppWebViewModel by inject()
    private var fragment: Fragment? = null
    private var touchEnabled: Boolean = true
    private val loadingIndicatorRunnable = Runnable {
        loadingIndicator?.visibility = View.VISIBLE
    }

    private val newTokenObserver = Observer<Event<String>> {
        it.getContentIfNotHandled()?.let { newToken ->
            if (AppKit.configuration.authenticationMode == AuthenticationMode.NATIVE) {
                webView.evaluateJavascript("window.messageCallback('$newToken')") {}
            }
        }
    }

    private val touchEnableObserver = Observer<Boolean> {
        touchEnabled = it
    }

    private val errorStateObserver = Observer<Event<Boolean>> {
        val isInErrorState = it.getContentIfNotHandled() ?: return@Observer

        if (isInErrorState) {
            webView?.visibility = View.GONE
            failureView?.visibility = View.VISIBLE
        } else {
            failureView?.visibility = View.GONE
            webView?.visibility = View.VISIBLE
        }
    }

    private val urlObserver = Observer<Event<String>> {
        val url = it.getContentIfNotHandled() ?: return@Observer

        val appWebViewClient = AppWebViewClient(url, webViewModel, context)
        webView.webViewClient = appWebViewClient
        webView.webChromeClient = appWebViewClient.chromeClient

        webView?.loadUrl(url)
    }

    private val broadcastIntentObserver = Observer<Event<Intent>> {
        val intent = it.getContentIfNotHandled() ?: return@Observer
        context.sendBroadcast(intent)
    }

    private val biometricRequestObserver = Observer<Event<AppWebViewModel.BiometricRequest>> {
        val callback = it.getContentIfNotHandled() ?: return@Observer
        fragment?.let {
            BiometricUtils.requestAuthentication(it, resources.getString(callback.title), onSuccess = callback.onSuccess, onFailure = callback.onFailure)
        }
    }

    private val loadingIndicatorObserver = Observer<Event<Boolean>> {
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

    init {
        val view = View.inflate(context, R.layout.app_web_view, null)
        addView(view)

        CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true)

        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            setSupportMultipleWindows(true)

            // Set user agent
            userAgentString = AppKit.userAgent
        }

        webView.addJavascriptInterface(InvalidTokenInterface(), "pace_invalidToken")
        webView.addJavascriptInterface(ImageDataInterface(), "pace_imageData")

        setOnTouchListener { _, _ -> !touchEnabled }

        failureView.setButtonClickListener {
            webView.reload()
        }
    }

    inner class InvalidTokenInterface {
        @JavascriptInterface
        fun postMessage(message: String) {
            webViewModel.handleInvalidToken(message)
        }
    }

    inner class ImageDataInterface {
        @JavascriptInterface
        fun postMessage(message: String) {
            webViewModel.handleImageData(message)
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
        webViewModel.url.observe(lifecycleOwner, urlObserver)
        webViewModel.touchEnable.observe(lifecycleOwner, touchEnableObserver)
        webViewModel.isInErrorState.observe(lifecycleOwner, errorStateObserver)
        webViewModel.broadcastIntent.observe(lifecycleOwner, broadcastIntentObserver)
        webViewModel.showLoadingIndicator.observe(lifecycleOwner, loadingIndicatorObserver)
        webViewModel.biometricRequest.observe(lifecycleOwner, biometricRequestObserver)
        webViewModel.newToken.observe(lifecycleOwner, newTokenObserver)
    }
}
