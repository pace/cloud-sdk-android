package cloud.pace.sdk.appkit.app.webview

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Message
import android.util.Log
import android.webkit.ConsoleMessage
import android.webkit.GeolocationPermissions
import android.webkit.WebChromeClient
import android.webkit.WebView
import timber.log.Timber

open class DefaultWebChromeClient(private val context: Context) : WebChromeClient() {

    override fun onCreateWindow(view: WebView?, isDialog: Boolean, isUserGesture: Boolean, resultMsg: Message?): Boolean {
        if (isUserGesture) {
            val data = view?.hitTestResult?.extra
            if (data != null) {
                val type = view.hitTestResult.type
                when {
                    type == WebView.HitTestResult.EMAIL_TYPE -> {
                        val uri = if (!data.startsWith("$MAILTO_SCHEME:")) {
                            "$MAILTO_SCHEME:$data"
                        } else {
                            data
                        }
                        startActivityIfAvailable(Intent(Intent.ACTION_SENDTO, Uri.parse(uri)))
                    }
                    type == WebView.HitTestResult.PHONE_TYPE -> {
                        val uri = if (!data.startsWith("$TEL_SCHEME:")) {
                            "$TEL_SCHEME:$data"
                        } else {
                            data
                        }
                        startActivityIfAvailable(Intent(Intent.ACTION_DIAL, Uri.parse(uri)))
                    }
                    data.startsWith("$SMS_SCHEME:") -> startActivityIfAvailable(Intent(Intent.ACTION_SENDTO, Uri.parse(data)))
                    else -> startActivityIfAvailable(Intent(Intent.ACTION_VIEW, Uri.parse(data))) // open blank links externally
                }
            }
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

    private fun startActivityIfAvailable(intent: Intent) {
        try {
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Timber.e(e, "Could not found an activity to start the intent with action ${intent.action} and URI ${intent.data}")
        }
    }

    companion object {
        const val HTTP_SCHEME = "http"
        const val HTTPS_SCHEME = "https"
        const val MAILTO_SCHEME = "mailto"
        const val TEL_SCHEME = "tel"
        const val SMS_SCHEME = "sms"
    }
}
