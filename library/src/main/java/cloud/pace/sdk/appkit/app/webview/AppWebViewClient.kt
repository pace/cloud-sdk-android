package cloud.pace.sdk.appkit.app.webview

import android.annotation.TargetApi
import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_VIEW
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Message
import android.webkit.*

class AppWebViewClient(var url: String, val callback: WebClientCallback, val context: Context? = null) : WebViewClient() {

    interface WebClientCallback {
        /**
         * Invoked when the WebClient changes it's error state (e.g. a website that couldn't be loaded
         * previously is now ready)
         * @param isError true, if the current state is an error page
         */
        fun onSwitchErrorState(isError: Boolean, isHttpError: Boolean)

        /**
         * Invoked when the page has started or finished loading
         */
        fun onLoadingChanged(isLoading: Boolean)
    }

    private var isInErrorState = false
    private var wasInErrorState = false
    private var wasHttpError = false

    val chromeClient = object : WebChromeClient() {
        override fun onProgressChanged(view: WebView?, newProgress: Int) {
            if (newProgress == 100) {
                if (wasInErrorState && !isInErrorState) {
                    callback.onSwitchErrorState(isError = false, isHttpError = false)
                    wasInErrorState = false
                }
            }
        }

        override fun onCreateWindow(view: WebView?, isDialog: Boolean, isUserGesture: Boolean, resultMsg: Message?): Boolean {
            if (isUserGesture) {
                // open blank links externally
                val data = view?.hitTestResult?.extra
                val browserIntent = Intent(ACTION_VIEW, Uri.parse(data))
                view?.context?.startActivity(browserIntent)
                return false
            }
            return false
        }

        override fun onGeolocationPermissionsShowPrompt(origin: String?, callback: GeolocationPermissions.Callback?) {
            callback?.invoke(origin, true, false) ?: super.onGeolocationPermissionsShowPrompt(origin, callback)
        }
    }

    @TargetApi(Build.VERSION_CODES.N)
    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
        val newUrl = request?.url ?: return false
        this.url = newUrl.toString()
        return super.shouldOverrideUrlLoading(view, request)
    }

    @Suppress("DEPRECATION")
    override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
        val newUrl = url?.let { Uri.parse(it) } ?: return false
        this.url = newUrl.toString()
        return super.shouldOverrideUrlLoading(view, url)
    }

    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
        super.onPageStarted(view, url, favicon)
        isInErrorState = false
        callback.onLoadingChanged(true)
    }

    override fun onPageFinished(view: WebView?, url: String?) {
        super.onPageFinished(view, url)
        callback.onLoadingChanged(false)
    }

    override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
        // No internet connection
        super.onReceivedError(view, request, error)
        val url = request?.url?.toString() ?: return
        setError(url, false)
    }

    override fun onReceivedHttpError(view: WebView?, request: WebResourceRequest?, errorResponse: WebResourceResponse?) {
        // HTTP error
        super.onReceivedHttpError(view, request, errorResponse)
        val url = request?.url?.toString() ?: return
        setError(url, true)
    }

    private fun setError(url: String, isHttpError: Boolean) {
        if (url != this.url) return

        if ((!wasInErrorState || wasHttpError != isHttpError)) {
            callback.onSwitchErrorState(true, isHttpError)
        }

        isInErrorState = true
        wasInErrorState = true
        wasHttpError = isHttpError
    }
}
