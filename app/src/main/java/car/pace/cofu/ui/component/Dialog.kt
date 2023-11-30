package car.pace.cofu.ui.component

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import car.pace.cofu.ui.theme.AppTheme

@Composable
fun DefaultDialog(
    title: String,
    text: String,
    confirmButtonText: String,
    dismissButtonText: String,
    imageVector: ImageVector,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            PrimaryButton(
                text = confirmButtonText,
                onClick = onConfirm
            )
        },
        dismissButton = {
            SecondaryButton(
                text = dismissButtonText,
                onClick = onDismiss
            )
        },
        icon = {
            Icon(
                imageVector = imageVector,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        title = {
            Title(text = title)
        },
        text = {
            Description(text = text)
        },
        tonalElevation = 0.dp
    )
}

@Preview
@Composable
fun DefaultDialogPreview() {
    AppTheme {
        DefaultDialog(
            title = "Dialog title",
            text = "Dialog text",
            confirmButtonText = "Confirm",
            dismissButtonText = "Cancel",
            imageVector = Icons.Outlined.Logout,
            onConfirm = { },
            onDismiss = {}
        )
    }
}
