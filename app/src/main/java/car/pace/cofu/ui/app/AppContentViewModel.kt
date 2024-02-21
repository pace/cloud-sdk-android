package car.pace.cofu.ui.app

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import car.pace.cofu.data.LegalRepository
import car.pace.cofu.data.PaymentMethodKindsRepository
import car.pace.cofu.data.SharedPreferencesRepository
import car.pace.cofu.data.SharedPreferencesRepository.Companion.PREF_KEY_ONBOARDING_DONE
import car.pace.cofu.data.UserRepository
import car.pace.cofu.ui.navigation.graph.Graph
import car.pace.cofu.util.Constants.STOP_TIMEOUT_MILLIS
import car.pace.cofu.util.LogAndBreadcrumb
import cloud.pace.sdk.idkit.model.InvalidSession
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class AppContentViewModel @Inject constructor(
    private val sharedPreferencesRepository: SharedPreferencesRepository,
    private val userRepository: UserRepository,
    private val paymentMethodKindsRepository: PaymentMethodKindsRepository,
    private val legalRepository: LegalRepository
) : ViewModel() {

    private val isOnboardingDone = isOnboardingDone()
    val startDestination = sharedPreferencesRepository.getValue(PREF_KEY_ONBOARDING_DONE, isOnboardingDone)
        .map(::getStartDestination)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = getStartDestination(isOnboardingDone)
        )

    suspend fun isReLoginNeeded(activity: AppCompatActivity): Boolean {
        if (!isOnboardingDone()) return false

        val result = userRepository.refreshToken()
        if (!userRepository.isAuthorizationValid()) {
            LogAndBreadcrumb.wtf(result.exceptionOrNull() ?: InvalidSession, "Token refresh", "Authorization is invalid after token refresh -> Reset app state and restart onboarding")
            userRepository.resetAppData(activity)
            return true
        }

        return false
    }

    fun onAppStart() {
        if (sharedPreferencesRepository.getBoolean(PREF_KEY_ONBOARDING_DONE, false)) {
            viewModelScope.launch {
                paymentMethodKindsRepository.check2FAState()
            }
        }
    }

    fun onOnboardingDone() {
        sharedPreferencesRepository.putValue(PREF_KEY_ONBOARDING_DONE, true)
    }

    private fun isOnboardingDone() = sharedPreferencesRepository.getBoolean(PREF_KEY_ONBOARDING_DONE, false)

    private fun getStartDestination(onboardingDone: Boolean): Graph {
        return when {
            !onboardingDone -> Graph.ONBOARDING
            legalRepository.isUpdateAvailable() -> Graph.LEGAL_UPDATE
            else -> Graph.LIST
        }
    }
}
