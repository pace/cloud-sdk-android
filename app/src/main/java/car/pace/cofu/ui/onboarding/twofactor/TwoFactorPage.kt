package car.pace.cofu.ui.onboarding.twofactor

import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import car.pace.cofu.R
import car.pace.cofu.ui.component.DefaultDialog
import car.pace.cofu.ui.component.Description
import car.pace.cofu.ui.component.SecondaryButton
import car.pace.cofu.ui.onboarding.PageScaffold
import car.pace.cofu.ui.onboarding.twofactor.biometric.rememberBiometricManager
import car.pace.cofu.ui.onboarding.twofactor.biometric.rememberBiometricPrompt
import car.pace.cofu.ui.onboarding.twofactor.setup.TwoFactorSetup
import car.pace.cofu.ui.theme.AppTheme
import timber.log.Timber

@Composable
fun TwoFactorPage(
    viewModel: TwoFactorViewModel = hiltViewModel(),
    onAuthorization: () -> Unit,
    onNext: () -> Unit
) {
    val context = LocalContext.current
    var showBiometricSetupDialog by remember { mutableStateOf(false) }
    val biometricManager = rememberBiometricManager()
    val canAuthenticate = remember {
        val allowedStates = arrayOf(BiometricManager.BIOMETRIC_SUCCESS, BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE, BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED)
        biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK) in allowedStates
    }
    val biometricPrompt = rememberBiometricPrompt(
        onSuccess = {
            viewModel.enableBiometricAuthentication(context)
        },
        onError = { errorCode, errString ->
            if (errorCode == BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED) {
                showBiometricSetupDialog = true
            } else {
                viewModel.onBiometricPromptError(context, errString)
            }
        }
    )

    LaunchedEffect(Unit) {
        viewModel.setupFinished.collect {
            onNext()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.navigateToAuthorization.collect {
            onAuthorization()
        }
    }

    PageScaffold(
        imageVector = Icons.Outlined.Lock,
        titleRes = R.string.onboarding_two_factor_authentication_title,
        nextButtonTextRes = if (canAuthenticate) R.string.onboarding_two_factor_authentication_biometry else R.string.onboarding_two_factor_authentication_pin,
        nextButtonEnabled = !viewModel.biometryLoading && !viewModel.pinLoading,
        nextButtonLoading = viewModel.biometryLoading,
        onNextButtonClick = {
            val info = BiometricPrompt.PromptInfo.Builder()
                .setTitle(context.getString(R.string.onboarding_authorization_request_fingerprint))
                .setNegativeButtonText(context.getString(R.string.common_use_back))
                .build()

            biometricPrompt.authenticate(info)
        },
        descriptionContent = {
            Description(
                text = stringResource(id = R.string.onboarding_two_factor_authentication_description)
            )
        },
        errorText = viewModel.errorText,
        footerContent = {
            if (canAuthenticate) {
                SecondaryButton(
                    text = stringResource(id = R.string.onboarding_two_factor_authentication_pin),
                    modifier = Modifier.padding(start = 20.dp, top = 12.dp, end = 20.dp),
                    enabled = !viewModel.pinLoading && !viewModel.biometryLoading,
                    loading = viewModel.pinLoading,
                    onClick = {
                        viewModel.isPinSet(context)
                    }
                )
            }
        }
    )

    val twoFactorSetupType = viewModel.twoFactorSetupType
    if (twoFactorSetupType != null) {
        TwoFactorSetup(
            type = twoFactorSetupType,
            onResult = viewModel::onTwoFactorSetupFinished
        )
    }

    if (showBiometricSetupDialog) {
        BiometricSetupDialog(
            onConfirm = {
                showBiometricSetupDialog = false

                try {
                    val intent = when {
                        Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> Intent(Settings.ACTION_BIOMETRIC_ENROLL)
                        Build.VERSION.SDK_INT >= Build.VERSION_CODES.P -> Intent(Settings.ACTION_FINGERPRINT_ENROLL)
                        else -> Intent(Settings.ACTION_SECURITY_SETTINGS)
                    }
                    context.startActivity(intent)
                } catch (e: Exception) {
                    try {
                        context.startActivity(Intent(Settings.ACTION_SECURITY_SETTINGS))
                    } catch (e: Exception) {
                        Timber.e(e, "Could not launch biometry setup settings")
                        viewModel.onFingerprintSettingsNotFound(context)
                    }
                }
            },
            onDismiss = {
                showBiometricSetupDialog = false
            }
        )
    }
}

@Composable
fun BiometricSetupDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    DefaultDialog(
        title = stringResource(id = R.string.onboarding_fingerprint_none_saved_title),
        text = stringResource(id = R.string.onboarding_fingerprint_none_saved_text),
        confirmButtonText = stringResource(id = R.string.onboarding_fingerprint_save),
        dismissButtonText = stringResource(id = R.string.common_use_cancel),
        imageVector = Icons.Outlined.Fingerprint,
        onConfirm = onConfirm,
        onDismiss = onDismiss
    )
}

@Preview
@Composable
fun TwoFactorPagePreview() {
    AppTheme {
        TwoFactorPage(
            onAuthorization = {},
            onNext = {}
        )
    }
}

@Preview
@Composable
fun BiometricSetupDialogPreview() {
    AppTheme {
        BiometricSetupDialog(
            onConfirm = {},
            onDismiss = {}
        )
    }
}
