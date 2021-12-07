package cloud.pace.sdk.appkit.app.deeplink

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import androidx.browser.customtabs.CustomTabsService
import cloud.pace.sdk.appkit.app.AppFragmentViewModelImpl

class DeepLinkManagementActivity : Activity() {

    private var isProcessStarted = false

    override fun onResume() {
        super.onResume()

        if (!isProcessStarted) {
            /*
            * If this is the first run of the activity, start the intent (WebViewActivity or custom tab).
            * Note that we do not finish the activity at this point, in order to remain on the back
            * stack underneath the new activity (WebViewActivity or custom tab).
            */
            val url = intent.extras?.getString(URL)
            if (url != null) {
                val integrated = intent.extras?.getBoolean(INTEGRATED) ?: false
                try {
                    val intent = if (integrated) {
                        // Create WebViewActivity intent to open URL in WebView
                        Intent(this, WebViewActivity::class.java)
                    } else {
                        // Create custom tab intent to open URL in custom tab activity
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
                    setCanceled()
                }
            } else {
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
            if (redirectUri != null) {
                setSuccess(redirectUri)
            } else {
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
        setIntent(intent)
    }

    companion object {
        const val URL = "url"
        const val INTEGRATED = "integrated"
        const val TO = "to"
    }
}
