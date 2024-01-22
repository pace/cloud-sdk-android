package car.pace.cofu.ui.wallet.authorization

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
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

@HiltViewModel
class AuthorisationViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    var isBiometricAuthenticationEnabled by mutableStateOf(userRepository.isBiometricAuthenticationEnabled())
        private set
    var twoFactorSetupType: TwoFactorSetupType? by mutableStateOf(null)
        private set
    val errorText = MutableSharedFlow<Int?>()

    fun enableBiometricAuthentication() {
        viewModelScope.launch {
            userRepository.enableBiometricAuthentication()
                .onSuccess {
                    if (it) {
                        isBiometricAuthenticationEnabled = true
                    } else {
                        LogAndBreadcrumb.i(LogAndBreadcrumb.ONBOARDING, "Start biometry setup")
                        twoFactorSetupType = TwoFactorSetupType.BIOMETRY
                    }
                }
                .onFailure {
                    LogAndBreadcrumb.e(it, LogAndBreadcrumb.AUTHORISATION, "Failed to enable biometric authentication")
                    errorText.emit(it.errorTextRes())
                }
        }
    }

    fun startPinSetUp() {
        LogAndBreadcrumb.i(LogAndBreadcrumb.AUTHORISATION, "User starts pin setup")
        twoFactorSetupType = TwoFactorSetupType.PIN
    }

    fun disableBiometricAuthentication() {
        userRepository.disableBiometricAuthentication()
        isBiometricAuthenticationEnabled = false
        LogAndBreadcrumb.i(LogAndBreadcrumb.AUTHORISATION, "Biometric authentication disabled")
    }

    fun onFingerprintSettingsNotFound() {
        viewModelScope.launch {
            errorText.emit(R.string.onboarding_fingerprint_setup_error)
        }
    }

    fun onTwoFactorSetupFinished(successful: Boolean) {
        if (successful) {
            isBiometricAuthenticationEnabled = userRepository.isBiometricAuthenticationEnabled()
            LogAndBreadcrumb.i(LogAndBreadcrumb.AUTHORISATION, "Setup two factor authentication: ${twoFactorSetupType?.name}")
        }

        twoFactorSetupType = null
    }
}
