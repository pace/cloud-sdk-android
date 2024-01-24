package car.pace.cofu.ui.wallet

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import car.pace.cofu.data.SharedPreferencesRepository
import car.pace.cofu.util.AnalyticsUtils
import cloud.pace.sdk.idkit.IDKit
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class WalletViewModel @Inject constructor(
    private val sharedPreferencesRepository: SharedPreferencesRepository
) : ViewModel() {

    suspend fun resetAppData(activity: AppCompatActivity) {
        IDKit.endSession(activity)
        AnalyticsUtils.setAnalyticsEnabled(false)
        sharedPreferencesRepository.clear()
    }
}
