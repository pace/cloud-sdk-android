package car.pace.cofu.ui.onboarding.legal

import androidx.lifecycle.ViewModel
import car.pace.cofu.data.DocumentRepository
import car.pace.cofu.ui.consent.Consent
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class LegalViewModel @Inject constructor(
    private val documentRepository: DocumentRepository
) : ViewModel() {

    fun acceptTermsAndPrivacy() {
        documentRepository.saveHash(Consent.Document.Terms)
        documentRepository.saveHash(Consent.Document.Privacy)
    }
}
