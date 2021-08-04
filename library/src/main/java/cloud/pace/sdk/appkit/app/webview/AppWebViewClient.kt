package cloud.pace.sdk.appkit.app.webview

import android.annotation.TargetApi
import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_VIEW
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Message
import android.util.Log
import android.webkit.*
import timber.log.Timber

class AppWebViewClient(var url: String, val callback: WebClientCallback, val context: Context? = null) : WebViewClient() {

    interface WebClientCallback {

        /**
         * Invoked when the WebClient catch a close URI.
         */
        fun onClose()

        /**
         * Invoked when the WebClient changes it's error state (e.g. a website that couldn't be loaded previously is now ready).
         *
         * @param isError true, if the current state is an error page
         */
        fun onSwitchErrorState(isError: Boolean, isHttpError: Boolean)

        /**
         * Invoked when the page has started or finished loading.
         */
        fun onLoadingChanged(isLoading: Boolean)

        /**
         * Invoked when the URL has changed.
         */
        fun onUrlChanged(newUrl: String)
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

        override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
            consoleMessage ?: return super.onConsoleMessage(consoleMessage)

            val logLevel = when (consoleMessage.messageLevel()) {
                ConsoleMessage.MessageLevel.LOG -> Log.INFO
                ConsoleMessage.MessageLevel.WARNING -> Log.WARN
                ConsoleMessage.MessageLevel.ERROR -> Log.ERROR
                ConsoleMessage.MessageLevel.DEBUG -> Log.DEBUG
                else -> Log.VERBOSE
            }
            Timber.log(logLevel, consoleMessage.message())
            return true
        }
    }

    override fun doUpdateVisitedHistory(view: WebView?, url: String?, isReload: Boolean) {
        url?.let { callback.onUrlChanged(it) }
    }

    @TargetApi(Build.VERSION_CODES.N)
    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
        val newUrl = request?.url?.toString()
        return intercept(view, newUrl)
    }

    @Suppress("DEPRECATION")
    override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
        val newUrl = url?.let { Uri.parse(it) }?.toString()
        return intercept(view, newUrl)
    }

    private fun injectFeatureFlags(view: WebView?) {
        val script = "window.features = { messageIds: true }"
        Timber.d("Excecute the following Javascript: $script")
        view?.evaluateJavascript(script) {
            Timber.d("Javascript execution completed with the following result: $it")
        }
    }

    private fun intercept(view: WebView?, url: String?): Boolean {
        injectFeatureFlags(view)

        this.url = url ?: return false
        if (url == CLOSE_URI) {
            callback.onClose()
            return true
        }

        return false
    }

    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
        super.onPageStarted(view, url, favicon)
        injectFeatureFlags(view)
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
        if (!request.isForMainFrame) return
        setError(url, false)
    }

    override fun onReceivedHttpError(view: WebView?, request: WebResourceRequest?, errorResponse: WebResourceResponse?) {
        // HTTP error
        super.onReceivedHttpError(view, request, errorResponse)
        val url = request?.url?.toString() ?: return
        if (!request.isForMainFrame) return
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

    companion object {
        const val CLOSE_URI = "cloudsdk://close"
    }
}
