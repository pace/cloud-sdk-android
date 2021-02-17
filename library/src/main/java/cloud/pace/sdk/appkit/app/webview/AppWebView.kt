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
import cloud.pace.sdk.PACECloudSDK
import cloud.pace.sdk.R
import cloud.pace.sdk.appkit.AppKit
import cloud.pace.sdk.appkit.utils.BiometricUtils
import cloud.pace.sdk.utils.AuthenticationMode
import cloud.pace.sdk.utils.CloudSDKKoinComponent
import cloud.pace.sdk.utils.Event
import cloud.pace.sdk.utils.onMainThread
import com.google.gson.Gson
import kotlinx.android.synthetic.main.app_web_view.view.*
import org.koin.core.inject
import org.koin.core.parameter.parametersOf

class AppWebView(context: Context, attributeSet: AttributeSet) : RelativeLayout(context, attributeSet), CloudSDKKoinComponent {

    private val webViewModel: AppWebViewModel by inject { parametersOf(context) }
    private var fragment: Fragment? = null
    private val gson = Gson()
    private val loadingIndicatorRunnable = Runnable {
        loadingIndicator?.visibility = View.VISIBLE
    }

    private val urlObserver = Observer<Event<String>> {
        val url = it.getContentIfNotHandled() ?: return@Observer

        val appWebViewClient = AppWebViewClient(url, webViewModel, context)
        webView.webViewClient = appWebViewClient
        webView.webChromeClient = appWebViewClient.chromeClient

        webView?.loadUrl(url)
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

    private val newTokenObserver = Observer<Event<String>> {
        it.getContentIfNotHandled()?.let { newToken ->
            if (PACECloudSDK.configuration.authenticationMode == AuthenticationMode.NATIVE) {
                sendMessageCallback(newToken)
            }
        }
    }

    private val verifyLocationResponseObserver = Observer<Event<AppWebViewModel.VerifyLocationResponse>> {
        it.getContentIfNotHandled()?.let { response ->
            sendMessageCallback(response.value)
        }
    }

    private val isBiometricAvailableObserver = Observer<Event<Boolean>> {
        it.getContentIfNotHandled()?.let { isBiometricAvailable ->
            sendMessageCallback(isBiometricAvailable.toString())
        }
    }

    private val statusCodeObserver = Observer<Event<AppWebViewModel.StatusCodeResponse>> {
        it.getContentIfNotHandled()?.let { statusCodeResponse ->
            sendMessageCallback(gson.toJson(statusCodeResponse))
        }
    }

    private val totpResponseObserver = Observer<Event<AppWebViewModel.TOTPResponse>> {
        it.getContentIfNotHandled()?.let { totpResponse ->
            sendMessageCallback(gson.toJson(totpResponse))
        }
    }

    private val secureDataObserver = Observer<Event<Map<String, String>>> {
        it.getContentIfNotHandled()?.let { secureDataResponse ->
            sendMessageCallback(gson.toJson(secureDataResponse))
        }
    }

    private val appInterceptableLinkObserver = Observer<Event<AppWebViewModel.AppInterceptableLinkResponse>> {
        it.getContentIfNotHandled()?.let { appInterceptableLinkResponse ->
            sendMessageCallback(gson.toJson(appInterceptableLinkResponse))
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

        webView.addJavascriptInterface(InvalidTokenInterface(), "pace_invalidToken")
        webView.addJavascriptInterface(ImageDataInterface(), "pace_imageData")
        webView.addJavascriptInterface(VerifyLocationInterface(), "pace_verifyLocation")
        webView.addJavascriptInterface(BackInterface(), "pace_back")
        webView.addJavascriptInterface(CloseInterface(), "pace_close")
        webView.addJavascriptInterface(GetBiometricStatusInterface(), "pace_getBiometricStatus")
        webView.addJavascriptInterface(SetTOTPSecretInterface(), "pace_setTOTPSecret")
        webView.addJavascriptInterface(GetTOTPInterface(), "pace_getTOTP")
        webView.addJavascriptInterface(SetSecureDataInterface(), "pace_setSecureData")
        webView.addJavascriptInterface(GetSecureDataInterface(), "pace_getSecureData")
        webView.addJavascriptInterface(DisableInterface(), "pace_disable")
        webView.addJavascriptInterface(OpenURLInNewTabInterface(), "pace_openURLInNewTab")
        webView.addJavascriptInterface(GetAppInterceptableLinkInterface(), "pace_getAppInterceptableLink")

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
        webViewModel.url.observe(lifecycleOwner, urlObserver)
        webViewModel.isInErrorState.observe(lifecycleOwner, isInErrorStateObserver)
        webViewModel.showLoadingIndicator.observe(lifecycleOwner, showLoadingIndicatorObserver)
        webViewModel.biometricRequest.observe(lifecycleOwner, biometricRequestObserver)
        webViewModel.newToken.observe(lifecycleOwner, newTokenObserver)
        webViewModel.verifyLocationResponse.observe(lifecycleOwner, verifyLocationResponseObserver)
        webViewModel.isBiometricAvailable.observe(lifecycleOwner, isBiometricAvailableObserver)
        webViewModel.statusCode.observe(lifecycleOwner, statusCodeObserver)
        webViewModel.totpResponse.observe(lifecycleOwner, totpResponseObserver)
        webViewModel.secureData.observe(lifecycleOwner, secureDataObserver)
        webViewModel.appInterceptableLink.observe(lifecycleOwner, appInterceptableLinkObserver)
    }

    private fun handleBack() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            webViewModel.handleClose()
        }
    }

