package car.pace.cofu.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import car.pace.cofu.data.SharedPreferencesRepository
import car.pace.cofu.data.SharedPreferencesRepository.Companion.PREF_KEY_ONBOARDING_DONE
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class AppContentViewModel @Inject constructor(
    private val sharedPreferencesRepository: SharedPreferencesRepository
) : ViewModel() {

    private val initialValue = sharedPreferencesRepository.getBoolean(PREF_KEY_ONBOARDING_DONE, false)
    val onboardingDone = sharedPreferencesRepository
        .getValue(PREF_KEY_ONBOARDING_DONE, initialValue)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = initialValue
        )

    fun onboardingDone() {
        sharedPreferencesRepository.putValue(PREF_KEY_ONBOARDING_DONE, true)
    }
}
