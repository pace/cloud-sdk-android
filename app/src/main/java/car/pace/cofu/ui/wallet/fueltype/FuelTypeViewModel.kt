package car.pace.cofu.ui.wallet.fueltype

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import car.pace.cofu.data.SharedPreferencesRepository
import car.pace.cofu.data.SharedPreferencesRepository.Companion.PREF_KEY_FUEL_TYPE
import car.pace.cofu.util.Constants.STOP_TIMEOUT_MILLIS
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class FuelTypeViewModel @Inject constructor(
    private val sharedPreferencesRepository: SharedPreferencesRepository
) : ViewModel() {

    private val initialValue = sharedPreferencesRepository.getInt(PREF_KEY_FUEL_TYPE, -1)
    val fuelTypeGroup = sharedPreferencesRepository
        .getValue(PREF_KEY_FUEL_TYPE, initialValue)
        .map { it.toFuelTypeGroup() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = initialValue.toFuelTypeGroup()
        )

    fun setFuelTypeGroup(fuelTypeGroup: FuelTypeGroup) {
        sharedPreferencesRepository.putValue(PREF_KEY_FUEL_TYPE, fuelTypeGroup.prefFuelType.ordinal)
    }
}
