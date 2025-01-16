package cloud.pace.sdk.appkit.app.deeplink

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.webkit.CookieManager
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import cloud.pace.sdk.appkit.AppKit
import cloud.pace.sdk.appkit.app.webview.DefaultWebChromeClient
import cloud.pace.sdk.appkit.app.webview.DefaultWebChromeClient.Companion.HTTPS_SCHEME
import cloud.pace.sdk.appkit.app.webview.DefaultWebChromeClient.Companion.HTTP_SCHEME
import cloud.pace.sdk.appkit.app.webview.DefaultWebChromeClient.Companion.MAILTO_SCHEME
import cloud.pace.sdk.appkit.app.webview.DefaultWebChromeClient.Companion.SMS_SCHEME
import cloud.pace.sdk.appkit.app.webview.DefaultWebChromeClient.Companion.TEL_SCHEME
import cloud.pace.sdk.utils.DeviceUtils
import cloud.pace.sdk.utils.ErrorListener
import cloud.pace.sdk.utils.applyInsets
import timber.log.Timber

class WebViewActivity : AppCompatActivity() {

    private lateinit var webView: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
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
                webChromeClient = DefaultWebChromeClient(context)
                loadUrl(uri.toString())
            }

            CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true)

            setContentView(webView)
            webView.applyInsets()
        } else {
            ErrorListener.reportError(NullPointerException("URL to load in payment WebView cannot be null. Finish WebViewActivity."))
            finish()
        }
    }

    private fun intercept(newUri: Uri?): Boolean {
        ErrorListener.reportBreadcrumb(TAG, "Intercept new URL", mapOf("New URL" to newUri))

        // Intercept redirect service deep link
        val paceRedirectScheme = DeviceUtils.getPACERedirectScheme(this)
        return when (newUri?.scheme) {
            paceRedirectScheme, FALLBACK_REDIRECT_SCHEME -> {
                ErrorListener.reportBreadcrumb(
                    TAG,
                    "New URL is a redirect URL. Start RedirectUriReceiverActivity.",
                    mapOf("paceRedirectScheme" to paceRedirectScheme, "fallbackRedirectScheme" to FALLBACK_REDIRECT_SCHEME)
                )

                val newIntent = Intent(this, RedirectUriReceiverActivity::class.java)
                newIntent.data = newUri
                startActivity(newIntent)
                true
            }

            MAILTO_SCHEME, SMS_SCHEME -> {
                startActivityIfAvailable(Intent(Intent.ACTION_SENDTO, newUri))
                true
            }

            TEL_SCHEME -> {
                startActivityIfAvailable(Intent(Intent.ACTION_DIAL, newUri))
                true
            }

            else -> {
                if (newUri?.scheme != HTTPS_SCHEME && newUri?.scheme != HTTP_SCHEME) {
                    ErrorListener.reportError(IllegalArgumentException("The scheme ${newUri?.scheme} is not a valid scheme."))
                }
                false
            }
        }
    }

    private fun startActivityIfAvailable(intent: Intent) {
        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Timber.i(e, "Could not found an activity to start the intent with action ${intent.action} and URI ${intent.data}")
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
