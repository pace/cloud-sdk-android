package cloud.pace.sdk.appkit.app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import cloud.pace.sdk.R
import cloud.pace.sdk.appkit.communication.*
import cloud.pace.sdk.idkit.IDKit
import cloud.pace.sdk.idkit.model.FailedRetrievingSessionWhileEnding
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
            event.getContentIfNotHandled()?.let { result ->
                lifecycleScope.launch(Dispatchers.Main) {
                    IDKit.authorize(this@AppActivity) {
                        result.onResult(it)
                        appModel.onLogin(this@AppActivity, it)
                    }
                }
            }
        }

        appModel.endSession.observe(this) { event ->
            event.getContentIfNotHandled()?.let { result ->
                lifecycleScope.launch(Dispatchers.Main) {
                    IDKit.endSession(this@AppActivity) {
                        val logoutResponse = when {
                            it is Success -> LogoutResponse.SUCCESSFUL
                            it is Failure && it.throwable is FailedRetrievingSessionWhileEnding -> LogoutResponse.UNAUTHORIZED
                            else -> LogoutResponse.OTHER
                        }
                        result.onResult(logoutResponse)
                    }
                }
            }
        }
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

    companion object {
        const val APP_URL = "APP_URL"
        const val BACK_TO_FINISH = "BACK_TO_FINISH"
    }
}
