package cloud.pace.sdk.appkit.app.deeplink

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import androidx.browser.customtabs.CustomTabsService
import cloud.pace.sdk.appkit.app.AppFragmentViewModelImpl
import cloud.pace.sdk.utils.ErrorLevel
import cloud.pace.sdk.utils.ErrorListener
import timber.log.Timber

class DeepLinkManagementActivity : Activity() {

    private var isProcessStarted = false

    override fun onResume() {
        super.onResume()

        /*
        * Check if the REDIRECT flag was set by the RedirectUriReceiverActivity,
        * because it can also be the first run of this activity if it was previously killed by the system
        * before the redirect from the RedirectUriReceiverActivity happened.
        */
        val isRedirect = intent.getBooleanExtra(REDIRECT, false)
        if (!isRedirect && !isProcessStarted) {
            /*
            * If this is the first run of the activity, start the intent (WebViewActivity or custom tab).
            * Note that we do not finish the activity at this point, in order to remain on the back
            * stack underneath the new activity (WebViewActivity or custom tab).
            */
            val url = intent.extras?.getString(URL)
            ErrorListener.reportBreadcrumb(TAG, "It is the first run of the activity", mapOf("isProcessStarted" to isProcessStarted, "URL" to url))

            if (url != null) {
                val integrated = intent.extras?.getBoolean(INTEGRATED) ?: false
                try {
                    val intent = if (integrated) {
                        // Create WebViewActivity intent to open URL in WebView
                        Timber.d("Open WebViewActivity with the following start URL: $url")
                        ErrorListener.reportBreadcrumb(TAG, "Open URL in WebView of WebViewActivity", mapOf("integrated" to integrated, "isProcessStarted" to isProcessStarted))
                        Intent(this, WebViewActivity::class.java)
                    } else {
                        // Create custom tab intent to open URL in custom tab activity
                        Timber.d("Open custom tab with the following start URL: $url")
                        ErrorListener.reportBreadcrumb(TAG, "Open URL in custom tab activity", mapOf("integrated" to integrated, "isProcessStarted" to isProcessStarted))
                        CustomTabsIntent.Builder().build().intent.apply {
                            if (isChromeCustomTabsSupported()) {
                                setPackage(AppFragmentViewModelImpl.CHROME_PACKAGE_NAME)
                            }
                        }
                    }

                    intent.data = Uri.parse(url)
                    startActivity(intent)
                    isProcessStarted = true
                } catch (e: ActivityNotFoundException) {
                    ErrorListener.reportError(e)
                    setCanceled()
                }
            } else {
                ErrorListener.reportBreadcrumb(
                    TAG,
                    "The start URL is null. This can also happen if the user has cancelled the flow and the activity was previously killed by the system.",
                    mapOf("isProcessStarted" to isProcessStarted, "URL" to url)
                )
                setCanceled()
            }
        } else {
            /*
            * On a subsequent run, it must be determined whether we have returned to this activity
            * due to an redirect from the other activity (success) or the user cancelled the flow.
            * This can be done by checking whether a redirect URI is available, which would be provided by
            * RedirectUriReceiverActivity. If it is not, we have returned here due to the user
            * pressing the back button or closes the custom tab.
            */
            val redirectUri = intent.data?.getQueryParameter(TO)
            Timber.d("Intent contains the following URI: ${intent.data?.toString()}")
            ErrorListener.reportBreadcrumb(TAG, "It is the subsequent run of the activity", mapOf("isProcessStarted" to isProcessStarted, "Intent data" to intent.data?.toString()))

            if (redirectUri != null) {
                ErrorListener.reportBreadcrumb(TAG, "The process succeeded. Load redirect URL in original WebView.", mapOf("isProcessStarted" to isProcessStarted, "Redirect URL" to redirectUri))
                setSuccess(redirectUri)
            } else {
                ErrorListener.reportBreadcrumb(TAG, "The process failed. Load cancel URL in original WebView.", mapOf("isProcessStarted" to isProcessStarted), ErrorLevel.WARNING)
                setCanceled()
            }
        }
    }

    private fun isChromeCustomTabsSupported(): Boolean {
        val serviceIntent = Intent(CustomTabsService.ACTION_CUSTOM_TABS_CONNECTION)
        serviceIntent.setPackage(AppFragmentViewModelImpl.CHROME_PACKAGE_NAME)
        val resolveInfos = packageManager.queryIntentServices(serviceIntent, 0)
        return resolveInfos.isNotEmpty()
    }

    private fun setSuccess(redirectUri: String) {
        val intent = Intent()
        intent.data = Uri.parse(redirectUri)
        setResult(RESULT_OK, intent)
        finish()
    }

    private fun setCanceled() {
        setResult(RESULT_CANCELED)
        finish()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        ErrorListener.reportBreadcrumb(TAG, "Set new intent", mapOf("isProcessStarted" to isProcessStarted, "Intent data" to intent?.data?.toString()))
        setIntent(intent)
    }

    companion object {
        const val URL = "url"
        const val INTEGRATED = "integrated"
        const val REDIRECT = "redirect"
        const val TO = "to"
        private const val TAG = "DeepLinkManagementActivity"
    }
}
