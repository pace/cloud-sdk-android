package cloud.pace.sdk.app

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import cloud.pace.sdk.app.view.loginscreen.ShowLoginScreen
import cloud.pace.sdk.idkit.IDKit
import cloud.pace.sdk.utils.Failure
import cloud.pace.sdk.utils.Success
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LoginScreenActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            if (IDKit.isAuthorizationValid()) {
                startMainActivity()
            } else {
                ShowLoginScreen {
                    lifecycleScope.launch(Dispatchers.Main) {
                        IDKit.authorize(this@LoginScreenActivity) {
                            when (it) {
                                // it.result contains accessToken
                                is Success -> {
                                    Toast.makeText(this@LoginScreenActivity, "$it: Login successful!", Toast.LENGTH_LONG).show()
                                    startMainActivity()
                                }
                                // it.throwable contains error
                                is Failure -> {
                                    Toast.makeText(this@LoginScreenActivity, "$it: Login failed!", Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun startMainActivity() {
        val intent = Intent(this, MainScreenActivity::class.java)
        startActivity(intent)
        finish()
    }
}
