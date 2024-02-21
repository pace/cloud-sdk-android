package car.pace.cofu.ui.more.legal.update

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import car.pace.cofu.data.LegalRepository
import car.pace.cofu.features.analytics.Analytics
import car.pace.cofu.util.LogAndBreadcrumb
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class LegalUpdateViewModel @Inject constructor(
    private val analytics: Analytics,
    private val legalRepository: LegalRepository
) : ViewModel() {

    var pageIndex by mutableIntStateOf(0)

    private val pages = legalRepository.getChangedDocuments()

    fun getCountOfPages() = pages.size

    fun getPage(index: Int) = pages.getOrNull(index)

    fun acceptTerms() {
        legalRepository.saveHash(LegalDocument.TERMS)
        pageIndex++
    }

    fun acceptPrivacy() {
        legalRepository.saveHash(LegalDocument.PRIVACY)
        pageIndex++
    }

    fun acceptTracking() {
        analytics.enableAnalyticsFeature(LogAndBreadcrumb.LEGAL_UPDATE, true)
        legalRepository.saveHash(LegalDocument.TRACKING)
        pageIndex++
    }

    fun declineTracking() {
        analytics.enableAnalyticsFeature(LogAndBreadcrumb.LEGAL_UPDATE, false)
        pageIndex++
    }
}
