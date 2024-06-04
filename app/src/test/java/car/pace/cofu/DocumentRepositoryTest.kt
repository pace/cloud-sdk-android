package car.pace.cofu

import android.content.Context
import android.content.res.AssetManager
import car.pace.cofu.data.DocumentRepository
import car.pace.cofu.data.SharedPreferencesRepository
import car.pace.cofu.data.analytics.Analytics
import car.pace.cofu.ui.consent.Consent
import car.pace.cofu.util.extension.hash
import car.pace.cofu.util.extension.openAsset
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import java.io.InputStream
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Test

class DocumentRepositoryTest {

    private val context = mockk<Context>(relaxed = true)
    private val sharedPreferencesRepository = mockk<SharedPreferencesRepository>(relaxed = true)
    private val analytics = mockk<Analytics>(relaxed = true)
    private lateinit var documentRepository: DocumentRepository

    @Test
    fun `legal document update is not available`() {
        init()
        assertEquals(false, documentRepository.isUpdateAvailable())
    }

    @Test
    fun `legal document update is available`() {
        init(newTermsHash = "newTermsHash")
        assertEquals(true, documentRepository.isUpdateAvailable())
    }

    @Test
    fun `no legal document changed`() {
        init()
        assertEquals(emptyList<Consent.Document>(), documentRepository.getChangedDocuments())
    }

    @Test
    fun `one legal document changed`() {
        init(newPrivacyHash = "newPrivacyHash")
        assertEquals(listOf(Consent.Document.Privacy), documentRepository.getChangedDocuments())
    }

    @Test
    fun `two legal documents changed`() {
        init(newTermsHash = "newTermsHash", newTrackingHash = "newTrackingHash")
        assertEquals(listOf(Consent.Document.Terms, Consent.Document.Tracking), documentRepository.getChangedDocuments())
    }

    @Test
    fun `auto accept terms and privacy updates`() {
        init(acceptedTermsHash = null, acceptedPrivacyHash = null, newTermsHash = "newTermsHash", newPrivacyHash = "newPrivacyHash", isTrackingEnabled = false)
        every { any<InputStream>().hash(any()) } returns "newTermsHash" andThen "newTermsHash" andThen "newPrivacyHash" andThen "newPrivacyHash"
        assertEquals(emptyList<Consent.Document>(), documentRepository.getChangedDocuments())

        verify {
            sharedPreferencesRepository.putValue(Consent.Document.Terms.hashPrefKey!!, "newTermsHash")
            sharedPreferencesRepository.putValue(Consent.Document.Terms.languagePrefKey!!, "en")
            sharedPreferencesRepository.putValue(Consent.Document.Privacy.hashPrefKey!!, "newPrivacyHash")
            sharedPreferencesRepository.putValue(Consent.Document.Privacy.languagePrefKey!!, "en")
        }
    }

    @Test
    fun `only ask for tracking update`() {
        init(acceptedTermsHash = null, acceptedPrivacyHash = null, acceptedTrackingHash = null, newTermsHash = "newTermsHash", newPrivacyHash = "newPrivacyHash", newTrackingHash = "newTrackingHash")
        assertEquals(listOf(Consent.Document.Tracking), documentRepository.getChangedDocuments())
    }

    @Test
    fun `no legal update if new hash is null`() {
        init(newTermsHash = null)
        assertEquals(emptyList<Consent.Document>(), documentRepository.getChangedDocuments())
    }

    @Test
    fun `no tracking update if user disabled tracking`() {
        init(newTrackingHash = "newTrackingHash", isTrackingEnabled = false)
        assertEquals(emptyList<Consent.Document>(), documentRepository.getChangedDocuments())
    }

    @Test
    fun `initial language is used`() {
        init()
        assertEquals("en", documentRepository.getLanguage(Consent.Document.Terms))
        assertEquals("en", documentRepository.getLanguage(Consent.Document.Privacy))
        assertEquals("en", documentRepository.getLanguage(Consent.Document.Tracking))
    }

    @Test
    fun `system language is used`() {
        init(initialLanguage = null, systemLanguage = Locale.GERMAN)
        assertEquals("de", documentRepository.getLanguage(Consent.Document.Terms))
        assertEquals("de", documentRepository.getLanguage(Consent.Document.Privacy))
        assertEquals("de", documentRepository.getLanguage(Consent.Document.Tracking))
    }

    @Test
    fun `fallback language is used`() {
        init(initialLanguage = null, systemLanguage = Locale.ITALIAN)
        assertEquals("en", documentRepository.getLanguage(Consent.Document.Terms))
        assertEquals("en", documentRepository.getLanguage(Consent.Document.Privacy))
        assertEquals("en", documentRepository.getLanguage(Consent.Document.Tracking))
    }

    @Test
    fun `hash and language are saved`() {
        init(newPrivacyHash = "newPrivacyHash")
        every { any<InputStream>().hash(any()) } returns "newPrivacyHash"
        documentRepository.saveHash(Consent.Document.Privacy)

        verify {
            sharedPreferencesRepository.putValue(Consent.Document.Privacy.hashPrefKey!!, "newPrivacyHash")
            sharedPreferencesRepository.putValue(Consent.Document.Privacy.languagePrefKey!!, "en")
        }
    }

    private fun init(
        acceptedTermsHash: String? = "acceptedTermsHash",
        acceptedPrivacyHash: String? = "acceptedPrivacyHash",
        acceptedTrackingHash: String? = "acceptedTrackingHash",
        newTermsHash: String? = acceptedTermsHash,
        newPrivacyHash: String? = acceptedPrivacyHash,
        newTrackingHash: String? = acceptedTrackingHash,
        initialLanguage: String? = "en",
        systemLanguage: Locale = Locale.GERMAN,
        isTrackingEnabled: Boolean = true
    ) {
        Locale.setDefault(systemLanguage)

        every { sharedPreferencesRepository.getString(Consent.Document.Terms.hashPrefKey!!, null) } returns acceptedTermsHash
        every { sharedPreferencesRepository.getString(Consent.Document.Privacy.hashPrefKey!!, null) } returns acceptedPrivacyHash
        every { sharedPreferencesRepository.getString(Consent.Document.Tracking.hashPrefKey!!, null) } returns acceptedTrackingHash
        every { sharedPreferencesRepository.getString(Consent.Document.Terms.languagePrefKey!!, null) } returns initialLanguage
        every { sharedPreferencesRepository.getString(Consent.Document.Privacy.languagePrefKey!!, null) } returns initialLanguage
        every { sharedPreferencesRepository.getString(Consent.Document.Tracking.languagePrefKey!!, null) } returns initialLanguage
        every { analytics.isTrackingEnabled() } returns isTrackingEnabled

        val inputStream = mockk<InputStream>(relaxed = true)
        mockkStatic("car.pace.cofu.util.extension.FileKt")
        every { any<Context>().openAsset(any()) } returns inputStream
        every { any<InputStream>().hash(any()) } returns newTermsHash andThen newPrivacyHash andThen newTrackingHash

        val assetManager = mockk<AssetManager>(relaxed = true)
        every { context.assets } returns assetManager
        every { assetManager.list(any()) } returns arrayOf(
            Consent.Document.Terms.getFullFileName("en"),
            Consent.Document.Privacy.getFullFileName("en"),
            Consent.Document.Tracking.getFullFileName("en"),
            Consent.Document.Terms.getFullFileName("de"),
            Consent.Document.Privacy.getFullFileName("de"),
            Consent.Document.Tracking.getFullFileName("de")
        )

        documentRepository = DocumentRepository(context, sharedPreferencesRepository, analytics)
    }
}
