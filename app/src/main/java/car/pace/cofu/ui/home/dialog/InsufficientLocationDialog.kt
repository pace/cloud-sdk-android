package car.pace.cofu.ui.home.dialog

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocationOff
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import car.pace.cofu.R
import car.pace.cofu.ui.component.DefaultDialog
import car.pace.cofu.ui.theme.AppTheme

@Composable
fun LocationPermissionDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    InsufficientLocationDialog(
        title = stringResource(id = R.string.LOCATION_DIALOG_PERMISSION_DENIED_TITLE),
        text = stringResource(id = R.string.LOCATION_DIALOG_PERMISSION_DENIED_TEXT),
        confirmButtonText = stringResource(id = R.string.ALERT_LOCATION_PERMISSION_ACTIONS_OPEN_SETTINGS),
        onConfirm = onConfirm,
        onDismiss = onDismiss
    )
}

@Composable
fun LocationDisabledDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    InsufficientLocationDialog(
        title = stringResource(id = R.string.LOCATION_DIALOG_DISABLED_TITLE),
        text = stringResource(id = R.string.LOCATION_DIALOG_DISABLED_TEXT),
        confirmButtonText = stringResource(id = R.string.ALERT_LOCATION_PERMISSION_ACTIONS_OPEN_SETTINGS),
        onConfirm = onConfirm,
        onDismiss = onDismiss
    )
}

@Composable
fun InsufficientLocationDialog(
    title: String,
    text: String,
    confirmButtonText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    DefaultDialog(
        title = title,
        text = text,
        confirmButtonText = confirmButtonText,
        dismissButtonText = stringResource(id = R.string.common_use_cancel),
        imageVector = Icons.Outlined.LocationOff,
        onConfirm = onConfirm,
        onDismiss = onDismiss
    )
}

@Preview
@Composable
fun LocationPermissionDialogPreview() {
    AppTheme {
        LocationPermissionDialog(
            onConfirm = {},
            onDismiss = {}
        )
    }
}

@Preview
@Composable
fun LocationDisabledDialogPreview() {
    AppTheme {
        LocationDisabledDialog(
            onConfirm = {},
            onDismiss = {}
        )
    }
}
