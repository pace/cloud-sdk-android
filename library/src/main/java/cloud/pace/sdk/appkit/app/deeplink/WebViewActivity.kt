package cloud.pace.sdk.appkit.app.deeplink

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Message
import android.util.Log
import android.webkit.*
import androidx.appcompat.app.AppCompatActivity
import cloud.pace.sdk.appkit.AppKit
import cloud.pace.sdk.utils.DeviceUtils
import cloud.pace.sdk.utils.ErrorListener
import timber.log.Timber

class WebViewActivity : AppCompatActivity() {

    private lateinit var webView: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val uri = intent.data
        ErrorListener.reportBreadcrumb(TAG, "Payment process started", mapOf("URL" to uri?.toString()))

        if (uri != null) {
            webView = WebView(this).apply {
                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    setSupportMultipleWindows(true)
                    userAgentString = AppKit.userAgent
                }
                webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                        return intercept(request?.url)
                    }

                    override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                        return intercept(url?.let { Uri.parse(it) })
                    }
                }
                webChromeClient = object : WebChromeClient() {
                    override fun onCreateWindow(view: WebView?, isDialog: Boolean, isUserGesture: Boolean, resultMsg: Message?): Boolean {
                        if (isUserGesture) {
                            // open blank links externally
                            val data = view?.hitTestResult?.extra
                            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(data))
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
                loadUrl(uri.toString())
            }

            CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true)
            WebView.setWebContentsDebuggingEnabled(true)

            setContentView(webView)
        } else {
            ErrorListener.reportError(NullPointerException("URL to load in payment WebView cannot be null. Finish WebViewActivity."))
            finish()
        }
    }

    private fun intercept(newUri: Uri?): Boolean {
        ErrorListener.reportBreadcrumb(TAG, "Intercept new URL", mapOf("New URL" to newUri))

        // Intercept redirect service deep link
        val paceRedirectScheme = DeviceUtils.getPACERedirectScheme(this)
        return if (newUri?.scheme == paceRedirectScheme || newUri?.scheme == FALLBACK_REDIRECT_SCHEME) {
            ErrorListener.reportBreadcrumb(
                TAG,
                "New URL is a redirect URL. Start RedirectUriReceiverActivity.",
                mapOf("paceRedirectScheme" to paceRedirectScheme, "fallbackRedirectScheme" to FALLBACK_REDIRECT_SCHEME)
            )

            val newIntent = Intent(this, RedirectUriReceiverActivity::class.java)
            newIntent.data = newUri
            startActivity(newIntent)
            true
        } else {
            if (newUri?.scheme != "https" && newUri?.scheme != "http") {
                ErrorListener.reportError(IllegalArgumentException("The scheme ${newUri?.scheme} is not a valid scheme."))
            }
            false
        }
    }

    override fun onBackPressed() {
        if (::webView.isInitialized && webView.canGoBack()) {
            webView.goBack()
        } else {
            finish()
        }
    }

    override fun onDestroy() {
        if (::webView.isInitialized) {
            webView.destroy()
        }
        super.onDestroy()
    }

    companion object {
        // Is needed so that the payment process also works for apps that have not set a redirect scheme (e.g. instant apps)
        private const val FALLBACK_REDIRECT_SCHEME = "cloudsdk"
        private const val TAG = "WebViewActivity"
    }
}
