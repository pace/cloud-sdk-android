package car.pace.cofu.ui.wallet.authorization

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material.icons.outlined.Pin
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import car.pace.cofu.R
import car.pace.cofu.ui.component.DefaultDialog
import car.pace.cofu.ui.component.DefaultListItem
import car.pace.cofu.ui.component.SwitchInfo
import car.pace.cofu.ui.component.TextTopBar
import car.pace.cofu.ui.onboarding.twofactor.BiometricSetupDialog
import car.pace.cofu.ui.onboarding.twofactor.biometric.rememberBiometricManager
import car.pace.cofu.ui.onboarding.twofactor.biometric.rememberBiometricPrompt
import car.pace.cofu.ui.onboarding.twofactor.setup.TwoFactorSetup
import car.pace.cofu.ui.theme.AppTheme
import car.pace.cofu.util.IntentUtils
import car.pace.cofu.util.LogAndBreadcrumb

@Composable
fun AuthorisationScreen(
    viewModel: AuthorisationViewModel = hiltViewModel(),
    onNavigateUp: () -> Unit
) {
    val context = LocalContext.current
    var showBiometricSetupDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val biometricManager = rememberBiometricManager()
    val canAuthenticate = remember {
        val allowedStates = arrayOf(BiometricManager.BIOMETRIC_SUCCESS, BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE, BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED)
        biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK) in allowedStates
    }
    val biometricPrompt = if (canAuthenticate) {
        rememberBiometricPrompt(
            onSuccess = {
                viewModel.enableBiometricAuthentication()
            },
            onError = { errorCode, errString ->
                LogAndBreadcrumb.i(LogAndBreadcrumb.AUTHORISATION, "Biometric error: $errString")
                if (errorCode == BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED) {
                    showBiometricSetupDialog = true
                } else if (errorCode != BiometricPrompt.ERROR_USER_CANCELED && errorCode != BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
                    errorMessage = errString.toString()
                }
            }
        )
    } else {
        null
    }

    LaunchedEffect(Unit) {
        viewModel.errorText.collect {
            if (it != null) {
                errorMessage = context.getString(it)
            }
        }
    }

    AuthorisationScreenContent(
        canAuthenticate = canAuthenticate,
        biometricPrompt = biometricPrompt,
        isBiometricAuthenticationEnabled = viewModel.isBiometricAuthenticationEnabled,
        onNavigateUp = onNavigateUp,
        onPinSetupClick = viewModel::startPinSetUp,
        onBiometricDisabled = viewModel::disableBiometricAuthentication
    )

    if (showBiometricSetupDialog) {
        BiometricSetupDialog(
            onConfirm = {
                showBiometricSetupDialog = false
                IntentUtils.openBiometricSettings(context).onFailure {
                    LogAndBreadcrumb.e(it, LogAndBreadcrumb.AUTHORISATION, "Could not launch biometry setup settings")
                    viewModel.onFingerprintSettingsNotFound()
                }
            },
            onDismiss = {
                showBiometricSetupDialog = false
            }
        )
    }

    val twoFactorSetupType = viewModel.twoFactorSetupType
    if (twoFactorSetupType != null) {
        TwoFactorSetup(
            type = twoFactorSetupType,
            onResult = viewModel::onTwoFactorSetupFinished
        )
    }

    val message = errorMessage
    if (message != null) {
        DefaultDialog(
            title = context.getString(R.string.wallet_two_factor_authentication_biometry_error),
            text = message,
            confirmButtonText = context.getString(R.string.common_use_ok),
            onConfirm = {
                errorMessage = null
            }
        )
    }
}

@Composable
fun AuthorisationScreenContent(
    canAuthenticate: Boolean,
    biometricPrompt: BiometricPrompt?,
    isBiometricAuthenticationEnabled: Boolean,
    onNavigateUp: () -> Unit,
    onPinSetupClick: () -> Unit,
    onBiometricDisabled: () -> Unit
) {
    var showDisableBiometricDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Column {
        TextTopBar(
            text = stringResource(id = R.string.wallet_two_factor_authentication_title),
            onNavigateUp = onNavigateUp
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            DefaultListItem(
                modifier = Modifier.clickable(
                    role = Role.Button,
                    onClick = onPinSetupClick
                ),
                icon = Icons.Outlined.Pin,
                title = stringResource(id = R.string.wallet_two_factor_authentication_pin_title)
            )

            if (canAuthenticate) {
                DefaultListItem(
                    icon = Icons.Outlined.Fingerprint,
                    title = stringResource(id = R.string.wallet_two_factor_authentication_biometry_title),
                    switchInfo = SwitchInfo(isBiometricAuthenticationEnabled) {
                        if (it) {
                            LogAndBreadcrumb.i(LogAndBreadcrumb.AUTHORISATION, "User starts biometry setup")
                            val info = BiometricPrompt.PromptInfo.Builder()
                                .setTitle(context.getString(R.string.onboarding_authorization_request_fingerprint))
                                .setNegativeButtonText(context.getString(R.string.common_use_back))
                                .build()

                            biometricPrompt?.authenticate(info)
                        } else {
                            showDisableBiometricDialog = true
                        }
                    }
                )
            }
        }
    }

    if (showDisableBiometricDialog) {
        DefaultDialog(
            title = context.getString(R.string.wallet_two_factor_authentication_biometry_disable_title),
            text = context.getString(R.string.wallet_two_factor_authentication_biometry_disable_text),
            confirmButtonText = context.getString(R.string.common_use_deactivate),
            dismissButtonText = context.getString(R.string.common_use_cancel),
            onConfirm = {
                showDisableBiometricDialog = false
                onBiometricDisabled()
            },
            onDismiss = {
                showDisableBiometricDialog = false
            }
        )
    }
}

@Preview
@Composable
fun AuthorisationScreenContentPreview() {
    AppTheme {
        AuthorisationScreenContent(
            canAuthenticate = true,
            biometricPrompt = null,
            isBiometricAuthenticationEnabled = true,
            onNavigateUp = {},
            onPinSetupClick = {},
            onBiometricDisabled = {}
        )
    }
}
