package car.pace.cofu.data

import android.content.Context
import car.pace.cofu.data.analytics.Analytics
import car.pace.cofu.ui.consent.Consent
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DocumentRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val sharedPreferencesRepository: SharedPreferencesRepository,
    private val analytics: Analytics
) {

    private val documents = listOf(Consent.Document.Terms, Consent.Document.Privacy, Consent.Document.Tracking)

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

    fun saveHash(document: Consent.Document) {
        val language = getLanguage(document)
        val hash = document.getFileHash(context, language)
        document.hashPrefKey?.let { sharedPreferencesRepository.putValue(it, hash) }
        document.languagePrefKey?.let { sharedPreferencesRepository.putValue(it, language) }
    }

    fun getLanguage(document: Consent.Document): String {
        val language = document.languagePrefKey?.let { sharedPreferencesRepository.getString(it, null) } ?: Locale.getDefault().language
        val fullFileName = document.getFullFileName(language)
        val assets = context.assets.list("")
        return if (assets?.contains(fullFileName) == true) language else "en"
    }

    private fun getAcceptedHashes(): Map<Consent.Document, String?> {
        return documents.associateWith(::getAcceptedHash)
    }

    private fun getAcceptedHash(document: Consent.Document): String? {
        return document.hashPrefKey?.let { sharedPreferencesRepository.getString(it, null) }
    }

    private fun getNewHash(document: Consent.Document): String? {
        val language = getLanguage(document)
        return document.getFileHash(context, language)
    }

    private fun hasDocumentChanged(document: Consent.Document, acceptedHash: String?): Boolean {
        if (document == Consent.Document.Tracking && !analytics.isTrackingEnabled()) return false

        // True means that we save the new hash but do not show the update screen.
        // This should be done for existing users for the updated terms or privacy policy.
        val autoAccept = document != Consent.Document.Tracking
        val newHash = getNewHash(document)

        return if (acceptedHash != null || !autoAccept) {
            // Ask to accept changed document or existing user sees tracking consent the first time
            newHash != null && acceptedHash != newHash
        } else {
            // Existing users only: Auto accept terms and privacy changes once
            saveHash(document)
            false
        }
    }
}
