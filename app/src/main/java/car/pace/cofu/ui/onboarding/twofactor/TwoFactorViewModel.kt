package car.pace.cofu.ui.onboarding.twofactor

import androidx.annotation.StringRes
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import car.pace.cofu.R
import car.pace.cofu.data.UserRepository
import car.pace.cofu.ui.onboarding.twofactor.setup.BiometrySetup
import car.pace.cofu.ui.onboarding.twofactor.setup.PinSetup
import car.pace.cofu.ui.onboarding.twofactor.setup.TwoFactorSetup
import car.pace.cofu.util.SnackbarData
import car.pace.cofu.util.UserCanceledException
import cloud.pace.sdk.idkit.model.InvalidSession
import dagger.hilt.android.lifecycle.HiltViewModel
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

@HiltViewModel
class TwoFactorViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _setupFinished = MutableSharedFlow<Unit>()
    val setupFinished = _setupFinished.asSharedFlow()

    private val _navigateToAuthorization = MutableSharedFlow<Unit>()
    val navigateToAuthorization = _navigateToAuthorization.asSharedFlow()

    private val _snackbar = MutableStateFlow<SnackbarData?>(null)
    val snackbar = _snackbar.asStateFlow()

    var loading by mutableStateOf(false)
    var twoFactorSetup: TwoFactorSetup? by mutableStateOf(null)

    fun enableBiometricAuthentication() {
        viewModelScope.launch {
            loading = true
            userRepository.enableBiometricAuthentication()
                .onSuccess {
                    if (it) {
                        finish()
                    } else {
                        startBiometrySetup()
                    }
                }
                .onFailure {
                    loading = false
                    Timber.e(it, "Failed to enable biometric authentication")
                    showSnackbar(throwable = it, onActionPerformed = ::enableBiometricAuthentication)
                }
        }
    }

    fun onBiometricPromptError(errString: CharSequence) {
        showSnackbar(generalMessageRes = R.string.ONBOARDING_FINGERPRINT_ERROR, generalMessageFormatArgs = arrayOf(errString))
    }

    fun isPinSet() {
        viewModelScope.launch {
            loading = true
            userRepository.isPINSet()
                .onSuccess {
                    if (it) {
                        finish()
                    } else {
                        startPinSetup()
                    }
                }
                .onFailure {
                    loading = false
                    Timber.e(it, "Failed to check if PIN is set")
                    showSnackbar(throwable = it, onActionPerformed = ::isPinSet)
                }
        }
    }

    fun onTwoFactorSetupFinished(result: Result<Unit>) {
        twoFactorSetup = null
        result
            .onSuccess {
                finish()
            }
            .onFailure {
                loading = false
                if (it !is UserCanceledException) {
                    showSnackbar(it)
                }
            }
    }

    fun showSnackbar(
        throwable: Throwable? = null,
        @StringRes generalMessageRes: Int = R.string.ONBOARDING_UNKNOWN_ERROR,
        vararg generalMessageFormatArgs: Any? = emptyArray(),
        onActionPerformed: () -> Unit = {}
    ) {
        _snackbar.value = when (throwable) {
            is InvalidSession -> {
                SnackbarData(
                    messageRes = R.string.ONBOARDING_INVALID_SESSION,
                    actionLabelRes = R.string.ONBOARDING_RETRY_LOGIN,
                    onActionPerformed = {
                        viewModelScope.launch {
                            _navigateToAuthorization.emit(Unit)
                        }
                    }
                )
            }

            is UnknownHostException, is SocketTimeoutException -> {
                SnackbarData(
                    messageRes = R.string.ONBOARDING_NETWORK_ERROR,
                    actionLabelRes = R.string.common_retry,
                    onActionPerformed = onActionPerformed
                )
            }

            else -> {
                SnackbarData(
                    messageRes = generalMessageRes,
                    messageFormatArgs = generalMessageFormatArgs
                )
            }
        }
    }

    private suspend fun startBiometrySetup() {
        loading = true
        userRepository.sendMailOTP()
            .onSuccess {
                twoFactorSetup = BiometrySetup
            }
            .onFailure {
                loading = false
                Timber.e(it, "Failed to send OTP mail")
                showSnackbar(it)
            }
    }

    private fun startPinSetup() {
        twoFactorSetup = PinSetup.PinInput
    }

    private fun finish() {
        viewModelScope.launch {
            _setupFinished.emit(Unit)
        }
    }
}
