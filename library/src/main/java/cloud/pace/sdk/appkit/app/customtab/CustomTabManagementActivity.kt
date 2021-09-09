package cloud.pace.sdk.appkit.app.customtab

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri

class CustomTabManagementActivity : Activity() {

    private var isCustomTabStarted = false

    override fun onResume() {
        super.onResume()

        if (!isCustomTabStarted) {
            /*
            * If this is the first run of the activity, start the custom tabs intent.
            * Note that we do not finish the activity at this point, in order to remain on the back
            * stack underneath the custom tabs activity.
            */
            val customTabIntent = intent.extras?.getParcelable<Intent>(CUSTOM_TABS_INTENT)
            if (customTabIntent != null) {
                try {
                    startActivity(customTabIntent)
                    isCustomTabStarted = true
                } catch (e: ActivityNotFoundException) {
                    setCanceled()
                }
            } else {
                setCanceled()
            }
        } else {
            /*
            * On a subsequent run, it must be determined whether we have returned to this activity
            * due to an redirect from custom tab (success) or the user cancelled the flow.
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
        const val CUSTOM_TABS_INTENT = "customTabsIntent"
        const val TO = "to"
    }
}
