package car.pace.cofu.ui.wallet.authorization

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import car.pace.cofu.R
import car.pace.cofu.data.UserRepository
import car.pace.cofu.ui.onboarding.twofactor.setup.TwoFactorSetupType
import car.pace.cofu.util.extension.errorTextRes
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import timber.log.Timber

@HiltViewModel
class AuthorisationViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    val isBiometricAuthenticationEnabled = mutableStateOf(userRepository.isBiometricAuthenticationEnabled())
    var twoFactorSetupType: TwoFactorSetupType? by mutableStateOf(null)
    var errorText = MutableSharedFlow<Int?>()

    fun enableBiometricAuthentication(context: Context) {
        viewModelScope.launch {
            userRepository.enableBiometricAuthentication()
                .onSuccess {
                    if (it) {
                        isBiometricAuthenticationEnabled.value = true
                    } else {
                        twoFactorSetupType = TwoFactorSetupType.BIOMETRY
                    }
                }
                .onFailure {
                    Timber.e(it, "Failed to enable biometric authentication")
                    errorText.emit(it.errorTextRes())
                }
        }
    }

    fun startPinSetUp() {
        twoFactorSetupType = TwoFactorSetupType.PIN
    }

    fun disableBiometricAuthentication() {
        userRepository.disableBiometricAuthentication()
        isBiometricAuthenticationEnabled.value = false
    }

    fun onFingerprintSettingsNotFound(context: Context) {
        viewModelScope.launch {
            errorText.emit(R.string.onboarding_fingerprint_setup_error)
        }
    }

    fun onTwoFactorSetupFinished(successful: Boolean) {
        twoFactorSetupType = null

        if (successful) {
            isBiometricAuthenticationEnabled.value = userRepository.isBiometricAuthenticationEnabled()
        }
    }
}
