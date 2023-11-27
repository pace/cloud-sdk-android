package car.pace.cofu.ui.wallet

import androidx.lifecycle.ViewModel
import car.pace.cofu.data.SharedPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class WalletViewModel @Inject constructor(
    private val sharedPreferencesRepository: SharedPreferencesRepository
) : ViewModel() {

    fun resetAppData() {
        sharedPreferencesRepository.clear()
    }
}
