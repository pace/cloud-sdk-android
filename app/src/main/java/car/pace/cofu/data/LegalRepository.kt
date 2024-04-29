package car.pace.cofu.data

import android.content.Context
import car.pace.cofu.data.analytics.Analytics
import car.pace.cofu.ui.consent.Consent
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LegalRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val sharedPreferencesRepository: SharedPreferencesRepository,
    private val analytics: Analytics
) {

    private val legalConsents = listOf(Consent.Legal.Terms, Consent.Legal.Privacy, Consent.Legal.Tracking)

    fun isUpdateAvailable(): Boolean {
        return getAcceptedHashes().any {
            hasDocumentChanged(it.key, it.value)
        }
    }

    fun getChangedDocuments(): List<Consent> {
        return getAcceptedHashes().mapNotNull {
            it.key.takeIf { _ ->
                hasDocumentChanged(it.key, it.value)
            }
        }
    }

    fun saveHash(legalConsent: Consent.Legal) {
        val language = getLanguage(legalConsent)
        val hash = legalConsent.getFileHash(context, language)
        legalConsent.hashPrefKey?.let { sharedPreferencesRepository.putValue(it, hash) }
        legalConsent.languagePrefKey?.let { sharedPreferencesRepository.putValue(it, language) }
    }

    fun getLanguage(legalConsent: Consent.Legal): String {
        val language = legalConsent.languagePrefKey?.let { sharedPreferencesRepository.getString(it, null) } ?: Locale.getDefault().language
        val fullFileName = legalConsent.getFullFileName(language)
        val assets = context.assets.list("")
        return if (assets?.contains(fullFileName) == true) language else "en"
    }

    private fun getAcceptedHashes(): Map<Consent.Legal, String?> {
        return legalConsents.associateWith(::getAcceptedHash)
    }

    private fun getAcceptedHash(legalConsent: Consent.Legal): String? {
        return legalConsent.hashPrefKey?.let { sharedPreferencesRepository.getString(it, null) }
    }

    private fun getNewHash(legalConsent: Consent.Legal): String? {
        val language = getLanguage(legalConsent)
        return legalConsent.getFileHash(context, language)
    }

    private fun hasDocumentChanged(legalConsent: Consent.Legal, acceptedHash: String?): Boolean {
        if (legalConsent == Consent.Legal.Tracking && !analytics.isTrackingEnabled()) return false

        // True means that we save the new hash but do not show the update screen.
        // This should be done for existing users for the updated terms or privacy policy.
        val autoAccept = legalConsent != Consent.Legal.Tracking
        val newHash = getNewHash(legalConsent)

        return if (acceptedHash != null || !autoAccept) {
            // Ask to accept changed document or existing user sees tracking consent the first time
            newHash != null && acceptedHash != newHash
        } else {
            // Existing users only: Auto accept terms and privacy changes once
            saveHash(legalConsent)
            false
        }
    }
}
