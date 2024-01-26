package car.pace.cofu.ui.app

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import car.pace.cofu.data.SharedPreferencesRepository
import car.pace.cofu.data.SharedPreferencesRepository.Companion.PREF_KEY_FUEL_TYPE
import car.pace.cofu.data.SharedPreferencesRepository.Companion.PREF_KEY_ONBOARDING_DONE
import car.pace.cofu.data.UserRepository
import car.pace.cofu.ui.wallet.fueltype.FuelTypeGroup
import car.pace.cofu.util.Constants.STOP_TIMEOUT_MILLIS
import car.pace.cofu.util.LogAndBreadcrumb
import cloud.pace.sdk.idkit.model.InvalidSession
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class AppContentViewModel @Inject constructor(
    private val sharedPreferencesRepository: SharedPreferencesRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val initialValue = isOnboardingDone()
    val onboardingDone = sharedPreferencesRepository
        .getValue(PREF_KEY_ONBOARDING_DONE, initialValue)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = initialValue
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

    fun onOnboardingDone(fuelTypeGroup: FuelTypeGroup) {
        sharedPreferencesRepository.putValue(PREF_KEY_FUEL_TYPE, fuelTypeGroup.prefFuelType.ordinal)
        sharedPreferencesRepository.putValue(PREF_KEY_ONBOARDING_DONE, true)
    }

    private fun isOnboardingDone() = sharedPreferencesRepository.getBoolean(PREF_KEY_ONBOARDING_DONE, false)
}
