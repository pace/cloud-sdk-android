package cloud.pace.sdk.appkit.app.deeplink

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import cloud.pace.sdk.appkit.app.deeplink.DeepLinkManagementActivity.Companion.REDIRECT
import cloud.pace.sdk.utils.ErrorListener
import timber.log.Timber

class RedirectUriReceiverActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Timber.d("Forward the following intent data to DeepLinkManagementActivity: ${intent.data?.toString()}")
        ErrorListener.reportBreadcrumb("RedirectUriReceiverActivity", "Forward intent data to DeepLinkManagementActivity", mapOf("URL" to intent.data?.toString()))

        // Handling the redirect in this way ensures that we can remove the WebViewActivity or custom tab from the back stack
        val newIntent = Intent(this, DeepLinkManagementActivity::class.java)
        newIntent.data = intent.data
        newIntent.putExtra(REDIRECT, true)
        newIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        startActivity(newIntent)

        finish()
    }
}
