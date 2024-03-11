package car.pace.cofu.ui.consent

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import car.pace.cofu.data.LegalRepository
import car.pace.cofu.data.PermissionRepository
import car.pace.cofu.data.SharedPreferencesRepository
import car.pace.cofu.data.SharedPreferencesRepository.Companion.PREF_KEY_NOTIFICATION_PERMISSION_REQUESTED
import car.pace.cofu.features.analytics.Analytics
import car.pace.cofu.util.LogAndBreadcrumb
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ConsentViewModel @Inject constructor(
    private val analytics: Analytics,
    private val legalRepository: LegalRepository,
    private val permissionRepository: PermissionRepository,
    private val sharedPreferencesRepository: SharedPreferencesRepository
) : ViewModel() {

    var pageIndex by mutableIntStateOf(0)

    private val pages = buildList {
        addAll(legalRepository.getChangedDocuments())

        if (permissionRepository.canRequestNotificationPermission()) {
            add(Consent.Notification)
        }
    }

    fun getCountOfPages() = pages.size

    fun getPage(index: Int) = pages.getOrNull(index)

    fun acceptTerms() {
        legalRepository.saveHash(Consent.Legal.Terms)
        pageIndex++
    }

    fun acceptPrivacy() {
        legalRepository.saveHash(Consent.Legal.Privacy)
        pageIndex++
    }

    fun acceptTracking() {
        analytics.enableAnalyticsFeature(LogAndBreadcrumb.CONSENT, true)
        legalRepository.saveHash(Consent.Legal.Tracking)
        pageIndex++
    }

    fun declineTracking() {
        analytics.enableAnalyticsFeature(LogAndBreadcrumb.CONSENT, false)
        pageIndex++
    }

    fun nextPage() {
        pageIndex++
    }

    fun notificationPermissionRequested() {
        sharedPreferencesRepository.putValue(PREF_KEY_NOTIFICATION_PERMISSION_REQUESTED, true)
    }
}
