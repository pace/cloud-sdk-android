package car.pace.cofu.ui.onboarding.tracking

import androidx.lifecycle.ViewModel
import car.pace.cofu.BuildConfig
import car.pace.cofu.data.SharedPreferencesRepository
import car.pace.cofu.data.SharedPreferencesRepository.Companion.PREF_KEY_TRACKING_ENABLED
import com.google.firebase.Firebase
import com.google.firebase.analytics.analytics
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class TrackingPageViewModel @Inject constructor(
    private val sharedPreferencesRepository: SharedPreferencesRepository
) : ViewModel() {

    fun enableAnalytics() {
        if (BuildConfig.ANALYTICS_ENABLED) {
            Firebase.analytics.setAnalyticsCollectionEnabled(true)
            sharedPreferencesRepository.putValue(PREF_KEY_TRACKING_ENABLED, true)
        } else {
            Firebase.analytics.setAnalyticsCollectionEnabled(false)
        }
    }

    fun disableAnalytics() {
        Firebase.analytics.setAnalyticsCollectionEnabled(false)
        sharedPreferencesRepository.putValue(PREF_KEY_TRACKING_ENABLED, false)
    }
}
