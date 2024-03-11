package car.pace.cofu.ui.onboarding.permission.notification

import androidx.lifecycle.ViewModel
import car.pace.cofu.data.SharedPreferencesRepository
import car.pace.cofu.data.SharedPreferencesRepository.Companion.PREF_KEY_NOTIFICATION_PERMISSION_REQUESTED
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class NotificationPermissionViewModel @Inject constructor(
    private val sharedPreferencesRepository: SharedPreferencesRepository
) : ViewModel() {

    fun notificationPermissionRequested() {
        sharedPreferencesRepository.putValue(PREF_KEY_NOTIFICATION_PERMISSION_REQUESTED, true)
    }
}
