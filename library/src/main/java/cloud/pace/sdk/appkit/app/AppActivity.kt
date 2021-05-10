package cloud.pace.sdk.appkit.app

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import cloud.pace.sdk.R
import cloud.pace.sdk.appkit.communication.AppEventManager
import cloud.pace.sdk.appkit.communication.AppModel
import cloud.pace.sdk.idkit.IDKit
import cloud.pace.sdk.utils.*
import kotlinx.android.synthetic.main.fragment_app.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class AppActivity : AppCompatActivity(), CloudSDKKoinComponent {

    private var backToFinish = true
    private val eventManager: AppEventManager by inject()
    private val appModel: AppModel by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app)

        appModel.reset()

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.frameLayout, AppFragment())
            .commit()

        supportActionBar?.apply {
            setDisplayShowTitleEnabled(false)
            setDisplayHomeAsUpEnabled(false)
        }

        backToFinish = intent.extras?.getBoolean(BACK_TO_FINISH, true) ?: true

        appModel.authorize.observe(this) { event ->
            event.getContentIfNotHandled()?.let { authorizationResult ->
                lifecycleScope.launch(Dispatchers.Main) {
                    IDKit.authorize(this@AppActivity) { completion ->
                        (completion as? Success)?.result?.let { authorizationResult.onResult(it) } ?: finish()
                    }
                }
            }
        }

        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    override fun onBackPressed() {
        if (backToFinish) {
            finish()
        } else {
            appWebView.onBackPressed()
        }
    }

    override fun onDestroy() {
        appWebView.onDestroy()
        super.onDestroy()
    }

    private fun handleIntent(intent: Intent?) {
        intent ?: return

        val appLinkAction = intent.action
        val appLinkData = intent.data
        if (Intent.ACTION_VIEW == appLinkAction) {
            appLinkData?.getQueryParameter(TO)?.let { finalRedirect ->
                eventManager.onReceivedRedirect(finalRedirect)
            }
        }
    }

    companion object {
        const val APP_URL = "APP_URL"
        const val BACK_TO_FINISH = "BACK_TO_FINISH"
        const val AUTO_CLOSE = "AUTO_CLOSE"
        const val TO = "to"
    }
}
