package car.pace.cofu.ui.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import car.pace.cofu.R
import car.pace.cofu.ui.theme.AppTheme

@Composable
fun DefaultDialog(
    title: String,
    text: String? = null,
    confirmButtonText: String,
    dismissButtonText: String? = null,
    imageVector: ImageVector? = null,
    imageTint: Color? = null,
    onConfirm: () -> Unit = {},
    onDismiss: () -> Unit = {}
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            PrimaryButton(
                text = confirmButtonText,
                onClick = onConfirm
            )
        },
        dismissButton = if (dismissButtonText != null) {
            {
                SecondaryButton(
                    text = dismissButtonText,
                    onClick = onDismiss
                )
            }
        } else {
            null
        },
        icon = if (imageVector != null) {
            {
                Icon(
                    imageVector = imageVector,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = imageTint ?: LocalContentColor.current
                )
            }
        } else {
            null
        },
        title = {
            Title(
                modifier = Modifier.fillMaxWidth(),
                text = title
            )
        },
        text = if (text != null) {
            {
                Description(
                    modifier = Modifier.fillMaxWidth(),
                    text = text
                )
            }
        } else {
            null
        },
        containerColor = MaterialTheme.colorScheme.background,
        iconContentColor = MaterialTheme.colorScheme.onSurface,
        titleContentColor = MaterialTheme.colorScheme.onPrimary,
        textContentColor = MaterialTheme.colorScheme.onPrimary,
        tonalElevation = 0.dp
    )
}

@Composable
fun FuelingLegalWarningDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    DefaultDialog(
        title = stringResource(id = R.string.common_use_attention),
        text = stringResource(id = R.string.fueling_legal_warning_description),
        confirmButtonText = stringResource(id = R.string.common_use_confirm),
        dismissButtonText = stringResource(id = R.string.common_use_cancel),
        onConfirm = onConfirm,
        onDismiss = onDismiss
    )
}

@Composable
fun MissingPaymentMethodDialog(
    canAddPaymentMethods: Boolean,
    onConfirm: () -> Unit
) {
    val title = if (canAddPaymentMethods) R.string.payment_methods_empty_title else R.string.managed_payment_methods_empty_title
    val text = if (canAddPaymentMethods) R.string.payment_methods_empty_description else R.string.managed_payment_methods_empty_description

    DefaultDialog(
        title = stringResource(title),
        text = stringResource(text),
        confirmButtonText = stringResource(id = R.string.common_use_ok),
        imageVector = Icons.Outlined.ErrorOutline,
        imageTint = MaterialTheme.colorScheme.error,
        onConfirm = onConfirm
    )
}

@Preview
@Composable
fun FuelingLegalWarningDialogPreview() {
    AppTheme {
        FuelingLegalWarningDialog(
            onConfirm = {},
            onDismiss = {}
        )
    }
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
            imageVector = Icons.AutoMirrored.Outlined.Logout
        )
    }
}

@Preview
@Composable
fun DefaultInfoDialog() {
    AppTheme {
        DefaultDialog(
            title = "Unexpected error",
            text = "Something happened",
            confirmButtonText = "Ok"
        )
    }
}
