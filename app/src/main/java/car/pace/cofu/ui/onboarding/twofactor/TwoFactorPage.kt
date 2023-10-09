package car.pace.cofu.ui.onboarding.twofactor

import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SnackbarHostState
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
import car.pace.cofu.ui.component.DefaultCircularProgressIndicator
import car.pace.cofu.ui.component.DefaultTextButton
import car.pace.cofu.ui.onboarding.PageScaffold
import car.pace.cofu.ui.onboarding.twofactor.biometric.BiometricSetupDialog
import car.pace.cofu.ui.onboarding.twofactor.biometric.rememberBiometricManager
import car.pace.cofu.ui.onboarding.twofactor.biometric.rememberBiometricPrompt
import car.pace.cofu.ui.onboarding.twofactor.setup.BiometrySetup
import car.pace.cofu.ui.onboarding.twofactor.setup.PinSetup
import car.pace.cofu.ui.onboarding.twofactor.setup.biometry.BiometrySetup
import car.pace.cofu.ui.onboarding.twofactor.setup.pin.PinSetup
import car.pace.cofu.ui.theme.AppTheme
import car.pace.cofu.util.showSnackbar
import kotlinx.coroutines.flow.collectLatest

@Composable
fun TwoFactorPage(
    viewModel: TwoFactorViewModel = hiltViewModel(),
    snackbarHostState: SnackbarHostState,
    onAuthorization: () -> Unit,
    onNext: () -> Unit
) {
    val context = LocalContext.current
    var openBiometricSetupDialog by remember { mutableStateOf(false) }
    val biometricManager = rememberBiometricManager()
    val canAuthenticate = remember {
        val allowedStates = arrayOf(BiometricManager.BIOMETRIC_SUCCESS, BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE, BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED)
        biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK) in allowedStates
    }
    val biometricPrompt = rememberBiometricPrompt(
        onSuccess = {
            viewModel.enableBiometricAuthentication()
        },
        onError = { errorCode, errString ->
            if (errorCode == BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED) {
                openBiometricSetupDialog = true
            } else {
                viewModel.onBiometricPromptError(errString)
            }
        }
    )

    LaunchedEffect(snackbarHostState) {
        viewModel.snackbar.collectLatest {
            it?.showSnackbar(context, snackbarHostState)
        }
    }

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
        imageRes = R.drawable.ic_scan,
        titleRes = R.string.ONBOARDING_TWO_FACTOR_AUTHENTICATION_TITLE,
        descriptionRes = R.string.ONBOARDING_TWO_FACTOR_AUTHENTICATION_DESCRIPTION,
        nextButtonTextRes = if (canAuthenticate) R.string.ONBOARDING_TWO_FACTOR_AUTHENTICATION_BIOMETRY else R.string.ONBOARDING_TWO_FACTOR_AUTHENTICATION_PIN,
        nextButtonEnabled = !viewModel.loading,
        onNextButtonClick = {
            val info = BiometricPrompt.PromptInfo.Builder()
                .setTitle(context.getString(R.string.ONBOARDING_AUTHORIZATION_REQUEST_FINGERPRINT))
                .setNegativeButtonText(context.getString(R.string.ONBOARDING_ACTIONS_BACK))
                .build()

            biometricPrompt.authenticate(info)
        },
        footerContent = {
            if (canAuthenticate) {
                DefaultTextButton(
                    text = stringResource(id = R.string.ONBOARDING_TWO_FACTOR_AUTHENTICATION_PIN).uppercase(),
                    enabled = !viewModel.loading,
                    onClick = viewModel::isPinSet
                )
            }
        }
    ) {
        if (viewModel.loading) {
            DefaultCircularProgressIndicator(
                modifier = Modifier.padding(horizontal = 35.dp, vertical = 10.dp)
            )
        }
    }

    when (viewModel.twoFactorSetup) {
        is BiometrySetup -> BiometrySetup(onDismiss = viewModel::onTwoFactorSetupFinished)
        is PinSetup -> PinSetup(onDismiss = viewModel::onTwoFactorSetupFinished)
        else -> {}
    }

    if (openBiometricSetupDialog) {
        BiometricSetupDialog(
            onConfirmation = {
                openBiometricSetupDialog = false

                try {
                    val intent = when {
                        Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> Intent(Settings.ACTION_BIOMETRIC_ENROLL)
                        Build.VERSION.SDK_INT >= Build.VERSION_CODES.P -> Intent(Settings.ACTION_FINGERPRINT_ENROLL)
                        else -> Intent(Settings.ACTION_SECURITY_SETTINGS)
                    }
                    context.startActivity(intent)
                } catch (e: Exception) {
                    context.startActivity(Intent(Settings.ACTION_SECURITY_SETTINGS))
                }
            },
            onDismiss = {
                openBiometricSetupDialog = false
            }
        )
    }
}

@Preview
@Composable
fun TwoFactorPagePreview() {
    AppTheme {
        val snackbarHostState = remember { SnackbarHostState() }
        TwoFactorPage(
            snackbarHostState = snackbarHostState,
            onAuthorization = {},
            onNext = {}
        )
    }
}
