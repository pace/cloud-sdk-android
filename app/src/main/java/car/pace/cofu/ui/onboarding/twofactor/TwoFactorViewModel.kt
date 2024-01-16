package car.pace.cofu.ui.onboarding.twofactor

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import car.pace.cofu.R
import car.pace.cofu.data.UserRepository
import car.pace.cofu.ui.onboarding.twofactor.setup.TwoFactorSetupType
import car.pace.cofu.util.LogAndBreadcrumb
import car.pace.cofu.util.extension.errorTextRes
import cloud.pace.sdk.idkit.model.InvalidSession
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

@HiltViewModel
class TwoFactorViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _setupFinished = MutableSharedFlow<Unit>()
    val setupFinished = _setupFinished.asSharedFlow()

    private val _navigateToAuthorization = MutableSharedFlow<Unit>()
    val navigateToAuthorization = _navigateToAuthorization.asSharedFlow()

    var errorText: String? by mutableStateOf(null)
    var biometryLoading by mutableStateOf(false)
    var pinLoading by mutableStateOf(false)
    var twoFactorSetupType: TwoFactorSetupType? by mutableStateOf(null)

    fun enableBiometricAuthentication(context: Context) {
        viewModelScope.launch {
            biometryLoading = true
            userRepository.enableBiometricAuthentication()
                .onSuccess {
                    biometryLoading = false
                    if (it) {
                        LogAndBreadcrumb.i(LogAndBreadcrumb.ONBOARDING, "Use biometry for authentication")
                        finish()
                    } else {
                        LogAndBreadcrumb.i(LogAndBreadcrumb.ONBOARDING, "Start biometry setup")
                        twoFactorSetupType = TwoFactorSetupType.BIOMETRY
                    }
                }
                .onFailure {
                    biometryLoading = false
                    LogAndBreadcrumb.e(it, LogAndBreadcrumb.ONBOARDING, "Failed to enable biometric authentication")
                    handleError(context, it)
                }
        }
    }

    fun onBiometricPromptError(context: Context, errString: CharSequence) {
        errorText = context.getString(R.string.onboarding_fingerprint_error, errString)
    }

    fun onFingerprintSettingsNotFound(context: Context) {
        errorText = context.getString(R.string.onboarding_fingerprint_setup_error)
    }

    fun isPinSet(context: Context) {
        viewModelScope.launch {
            pinLoading = true
            userRepository.isPINSet()
                .onSuccess {
                    pinLoading = false
                    if (it) {
                        LogAndBreadcrumb.i(LogAndBreadcrumb.ONBOARDING, "Use pin for authentication")
                        finish()
                    } else {
                        LogAndBreadcrumb.i(LogAndBreadcrumb.ONBOARDING, "Start pin setup")
                        twoFactorSetupType = TwoFactorSetupType.PIN
                    }
                }
                .onFailure {
                    pinLoading = false
                    LogAndBreadcrumb.e(it, LogAndBreadcrumb.ONBOARDING, "Failed to check if PIN is set")
                    handleError(context, it)
                }
        }
    }

    fun onTwoFactorSetupFinished(successful: Boolean) {
        if (successful) {
            LogAndBreadcrumb.i(LogAndBreadcrumb.ONBOARDING, "Setup two factor authentication: ${twoFactorSetupType?.name}")
            finish()
        }

        twoFactorSetupType = null
    }

    private fun handleError(context: Context, throwable: Throwable) {
        if (throwable is InvalidSession) {
            viewModelScope.launch {
                _navigateToAuthorization.emit(Unit)
            }
        } else {
            errorText = context.getString(throwable.errorTextRes())
        }
    }

    private fun finish() {
        viewModelScope.launch {
            _setupFinished.emit(Unit)
        }
    }
}
