package car.pace.cofu.ui.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import car.pace.cofu.data.SharedPreferencesRepository
import car.pace.cofu.data.SharedPreferencesRepository.Companion.PREF_KEY_FUEL_TYPE
import car.pace.cofu.data.SharedPreferencesRepository.Companion.PREF_KEY_ONBOARDING_DONE
import car.pace.cofu.ui.wallet.fueltype.FuelTypeGroup
import car.pace.cofu.util.Constants.STOP_TIMEOUT_MILLIS
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class AppNavHostViewModel @Inject constructor(
    private val sharedPreferencesRepository: SharedPreferencesRepository
) : ViewModel() {

    private val initialValue = sharedPreferencesRepository.getBoolean(PREF_KEY_ONBOARDING_DONE, false)
    val onboardingDone = sharedPreferencesRepository
        .getValue(PREF_KEY_ONBOARDING_DONE, initialValue)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = initialValue
        )

    fun onboardingDone(fuelTypeGroup: FuelTypeGroup) {
        sharedPreferencesRepository.putValue(PREF_KEY_FUEL_TYPE, fuelTypeGroup.prefFuelType.ordinal)
        sharedPreferencesRepository.putValue(PREF_KEY_ONBOARDING_DONE, true)
    }
}
