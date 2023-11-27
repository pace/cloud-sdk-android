package car.pace.cofu.ui.onboarding.twofactor.setup.biometry

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import car.pace.cofu.data.UserRepository
import car.pace.cofu.util.extension.UserCanceledException
import car.pace.cofu.util.extension.errorTextRes
import cloud.pace.sdk.idkit.model.InternalError
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import timber.log.Timber

@HiltViewModel
class BiometrySetupViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    var errorTextRes: Int? by mutableStateOf(null)

    private val _setupFinished = MutableSharedFlow<Result<Unit>>()
    val setupFinished = _setupFinished.asSharedFlow()

    fun next(input: String?) {
        errorTextRes = null

        if (input != null) {
            setBiometryWithOtp(input)
        } else {
            finish(Result.failure(UserCanceledException()))
        }
    }

    private fun setBiometryWithOtp(otp: String) {
        viewModelScope.launch {
            userRepository.enableBiometricAuthenticationWithOTP(otp)
                .onSuccess {
                    if (it) {
                        finish(Result.success(Unit))
                    } else {
                        errorTextRes = InternalError.errorTextRes()
                    }
                }
                .onFailure {
                    Timber.e(it, "Failed to enable biometric authentication with OTP")
                    errorTextRes = it.errorTextRes()
                }
        }
    }

    private fun finish(result: Result<Unit>) {
        viewModelScope.launch {
            _setupFinished.emit(result)
        }
    }
}
