package cloud.pace.sdk.appkit.app.webview

import android.annotation.TargetApi
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import cloud.pace.sdk.appkit.app.webview.DefaultWebChromeClient.Companion.MAILTO_SCHEME
import cloud.pace.sdk.appkit.app.webview.DefaultWebChromeClient.Companion.SMS_SCHEME
import cloud.pace.sdk.appkit.app.webview.DefaultWebChromeClient.Companion.TEL_SCHEME
import timber.log.Timber

class AppWebViewClient(var url: String, val callback: WebClientCallback, val context: Context) : WebViewClient() {

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

    val chromeClient = object : DefaultWebChromeClient(context) {
        override fun onProgressChanged(view: WebView?, newProgress: Int) {
            if (newProgress == 100) {
                if (wasInErrorState && !isInErrorState) {
                    callback.onSwitchErrorState(isError = false, isHttpError = false)
                    wasInErrorState = false
                }
            }
        }
    }

    override fun doUpdateVisitedHistory(view: WebView?, url: String?, isReload: Boolean) {
        url?.let { callback.onUrlChanged(it) }

        if (url == CLOSE_URI) {
            callback.onClose()
        }
    }

    @TargetApi(Build.VERSION_CODES.N)
    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
        return intercept(view, request?.url)
    }

    @Suppress("DEPRECATION")
    override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
        return intercept(view, url?.let { Uri.parse(it) })
    }

    private fun injectFeatureFlags(view: WebView?) {
        val script = "window.features = { messageIds: true }"
        Timber.d("Excecute the following Javascript: $script")
        view?.evaluateJavascript(script) {
            Timber.d("Javascript execution completed with the following result: $it")
        }
    }

    private fun intercept(view: WebView?, url: Uri?): Boolean {
        injectFeatureFlags(view)

        this.url = url?.toString() ?: return false

        if (url.toString() == CLOSE_URI) {
            callback.onClose()
            return true
        } else if (url.scheme == MAILTO_SCHEME || url.scheme == SMS_SCHEME) {
            startActivityIfAvailable(Intent(Intent.ACTION_SENDTO, url))
            return true
        } else if (url.scheme == TEL_SCHEME) {
            startActivityIfAvailable(Intent(Intent.ACTION_DIAL, url))
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

    private fun startActivityIfAvailable(intent: Intent) {
        try {
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Timber.e(e, "Could not found an activity to start the intent with action ${intent.action} and URI ${intent.data}")
        }
    }

    companion object {
        const val CLOSE_URI = "cloudsdk://close"
    }
}
