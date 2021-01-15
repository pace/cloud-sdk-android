package cloud.pace.sdk.appkit.app.webview

import android.annotation.TargetApi
import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_VIEW
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Message
import android.webkit.*
import cloud.pace.sdk.PACECloudSDK

class AppWebViewClient(var url: String, val callback: WebClientCallback, val context: Context? = null) : WebViewClient() {

    interface WebClientCallback {
        /**
         * Invoked when the WebClient changes it's error state (e.g. a website that couldn't be loaded
         * previously is now ready)
         * @param isError true, if the current state is an error page
         */
        fun onSwitchErrorState(isError: Boolean, isHttpError: Boolean)

        /**
         * Invoked when the WebClient catch a close fragment
         */
        fun close(reopenRequest: ReopenRequest? = null)

        fun getBiometricStatus(redirectUri: String?, state: String?)

        fun saveTotpSecret(request: TotpSecretRequest)

        fun getTotp(host: String?, key: String?, serverTime: Long?, redirectUri: String?, state: String?)

        fun setSecureData(host: String?, key: String?, value: String?, redirectUri: String?, state: String?)

        fun getSecureData(host: String?, key: String?, redirectUri: String?, state: String?)

        fun setDisableTime(host: String?, until: Long?)

        fun openInNewTab(url: String, cancelUrl: String)

        fun onCustomSchemeError(context: Context?, cancelUrl: String, scheme: String)

        /**
         * Invoked when the page has started or finished loading
         */
        fun onLoadingChanged(isLoading: Boolean)

        class ReopenRequest(
            var reopenUrl: String?,
            var state: String?,
            var reopenTitle: String?,
            var reopenSubtitle: String?
        )

        class TotpSecretRequest(
            val host: String?,
            val secret: String?,
            val key: String?,
            val period: Int?,
            val digits: Int?,
            val algorithm: String?,
            val redirectUri: String?,
            val state: String?
        )
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
        val oldUrl = view?.url?.let { Uri.parse(it) } ?: return false
        val newUrl = request?.url ?: return false
        return if (!interceptUrl(oldUrl, newUrl)) {
            super.shouldOverrideUrlLoading(view, request)
        } else true
    }

