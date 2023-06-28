package cloud.pace.sdk.app

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.lifecycle.lifecycleScope
import cloud.pace.sdk.app.ui.components.loginscreen.ShowLoginScreen
import cloud.pace.sdk.idkit.IDKit
import cloud.pace.sdk.idkit.model.NoSupportedBrowser
import cloud.pace.sdk.ui.theme.PACETheme
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
                PACETheme {
                    LoginScreenContent()
                }
            }
        }
    }

    @Composable
    fun LoginScreenContent() {
        val openDialog = remember { mutableStateOf(false) }

        ShowLoginScreen(
            showDialog = openDialog.value,
            onDialogDismiss = {
                openDialog.value = false
            },
            openLogin = {
                lifecycleScope.launch(Dispatchers.Main) {
                    when (val result = IDKit.authorize(this@LoginScreenActivity)) {
                        is Success -> startMainActivity()
                        is Failure -> {
                            if (result.throwable is NoSupportedBrowser) {
                                openDialog.value = true
                            } else {
                                Toast.makeText(this@LoginScreenActivity, result.throwable.message, Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                }
            }
        )
    }

    private fun startMainActivity() {
        val intent = Intent(this, MainScreenActivity::class.java)
        startActivity(intent)
        finish()
    }
}
