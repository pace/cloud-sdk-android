package cloud.pace.sdk.app.ui.components

import android.widget.Toast
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.text.font.FontWeight
import cloud.pace.sdk.app.MainScreenActivity

/**
 * Opens a dialog that asks the user if he likes to enable BioAuth
 */

@Composable
fun SuccessfulLoginDialog(activity: MainScreenActivity, enableBioAuth: () -> Unit) {
    val openDialog = remember { mutableStateOf(true) }
    if (openDialog.value) {
        AlertDialog(
            onDismissRequest = {
                openDialog.value = false
            },
            title = {
                Text(
                    text = "You were successfully logged in. Welcome!",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text("Do you want to enable biometric authentication? You can still choose to do this afterwards, but for security reasons you will then also have to enter a password or pin.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        openDialog.value = false
                        enableBioAuth()
                    }
                ) {
                    Text("Yes")
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        openDialog.value = false
                        Toast.makeText(activity, "declined enabling Biometric authentication!", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("No")
                }
            },
        )
    }
}