    @Suppress("DEPRECATION")
    override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
        val oldUrl = view?.url?.let { Uri.parse(it) } ?: return false
        val newUrl = url?.let { Uri.parse(it) } ?: return false
        return if (!interceptUrl(oldUrl, newUrl)) {
            super.shouldOverrideUrlLoading(view, url)
        } else true
    }

    private fun interceptUrl(oldUrl: Uri, newUrl: Uri): Boolean {
        return when {
            newUrl.toString().contains(CLOSE) -> {
                catchClose(newUrl)
                true
            }
            newUrl.toString().contains(GET_BIOMETRIC_STATUS) -> {
                catchBiometricStatus(newUrl)
                true
            }
            newUrl.toString().contains(SET_TOTP_SECRET) -> {
                catchTotpSecret(oldUrl, newUrl)
                true
            }
            newUrl.toString().contains(GET_TOTP) -> {
                catchTotp(oldUrl, newUrl)
                true
            }
            newUrl.toString().contains(SET_SECURE_DATA) -> {
                catchSetData(oldUrl, newUrl)
                true
            }
            newUrl.toString().contains(GET_SECURE_DATA) -> {
                catchGetData(oldUrl, newUrl)
                true
            }
            newUrl.toString().contains(DISABLE) -> {
                catchDisable(oldUrl, newUrl)
                true
            }
            newUrl.toString().contains(OPEN_URL_IN_NEW_TAB) -> {
                catchOpenUrlInNewTab(newUrl)
                true
            }
            else -> {
                url = newUrl.toString()
                false
            }
        }
    }

    private fun catchClose(url: Uri) {
        val reopenUrl = url.getQueryParameter(REOPEN_URL)
        val state = url.getQueryParameter(STATE)
        val reopenTitle = url.getQueryParameter(REOPEN_TITLE)
        val reopenSubtitle = url.getQueryParameter(REOPEN_SUBTITLE)

        if (reopenUrl != null && URLUtil.isValidUrl(reopenUrl) || state != null || reopenTitle != null || reopenSubtitle != null) {
            callback.close(WebClientCallback.ReopenRequest(reopenUrl, state, reopenTitle, reopenSubtitle))
        } else {
            callback.close()
        }
    }

    private fun catchBiometricStatus(url: Uri) {
        val redirectUri = url.getQueryParameter(REDIRECT_URI)
        val state = url.getQueryParameter(STATE)

        callback.getBiometricStatus(redirectUri, state)
    }

    private fun catchTotpSecret(oldUrl: Uri, newUrl: Uri) {
        val redirectUri = newUrl.getQueryParameter(REDIRECT_URI)
        val secret = newUrl.getQueryParameter(SECRET)
        val key = newUrl.getQueryParameter(KEY)
        val period = newUrl.getQueryParameter(PERIOD)
        val digits = newUrl.getQueryParameter(DIGITS)
        val algorithm = newUrl.getQueryParameter(ALGORITHM)
        val state = newUrl.getQueryParameter(STATE)

        callback.saveTotpSecret(WebClientCallback.TotpSecretRequest(oldUrl.host, secret, key, period?.toInt(), digits?.toInt(), algorithm, redirectUri, state))
    }

    private fun catchTotp(oldUrl: Uri, newUrl: Uri) {
        val redirectUri = newUrl.getQueryParameter(REDIRECT_URI)
        val key = newUrl.getQueryParameter(KEY)
        val serverTime = newUrl.getQueryParameter(SERVER_TIME)
        val state = newUrl.getQueryParameter(STATE)

        callback.getTotp(oldUrl.host, key, serverTime?.toLong(), redirectUri, state)
    }

    private fun catchSetData(oldUrl: Uri, newUrl: Uri) {
        val redirectUri = newUrl.getQueryParameter(REDIRECT_URI)
        val key = newUrl.getQueryParameter(KEY)
        val value = newUrl.getQueryParameter(VALUE)
        val state = newUrl.getQueryParameter(STATE)

        callback.setSecureData(oldUrl.host, key, value, redirectUri, state)
    }

    private fun catchGetData(oldUrl: Uri, newUrl: Uri) {
        val redirectUri = newUrl.getQueryParameter(REDIRECT_URI)
        val key = newUrl.getQueryParameter(KEY)
        val state = newUrl.getQueryParameter(STATE)

        callback.getSecureData(oldUrl.host, key, redirectUri, state)
    }

    private fun catchDisable(oldUrl: Uri, newUrl: Uri) {
        val until = newUrl.getQueryParameter(UNTIL)?.toLongOrNull()
        callback.setDisableTime(oldUrl.host, until)
        callback.close()
    }

    private fun catchOpenUrlInNewTab(newUrl: Uri) {
        val url = newUrl.getQueryParameter(URL) ?: return
        val cancelUrl = newUrl.getQueryParameter(CANCEL_URL) ?: return

        val customScheme = "pace.${PACECloudSDK.configuration.clientId}://redirect"
        val intent = Intent(ACTION_VIEW, Uri.parse(customScheme))
        val resolveInfo: List<ResolveInfo>? = context?.packageManager?.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)?.toList()

        if (!resolveInfo.isNullOrEmpty()) {
            callback.openInNewTab(url, cancelUrl)
        } else {
            callback.onCustomSchemeError(context, cancelUrl, customScheme)
        }
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

    companion object {
        private const val PWA_SDK_SCHEME = "pacepwasdk"
        private const val ACTION_SCHEME = "$PWA_SDK_SCHEME://action"

        private const val CLOSE = "$ACTION_SCHEME/close"
        private const val GET_BIOMETRIC_STATUS = "$ACTION_SCHEME/getBiometricStatus"
        private const val SET_TOTP_SECRET = "$ACTION_SCHEME/setTOTPSecret"
        private const val GET_TOTP = "$ACTION_SCHEME/getTOTP"
        private const val SET_SECURE_DATA = "$ACTION_SCHEME/setSecureData"
        private const val GET_SECURE_DATA = "$ACTION_SCHEME/getSecureData"
        private const val DISABLE = "$ACTION_SCHEME/disable"
        private const val OPEN_URL_IN_NEW_TAB = "$ACTION_SCHEME/openURLInNewTab"

        const val REOPEN_URL = "reopen_url"
        const val REOPEN_TITLE = "reopen_title"
        const val REOPEN_SUBTITLE = "reopen_subtitle"

        const val REDIRECT_URI = "redirect_uri"
        const val STATE = "state"

        const val SECRET = "secret"
        const val PERIOD = "period"
        const val DIGITS = "digits"
        const val ALGORITHM = "algorithm"
        const val SERVER_TIME = "server_time"
        const val KEY = "key"
        const val VALUE = "value"
        const val STATUS_CODE = "status_code"
        const val TOTP = "totp"
        const val BIOMETRIC_METHOD = "biometric_method"

        const val UNTIL = "until"

        const val URL = "url"
        const val CANCEL_URL = "cancel_url"
        const val TO = "to"
    }
}
