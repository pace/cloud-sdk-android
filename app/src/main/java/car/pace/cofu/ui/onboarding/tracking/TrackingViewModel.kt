package car.pace.cofu.ui.onboarding.tracking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import car.pace.cofu.features.analytics.Analytics
import car.pace.cofu.util.Constants
import car.pace.cofu.util.LogAndBreadcrumb
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class TrackingViewModel @Inject constructor(
    private val analytics: Analytics
) : ViewModel() {

    val trackingEnabled = analytics.userEnabledTracking.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(Constants.STOP_TIMEOUT_MILLIS),
        initialValue = analytics.userEnabledTracking()
    )

    fun enableAnalytics(tag: String) {
        analytics.enableAnalyticsFeature(tag, true)
        if (tag == LogAndBreadcrumb.ONBOARDING) {
            analytics.logAppInstall()
        }
    }

    fun disableAnalytics(tag: String) {
        analytics.enableAnalyticsFeature(tag, false)
    }
}