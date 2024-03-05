package car.pace.cofu.ui.onboarding.legal

import androidx.lifecycle.ViewModel
import car.pace.cofu.data.LegalRepository
import car.pace.cofu.ui.more.legal.update.LegalDocument
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class LegalViewModel @Inject constructor(
    private val legalRepository: LegalRepository
) : ViewModel() {

    fun acceptTermsAndPrivacy() {
        legalRepository.saveHash(LegalDocument.TERMS)
        legalRepository.saveHash(LegalDocument.PRIVACY)
    }
}
