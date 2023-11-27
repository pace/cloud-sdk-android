package car.pace.cofu.ui.home.dialog

import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import car.pace.cofu.R
import car.pace.cofu.ui.component.Description
import car.pace.cofu.ui.component.PrimaryButton
import car.pace.cofu.ui.component.Title
import car.pace.cofu.ui.theme.AppTheme

@Composable
fun LocationPermissionDialog(
    onConfirmation: () -> Unit,
    onDismiss: () -> Unit
) {
    InsufficientLocationDialog(
        title = stringResource(id = R.string.LOCATION_DIALOG_PERMISSION_DENIED_TITLE),
        text = stringResource(id = R.string.LOCATION_DIALOG_PERMISSION_DENIED_TEXT),
        buttonText = stringResource(id = R.string.ALERT_LOCATION_PERMISSION_ACTIONS_OPEN_SETTINGS),
        onConfirmation = onConfirmation,
        onDismiss = onDismiss
    )
}

@Composable
fun LocationDisabledDialog(
    onConfirmation: () -> Unit,
    onDismiss: () -> Unit
) {
    InsufficientLocationDialog(
        title = stringResource(id = R.string.LOCATION_DIALOG_DISABLED_TITLE),
        text = stringResource(id = R.string.LOCATION_DIALOG_DISABLED_TEXT),
        buttonText = stringResource(id = R.string.ALERT_LOCATION_PERMISSION_ACTIONS_OPEN_SETTINGS),
        onConfirmation = onConfirmation,
        onDismiss = onDismiss
    )
}

@Composable
fun InsufficientLocationDialog(
    title: String,
    text: String,
    buttonText: String,
    onConfirmation: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            PrimaryButton(
                text = buttonText.uppercase(),
                onClick = onConfirmation
            )
        },
        title = {
            Title(text = title)
        },
        text = {
            Description(text = text)
        }
    )
}

@Preview
@Composable
fun LocationPermissionDialogPreview() {
    AppTheme {
        LocationPermissionDialog(
            onConfirmation = {},
            onDismiss = {}
        )
    }
}

@Preview
@Composable
fun LocationDisabledDialogPreview() {
    AppTheme {
        LocationDisabledDialog(
            onConfirmation = {},
            onDismiss = {}
        )
    }
}
