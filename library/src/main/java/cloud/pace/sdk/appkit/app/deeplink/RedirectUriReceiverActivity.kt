package cloud.pace.sdk.appkit.app.deeplink

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class RedirectUriReceiverActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Handling the redirect in this way ensures that we can remove the WebViewActivity or custom tab from the back stack
        val newIntent = Intent(this, DeepLinkManagementActivity::class.java)
        newIntent.data = intent.data
        newIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        startActivity(newIntent)

        finish()
    }
}
