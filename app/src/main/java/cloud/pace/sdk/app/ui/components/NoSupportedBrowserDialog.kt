package cloud.pace.sdk.app.ui.components

import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import cloud.pace.sdk.app.R

@Composable
fun NoSupportedBrowserDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("No supported browser")
        },
        text = {
            Text(stringResource(R.string.no_supported_browser_toast))
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("OK")
            }
        }
    )
}
