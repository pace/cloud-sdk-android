package car.pace.cofu.ui.onboarding.authentication

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import car.pace.cofu.R
import car.pace.cofu.features.analytics.Analytics
import car.pace.cofu.features.analytics.UserSignedIn
import car.pace.cofu.ui.onboarding.twofactor.biometric.findActivity
import cloud.pace.sdk.idkit.IDKit
import cloud.pace.sdk.utils.Failure
import cloud.pace.sdk.utils.Success
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

@HiltViewModel
class AuthenticationViewModel @Inject constructor(
    private val analytics: Analytics
) : ViewModel() {

    private val _loginFinished = MutableSharedFlow<Unit>()
    val loginFinished = _loginFinished.asSharedFlow()

    var errorText: String? by mutableStateOf(null)

    fun login(context: Context) {
        viewModelScope.launch {
            val activity = context.findActivity<AppCompatActivity>()
            when (IDKit.authorize(activity)) {
                is Success -> {
                    analytics.logEvent(UserSignedIn)
                    _loginFinished.emit(Unit)
                }
                is Failure -> errorText = context.getString(R.string.onboarding_login_unsuccessful)
            }
        }
    }
}
