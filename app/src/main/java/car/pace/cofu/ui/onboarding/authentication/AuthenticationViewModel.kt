package car.pace.cofu.ui.onboarding.authentication

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import car.pace.cofu.R
import car.pace.cofu.data.PaymentMethodKindsRepository
import car.pace.cofu.data.PaymentMethodRepository
import car.pace.cofu.data.analytics.Analytics
import car.pace.cofu.data.analytics.UserSignedIn
import car.pace.cofu.ui.onboarding.twofactor.biometric.findActivity
import car.pace.cofu.util.LogAndBreadcrumb
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
    private val analytics: Analytics,
    private val paymentMethodKindsRepository: PaymentMethodKindsRepository,
    private val paymentMethodRepository: PaymentMethodRepository
) : ViewModel() {

    data class AuthenticationResult(
        val twoFactorEnabled: Boolean,
        val paymentMethodManagementEnabled: Boolean,
        val userHasPaymentMethods: Boolean
    )

    var loading by mutableStateOf(false)

    private val _loginFinished = MutableSharedFlow<AuthenticationResult>()
    val loginFinished = _loginFinished.asSharedFlow()

    var errorText: String? by mutableStateOf(null)

    fun login(context: Context) {
        viewModelScope.launch {
            loading = true

            val activity = context.findActivity<AppCompatActivity>()
            when (IDKit.authorize(activity)) {
                is Success -> {
                    analytics.logEvent(UserSignedIn)
                    finish()
                }

                is Failure -> {
                    loading = false
                    errorText = context.getString(R.string.onboarding_login_unsuccessful)
                }
            }
        }
    }

    private suspend fun finish() {
        val paymentMethodKindsResult = paymentMethodKindsRepository.checkPaymentMethodKinds()
        LogAndBreadcrumb.i(LogAndBreadcrumb.ONBOARDING, if (paymentMethodKindsResult.twoFactorNeeded) "Two factor is enabled" else "Two factor is not enabled")
        LogAndBreadcrumb.i(
            LogAndBreadcrumb.ONBOARDING,
            if (paymentMethodKindsResult.paymentMethodManagementEnabled) "Payment method management is available" else "Payment method management is not available"
        )

        val hasPaymentMethod = paymentMethodRepository.getPaymentMethods(true)?.getOrNull()?.isNotEmpty() == true
        LogAndBreadcrumb.i(LogAndBreadcrumb.ONBOARDING, if (hasPaymentMethod) "User has payment methods" else "User has no payment methods")

        _loginFinished.emit(
            AuthenticationResult(
                twoFactorEnabled = paymentMethodKindsResult.twoFactorNeeded,
                paymentMethodManagementEnabled = paymentMethodKindsResult.paymentMethodManagementEnabled,
                userHasPaymentMethods = hasPaymentMethod
            )
        )
    }
}
