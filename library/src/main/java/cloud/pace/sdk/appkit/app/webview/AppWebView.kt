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
import cloud.pace.sdk.appkit.communication.GetAccessTokenResponse
import cloud.pace.sdk.appkit.communication.MessageHandler
import cloud.pace.sdk.appkit.utils.BiometricUtils
import cloud.pace.sdk.utils.AuthenticationMode
import cloud.pace.sdk.utils.CloudSDKKoinComponent
import cloud.pace.sdk.utils.Event
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

    private val getAccessTokenResponseObserver = Observer<AppWebViewModel.ResponseEvent<GetAccessTokenResponse>> {
        it.getContentIfNotHandled()?.let { event ->
            if (PACECloudSDK.configuration.authenticationMode == AuthenticationMode.NATIVE) {
                sendMessageCallback(event)
            }
        }
    }

    private val verifyLocationResponseObserver = Observer<AppWebViewModel.ResponseEvent<AppWebViewModel.VerifyLocationResponse>> {
        it.getContentIfNotHandled()?.let { response ->
            sendMessageCallback(response)
        }
    }

    private val goBackObserver = Observer<Event<Unit>> {
        it.getContentIfNotHandled()?.let {
            if (webView.canGoBack()) {
                webView.goBack()
            } else {
                webViewModel.close()
            }
        }
    }

    private val isBiometricAvailableObserver = Observer<AppWebViewModel.ResponseEvent<Boolean>> {
        it.getContentIfNotHandled()?.let { event ->
            sendMessageCallback(AppWebViewModel.MessageBundle(event.id, event.message))
        }
    }

    private val statusCodeObserver = Observer<AppWebViewModel.ResponseEvent<AppWebViewModel.StatusCodeResponse>> {
        it.getContentIfNotHandled()?.let { event ->
            sendMessageCallback(event)
        }
    }

    private val totpResponseObserver = Observer<AppWebViewModel.ResponseEvent<AppWebViewModel.TOTPResponse>> {
        it.getContentIfNotHandled()?.let { totpResponse ->
            sendMessageCallback(totpResponse)
        }
    }

    private val secureDataObserver = Observer<AppWebViewModel.ResponseEvent<Map<String, String>>> {
        it.getContentIfNotHandled()?.let { secureDataResponse ->
            sendMessageCallback(secureDataResponse)
        }
    }

    private val appInterceptableLinkObserver = Observer<AppWebViewModel.ResponseEvent<AppWebViewModel.AppInterceptableLinkResponse>> {
        it.getContentIfNotHandled()?.let { appInterceptableLinkResponse ->
            sendMessageCallback(appInterceptableLinkResponse)
        }
    }

    private val valueResponseObserver = Observer<AppWebViewModel.ResponseEvent<AppWebViewModel.ValueResponse>> {
        it.getContentIfNotHandled()?.let { valueResponse ->
            sendMessageCallback(valueResponse)
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

        webView.addJavascriptInterface(GetAccessTokenInterface(), MessageHandler.GET_ACCESS_TOKEN.id)
        webView.addJavascriptInterface(LogoutInterface(), MessageHandler.LOGOUT.id)
        webView.addJavascriptInterface(ImageDataInterface(), MessageHandler.IMAGE_DATA.id)
        webView.addJavascriptInterface(VerifyLocationInterface(), MessageHandler.VERIFY_LOCATION.id)
        webView.addJavascriptInterface(BackInterface(), MessageHandler.BACK.id)
        webView.addJavascriptInterface(CloseInterface(), MessageHandler.CLOSE.id)
        webView.addJavascriptInterface(GetBiometricStatusInterface(), MessageHandler.GET_BIOMETRIC_STATUS.id)
        webView.addJavascriptInterface(SetTOTPSecretInterface(), MessageHandler.SET_TOTP_SECRET.id)
        webView.addJavascriptInterface(GetTOTPInterface(), MessageHandler.GET_TOTP.id)
        webView.addJavascriptInterface(SetSecureDataInterface(), MessageHandler.SET_SECURE_DATA.id)
        webView.addJavascriptInterface(GetSecureDataInterface(), MessageHandler.GET_SECURE_DATA.id)
        webView.addJavascriptInterface(DisableInterface(), MessageHandler.DISABLE.id)
        webView.addJavascriptInterface(OpenURLInNewTabInterface(), MessageHandler.OPEN_URL_IN_NEW_TAB.id)
        webView.addJavascriptInterface(GetAppInterceptableLinkInterface(), MessageHandler.GET_APP_INTERCEPTABLE_LINK.id)
        webView.addJavascriptInterface(SetUserProperty(), MessageHandler.SET_USER_PROPERTY.id)
        webView.addJavascriptInterface(LogEvent(), MessageHandler.LOG_EVENT.id)
        webView.addJavascriptInterface(GetConfig(), MessageHandler.GET_CONFIG.id)
        webView.addJavascriptInterface(GetTraceId(), MessageHandler.GET_TRACE_ID.id)

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
        webViewModel.getAccessTokenResponse.observe(lifecycleOwner, getAccessTokenResponseObserver)
        webViewModel.verifyLocationResponse.observe(lifecycleOwner, verifyLocationResponseObserver)
        webViewModel.goBack.observe(lifecycleOwner, goBackObserver)
        webViewModel.isBiometricAvailable.observe(lifecycleOwner, isBiometricAvailableObserver)
        webViewModel.statusCode.observe(lifecycleOwner, statusCodeObserver)
        webViewModel.totpResponse.observe(lifecycleOwner, totpResponseObserver)
        webViewModel.secureData.observe(lifecycleOwner, secureDataObserver)
        webViewModel.appInterceptableLink.observe(lifecycleOwner, appInterceptableLinkObserver)
        webViewModel.valueResponse.observe(lifecycleOwner, valueResponseObserver)
    }

    private fun <T> sendMessageCallback(bundle: AppWebViewModel.MessageBundle<T>) {
        webView.evaluateJavascript("window.postMessage('${gson.toJson(bundle)}', window.origin)") {}
    }

    inner class GetAccessTokenInterface {
        @JavascriptInterface
        fun postMessage(message: String) {
            webViewModel.handleGetAccessToken(message)
        }
    }

    inner class LogoutInterface {
        @JavascriptInterface
        fun postMessage(message: String) {
            webViewModel.handleLogout(message)
        }
    }

    inner class ImageDataInterface {
        @JavascriptInterface
        fun postMessage(message: String) {
            webViewModel.handleImageData(message)
        }
    }

    inner class VerifyLocationInterface {
        @JavascriptInterface
        fun postMessage(message: String) {
            webViewModel.handleVerifyLocation(message)
        }
    }

    inner class BackInterface {
        @JavascriptInterface
        fun postMessage(message: String) {
            webViewModel.handleBack(message)
        }
    }

    inner class CloseInterface {
        @JavascriptInterface
        fun postMessage(message: String) {
            webViewModel.handleClose(message)
        }
    }

    inner class GetBiometricStatusInterface {
        @JavascriptInterface
        fun postMessage(message: String) {
            webViewModel.handleGetBiometricStatus(message)
        }
    }

    inner class SetTOTPSecretInterface {
        @JavascriptInterface
        fun postMessage(message: String) {
            webViewModel.handleSetTOTPSecret(message)
        }
    }

    inner class GetTOTPInterface {
        @JavascriptInterface
        fun postMessage(message: String) {
            webViewModel.handleGetTOTP(message)
        }
    }

    inner class SetSecureDataInterface {
        @JavascriptInterface
        fun postMessage(message: String) {
            webViewModel.handleSetSecureData(message)
        }
    }

    inner class GetSecureDataInterface {
        @JavascriptInterface
        fun postMessage(message: String) {
            webViewModel.handleGetSecureData(message)
        }
    }

    inner class DisableInterface {
        @JavascriptInterface
        fun postMessage(message: String) {
            webViewModel.handleDisable(message)
        }
    }

    inner class OpenURLInNewTabInterface {
        @JavascriptInterface
        fun postMessage(message: String) {
            webViewModel.handleOpenURLInNewTab(message)
        }
    }

    inner class GetAppInterceptableLinkInterface {
        @JavascriptInterface
        fun postMessage(message: String) {
            webViewModel.handleGetAppInterceptableLink(message)
        }
    }

    inner class SetUserProperty {
        @JavascriptInterface
        fun postMessage(message: String) {
            webViewModel.handleSetUserProperty(message)
        }
    }

    inner class LogEvent {
        @JavascriptInterface
        fun postMessage(message: String) {
            webViewModel.handleLogEvent(message)
        }
    }

    inner class GetConfig {
        @JavascriptInterface
        fun postMessage(message: String) {
            webViewModel.handleGetConfig(message)
        }
    }

    inner class GetTraceId {
        @JavascriptInterface
        fun postMessage(message: String) {
            webViewModel.handleGetTraceId(message)
        }
    }
}