    private fun sendMessageCallback(message: String) {
        webView.evaluateJavascript("window.messageCallback('$message')") {}
    }

    inner class InvalidTokenInterface {
        @JavascriptInterface
        fun postMessage(message: String) {
            onMainThread { webViewModel.handleInvalidToken(message) }
        }
    }

    inner class ImageDataInterface {
        @JavascriptInterface
        fun postMessage(message: String) {
            onMainThread { webViewModel.handleImageData(message) }
        }
    }

    inner class VerifyLocationInterface {
        @JavascriptInterface
        fun postMessage(message: String) {
            onMainThread { webViewModel.handleVerifyLocation(message) }
        }
    }

    inner class BackInterface {
        @JavascriptInterface
        fun postMessage(message: String) {
            onMainThread { handleBack() }
        }
    }

    inner class CloseInterface {
        @JavascriptInterface
        fun postMessage(message: String) {
            onMainThread { webViewModel.handleClose() }
        }
    }

    inner class GetBiometricStatusInterface {
        @JavascriptInterface
        fun postMessage(message: String) {
            onMainThread { webViewModel.handleGetBiometricStatus(message) }
        }
    }

    inner class SetTOTPSecretInterface {
        @JavascriptInterface
        fun postMessage(message: String) {
            onMainThread { webViewModel.handleSetTOTPSecret(message) }
        }
    }

    inner class GetTOTPInterface {
        @JavascriptInterface
        fun postMessage(message: String) {
            onMainThread { webViewModel.handleGetTOTP(message) }
        }
    }

    inner class SetSecureDataInterface {
        @JavascriptInterface
        fun postMessage(message: String) {
            onMainThread { webViewModel.handleSetSecureData(message) }
        }
    }

    inner class GetSecureDataInterface {
        @JavascriptInterface
        fun postMessage(message: String) {
            onMainThread { webViewModel.handleGetSecureData(message) }
        }
    }

    inner class DisableInterface {
        @JavascriptInterface
        fun postMessage(message: String) {
            onMainThread { webViewModel.handleDisable(message) }
        }
    }

    inner class OpenURLInNewTabInterface {
        @JavascriptInterface
        fun postMessage(message: String) {
            onMainThread { webViewModel.handleOpenURLInNewTab(message) }
        }
    }

    inner class GetAppInterceptableLinkInterface {
        @JavascriptInterface
        fun postMessage(message: String) {
            onMainThread { webViewModel.handleGetAppInterceptableLink(message) }
        }
    }
}
