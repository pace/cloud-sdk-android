package cloud.pace.sdk.idkit.authorization.integrated

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.webkit.ConsoleMessage
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import cloud.pace.sdk.appkit.AppKit
import cloud.pace.sdk.utils.CloudSDKKoinComponent
import cloud.pace.sdk.utils.DeviceUtils
import cloud.pace.sdk.utils.applyInsets
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationException.AuthorizationRequestErrors
import net.openid.appauth.AuthorizationManagementRequest
import net.openid.appauth.AuthorizationManagementResponse
import net.openid.appauth.AuthorizationRequest
import net.openid.appauth.AuthorizationResponse
import net.openid.appauth.EndSessionRequest
import net.openid.appauth.EndSessionResponse
import org.json.JSONException
import org.json.JSONObject
import timber.log.Timber

class AuthorizationWebViewActivity : AppCompatActivity(), CloudSDKKoinComponent {

    private lateinit var webView: WebView
    private lateinit var authRequest: AuthorizationManagementRequest
    private var completeIntent: PendingIntent? = null
    private var cancelIntent: PendingIntent? = null
    private var appAuthRedirectScheme: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val state = savedInstanceState ?: intent.extras
        if (state != null) {
            completeIntent = state.getParcelable(KEY_COMPLETE_INTENT)
            cancelIntent = state.getParcelable(KEY_CANCEL_INTENT)

            try {
                val authRequestJson = state.getString(KEY_AUTH_REQUEST)
                val authRequestType = state.getString(KEY_AUTH_REQUEST_TYPE)
                val request = requestFrom(authRequestJson, authRequestType)
                if (request != null) {
                    authRequest = request
                    appAuthRedirectScheme = DeviceUtils.getAppAuthRedirectScheme(this)
                    initWebView()
                } else {
                    Timber.e("No AuthorizationManagementRequest found matching the request json $authRequestJson and type $authRequestType")
                    sendResultAndFinish(cancelIntent, AuthorizationRequestErrors.INVALID_REQUEST.toIntent(), RESULT_CANCELED)
                }
            } catch (e: JSONException) {
                Timber.e(e, "Could not parse string as json object")
                sendResultAndFinish(cancelIntent, AuthorizationRequestErrors.INVALID_REQUEST.toIntent(), RESULT_CANCELED)
            }
        } else {
            Timber.w("No stored state - unable to handle response")
            sendResultAndFinish(cancelIntent, AuthorizationRequestErrors.INVALID_REQUEST.toIntent(), RESULT_CANCELED)
        }
    }

    private fun requestFrom(jsonString: String?, type: String?): AuthorizationManagementRequest? {
        if (jsonString == null || type == null) return null

        val json = JSONObject(jsonString)
        return when (type) {
            REQUEST_TYPE_AUTHORIZATION -> AuthorizationRequest.jsonDeserialize(json)
            REQUEST_TYPE_END_SESSION -> EndSessionRequest.jsonDeserialize(json)
            else -> null
        }
    }

    private fun initWebView() {
        webView = WebView(this).apply {
            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                userAgentString = AppKit.userAgent
            }
            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                    return intercept(request?.url)
                }

                override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                    return intercept(url?.let { Uri.parse(url) })
                }
            }
            webChromeClient = object : WebChromeClient() {
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
            loadUrl(authRequest.toUri().toString())
        }

        CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true)

        setContentView(webView)
        webView.applyInsets()
    }

    private fun intercept(newUri: Uri?): Boolean {
        return if (newUri != null && newUri.scheme == appAuthRedirectScheme) {
            val responseData = extractResponseData(newUri)
            if (responseData != null) {
                responseData.data = newUri
                sendResultAndFinish(completeIntent, responseData, RESULT_OK)
            } else {
                Timber.e("Failed to extract OAuth2 response from redirect URI: $newUri")
                sendResultAndFinish(cancelIntent, AuthorizationRequestErrors.INVALID_REQUEST.toIntent(), RESULT_CANCELED)
            }
            true
        } else {
            false
        }
    }

    private fun sendResultAndFinish(callback: PendingIntent?, cancelData: Intent, resultCode: Int) {
        if (callback != null) {
            try {
                callback.send(this, 0, cancelData)
            } catch (e: PendingIntent.CanceledException) {
                Timber.e(e, "Failed to send cancel intent")
            }
        } else {
            setResult(resultCode, cancelData)
        }
        finish()
    }

    private fun extractResponseData(responseUri: Uri): Intent? {
        return if (responseUri.queryParameterNames.contains(AuthorizationException.PARAM_ERROR)) {
            AuthorizationException.fromOAuthRedirect(responseUri).toIntent()
        } else {
            val response = responseWith(authRequest, responseUri)
            if (authRequest.state != response?.state) {
                Timber.w("State mismatch: State returned in authorization response (${response?.state}) does not match state from request (${authRequest.state}) - discarding response")
                AuthorizationRequestErrors.STATE_MISMATCH.toIntent()
            } else {
                response?.toIntent()
            }
        }
    }

    private fun responseWith(request: AuthorizationManagementRequest?, uri: Uri): AuthorizationManagementResponse? {
        return when (request) {
            is AuthorizationRequest -> AuthorizationResponse.Builder(request)
                .fromUri(uri)
                .build()

            is EndSessionRequest -> EndSessionResponse.Builder(request)
                .setState(uri.getQueryParameter(KEY_STATE))
                .build()

            else -> null
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

        private const val KEY_AUTH_REQUEST = "authRequest"
        private const val KEY_AUTH_REQUEST_TYPE = "authRequestType"
        private const val KEY_COMPLETE_INTENT = "completeIntent"
        private const val KEY_CANCEL_INTENT = "cancelIntent"
        private const val KEY_STATE = "state"
        private const val REQUEST_TYPE_AUTHORIZATION = "authorization"
        private const val REQUEST_TYPE_END_SESSION = "end_session"

        internal fun createStartIntent(context: Context, request: AuthorizationManagementRequest, completeIntent: PendingIntent? = null, cancelIntent: PendingIntent? = null): Intent {
            val intent = Intent(context, AuthorizationWebViewActivity::class.java)
            intent.putExtra(KEY_AUTH_REQUEST, request.jsonSerializeString())
            intent.putExtra(KEY_AUTH_REQUEST_TYPE, requestTypeFor(request))
            intent.putExtra(KEY_COMPLETE_INTENT, completeIntent)
            intent.putExtra(KEY_CANCEL_INTENT, cancelIntent)

            return intent
        }

        private fun requestTypeFor(request: AuthorizationManagementRequest?): String? {
            return when (request) {
                is AuthorizationRequest -> REQUEST_TYPE_AUTHORIZATION
                is EndSessionRequest -> REQUEST_TYPE_END_SESSION
                else -> null
            }
        }
    }
}
