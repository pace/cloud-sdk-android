package car.pace.cofu.ui.onboarding.tracking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import car.pace.cofu.BuildConfig
import car.pace.cofu.data.SharedPreferencesRepository
import car.pace.cofu.data.SharedPreferencesRepository.Companion.PREF_KEY_TRACKING_ENABLED
import car.pace.cofu.util.Constants.STOP_TIMEOUT_MILLIS
import car.pace.cofu.util.LogAndBreadcrumb
import com.google.firebase.Firebase
import com.google.firebase.analytics.analytics
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class TrackingViewModel @Inject constructor(
    private val sharedPreferencesRepository: SharedPreferencesRepository
) : ViewModel() {

    private val initialValue = sharedPreferencesRepository.getBoolean(PREF_KEY_TRACKING_ENABLED, false)
    val trackingEnabled = sharedPreferencesRepository
        .getValue(PREF_KEY_TRACKING_ENABLED, initialValue)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = initialValue
        )

    fun enableAnalytics(tag: String) {
        Firebase.analytics.setAnalyticsCollectionEnabled(BuildConfig.ANALYTICS_ENABLED)
        sharedPreferencesRepository.putValue(PREF_KEY_TRACKING_ENABLED, true)
        LogAndBreadcrumb.i(tag, "Analytics enabled")
    }

    fun disableAnalytics(tag: String) {
        Firebase.analytics.setAnalyticsCollectionEnabled(false)
        sharedPreferencesRepository.putValue(PREF_KEY_TRACKING_ENABLED, false)
        LogAndBreadcrumb.i(tag, "Analytics disabled")
    }
}
