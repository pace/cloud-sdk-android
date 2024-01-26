package car.pace.cofu.ui.wallet

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import car.pace.cofu.data.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class WalletViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    suspend fun resetAppData(activity: AppCompatActivity) {
        userRepository.resetAppData(activity)
    }
}
