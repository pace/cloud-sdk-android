package car.pace.cofu.ui.onboarding.twofactor.biometric

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import car.pace.cofu.R
import car.pace.cofu.ui.component.DefaultTextButton
import car.pace.cofu.ui.component.Description
import car.pace.cofu.ui.theme.AppTheme

@Composable
fun BiometricSetupDialog(
    onConfirmation: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            DefaultTextButton(
                text = stringResource(id = R.string.onboarding_fingerprint_save).uppercase(),
                color = MaterialTheme.colorScheme.primary,
                onClick = onConfirmation
            )
        },
        dismissButton = {
            DefaultTextButton(
                text = stringResource(id = R.string.common_use_back),
                color = MaterialTheme.colorScheme.primary,
                onClick = onDismiss
            )
        },
        text = {
            Description(
                text = stringResource(id = R.string.onboarding_fingerprint_none_saved_title)
            )
        }
    )
}

@Preview
@Composable
fun BiometricSetupDialogPreview() {
    AppTheme {
        BiometricSetupDialog(
            onConfirmation = {},
            onDismiss = {}
        )
    }
}
