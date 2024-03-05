package car.pace.cofu.data

import android.content.Context
import car.pace.cofu.features.analytics.Analytics
import car.pace.cofu.ui.more.legal.update.LegalDocument
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

    private val documents = listOf(LegalDocument.TERMS, LegalDocument.PRIVACY, LegalDocument.TRACKING)

    fun isUpdateAvailable(): Boolean {
        return getAcceptedHashes().any {
            hasDocumentChanged(it.key, it.value)
        }
    }

    fun getChangedDocuments(): List<LegalDocument> {
        return getAcceptedHashes().mapNotNull {
            it.key.takeIf { _ ->
                hasDocumentChanged(it.key, it.value)
            }
        }
    }

    fun saveHash(legalDocument: LegalDocument) {
        val language = getLanguage(legalDocument)
        val hash = legalDocument.getFileHash(context, language)
        legalDocument.hashPrefKey?.let { sharedPreferencesRepository.putValue(it, hash) }
        legalDocument.languagePrefKey?.let { sharedPreferencesRepository.putValue(it, language) }
    }

    fun getLanguage(legalDocument: LegalDocument): String {
        val language = legalDocument.languagePrefKey?.let { sharedPreferencesRepository.getString(it, null) } ?: Locale.getDefault().language
        val fullFileName = legalDocument.getFullFileName(language)
        val assets = context.assets.list("")
        return if (assets?.contains(fullFileName) == true) language else "en"
    }

    private fun getAcceptedHashes(): Map<LegalDocument, String?> {
        return documents.associateWith(::getAcceptedHash)
    }

    private fun getAcceptedHash(legalDocument: LegalDocument): String? {
        return legalDocument.hashPrefKey?.let { sharedPreferencesRepository.getString(it, null) }
    }

    private fun getNewHash(legalDocument: LegalDocument): String? {
        val language = getLanguage(legalDocument)
        return legalDocument.getFileHash(context, language)
    }

    private fun hasDocumentChanged(legalDocument: LegalDocument, acceptedHash: String?): Boolean {
        if (legalDocument == LegalDocument.TRACKING && !analytics.isTrackingEnabled()) return false

        // True means that we save the new hash but do not show the update screen.
        // This should be done for existing users for the updated terms or privacy policy.
        val autoAccept = legalDocument != LegalDocument.TRACKING
        val newHash = getNewHash(legalDocument)

        return if (acceptedHash != null || !autoAccept) {
            // Ask to accept changed document or existing user sees tracking consent the first time
            newHash != null && acceptedHash != newHash
        } else {
            // Existing users only: Auto accept terms and privacy changes once
            saveHash(legalDocument)
            false
        }
    }
}
