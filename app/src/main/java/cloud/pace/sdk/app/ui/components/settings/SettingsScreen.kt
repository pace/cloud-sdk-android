package cloud.pace.sdk.app.ui.components.settings

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.LifecycleCoroutineScope
import cloud.pace.sdk.app.BiometrySubSettingsActivity
import cloud.pace.sdk.app.LoginScreenActivity
import cloud.pace.sdk.app.MainScreenActivity
import cloud.pace.sdk.app.R
import cloud.pace.sdk.app.ui.components.NoSupportedBrowserDialog
import cloud.pace.sdk.app.ui.theme.ButtonCornerShape
import cloud.pace.sdk.idkit.IDKit
import cloud.pace.sdk.idkit.model.NoSupportedBrowser
import cloud.pace.sdk.utils.Failure
import cloud.pace.sdk.utils.Success
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(activity: MainScreenActivity, lifecycleScope: LifecycleCoroutineScope, finishMainActivity: () -> Unit) {
    val openDialog = remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 10.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OpenSubSettingsButton {
            val intent = Intent(activity, BiometrySubSettingsActivity::class.java)
            activity.startActivity(intent)
        }
        MiscellaneousButton()
        LogoutButton {
            lifecycleScope.launch(Dispatchers.Main) {
                when (val result = IDKit.endSession(activity)) {
                    is Success -> {
                        val intent = Intent(activity, LoginScreenActivity::class.java)
                        activity.startActivity(intent)
                        finishMainActivity()
                    }

                    is Failure -> {
                        if (result.throwable is NoSupportedBrowser) {
                            openDialog.value = true
                        } else {
                            Toast.makeText(activity, result.throwable.message, Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }
    }

    if (openDialog.value) {
        NoSupportedBrowserDialog {
            openDialog.value = false
        }
    }
}

/**
 * Buttons used on the settings Screen
 */

@Composable
fun OpenSubSettingsButton(onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        shape = ButtonCornerShape,
        modifier = Modifier
            .padding(16.dp)
            .height(50.dp)
            .width(300.dp),
        border = BorderStroke(1.dp, Color.Black)
    ) {
        Text(
            text = "Biometry settings",
            fontSize = 16.sp
        )
        Icon(
            painter = painterResource(id = R.drawable.ic_baseline_chevron_right_24),
            contentDescription = null,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
fun MiscellaneousButton() {
    val context = LocalContext.current
    OutlinedButton(
        onClick = { Toast.makeText(context, "Clicked on the miscellaneous button", Toast.LENGTH_SHORT).show() },
        shape = ButtonCornerShape,
        modifier = Modifier
            .padding(12.dp)
            .height(50.dp)
            .width(300.dp),
        border = BorderStroke(1.dp, Color.Black)
    ) {
        Text(
            text = "Miscellaneous",
            fontSize = 16.sp
        )
    }
}

@Composable
fun LogoutButton(onClick: () -> Unit) {
    val context = LocalContext.current
    OutlinedButton(
        onClick = {
            Toast.makeText(context, "Clicked on logout button", Toast.LENGTH_SHORT).show()
            onClick()
        },
        shape = ButtonCornerShape,
        modifier = Modifier
            .padding(12.dp)
            .height(50.dp)
            .width(300.dp),
        border = BorderStroke(1.dp, Color.Black)
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_baseline_logout_24),
            contentDescription = "",
            modifier = Modifier
                .size(20.dp)
        )
        Text(
            text = "Logout",
            fontSize = 16.sp,
            modifier = Modifier
                .padding(6.dp, 0.dp)
        )
    }
}
