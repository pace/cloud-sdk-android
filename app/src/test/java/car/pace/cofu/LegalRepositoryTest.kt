package car.pace.cofu

import android.content.Context
import android.content.res.AssetManager
import car.pace.cofu.data.LegalRepository
import car.pace.cofu.data.SharedPreferencesRepository
import car.pace.cofu.features.analytics.Analytics
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

class LegalRepositoryTest {

    private val context = mockk<Context>(relaxed = true)
    private val sharedPreferencesRepository = mockk<SharedPreferencesRepository>(relaxed = true)
    private val analytics = mockk<Analytics>(relaxed = true)
    private lateinit var legalRepository: LegalRepository

    @Test
    fun `legal document update is not available`() {
        init()
        assertEquals(false, legalRepository.isUpdateAvailable())
    }

    @Test
    fun `legal document update is available`() {
        init(newTermsHash = "newTermsHash")
        assertEquals(true, legalRepository.isUpdateAvailable())
    }

    @Test
    fun `no legal document changed`() {
        init()
        assertEquals(emptyList<Consent.Legal>(), legalRepository.getChangedDocuments())
    }

    @Test
    fun `one legal document changed`() {
        init(newPrivacyHash = "newPrivacyHash")
        assertEquals(listOf(Consent.Legal.Privacy), legalRepository.getChangedDocuments())
    }

    @Test
    fun `two legal documents changed`() {
        init(newTermsHash = "newTermsHash", newTrackingHash = "newTrackingHash")
        assertEquals(listOf(Consent.Legal.Terms, Consent.Legal.Tracking), legalRepository.getChangedDocuments())
    }

    @Test
    fun `auto accept terms and privacy updates`() {
        init(acceptedTermsHash = null, acceptedPrivacyHash = null, newTermsHash = "newTermsHash", newPrivacyHash = "newPrivacyHash", isTrackingEnabled = false)
        every { any<InputStream>().hash(any()) } returns "newTermsHash" andThen "newTermsHash" andThen "newPrivacyHash" andThen "newPrivacyHash"
        assertEquals(emptyList<Consent.Legal>(), legalRepository.getChangedDocuments())

        verify {
            sharedPreferencesRepository.putValue(Consent.Legal.Terms.hashPrefKey!!, "newTermsHash")
            sharedPreferencesRepository.putValue(Consent.Legal.Terms.languagePrefKey!!, "en")
            sharedPreferencesRepository.putValue(Consent.Legal.Privacy.hashPrefKey!!, "newPrivacyHash")
            sharedPreferencesRepository.putValue(Consent.Legal.Privacy.languagePrefKey!!, "en")
        }
    }

    @Test
    fun `only ask for tracking update`() {
        init(acceptedTermsHash = null, acceptedPrivacyHash = null, acceptedTrackingHash = null, newTermsHash = "newTermsHash", newPrivacyHash = "newPrivacyHash", newTrackingHash = "newTrackingHash")
        assertEquals(listOf(Consent.Legal.Tracking), legalRepository.getChangedDocuments())
    }

    @Test
    fun `no legal update if new hash is null`() {
        init(newTermsHash = null)
        assertEquals(emptyList<Consent.Legal>(), legalRepository.getChangedDocuments())
    }

    @Test
    fun `no tracking update if user disabled tracking`() {
        init(newTrackingHash = "newTrackingHash", isTrackingEnabled = false)
        assertEquals(emptyList<Consent.Legal>(), legalRepository.getChangedDocuments())
    }

    @Test
    fun `initial language is used`() {
        init()
        assertEquals("en", legalRepository.getLanguage(Consent.Legal.Terms))
        assertEquals("en", legalRepository.getLanguage(Consent.Legal.Privacy))
        assertEquals("en", legalRepository.getLanguage(Consent.Legal.Tracking))
    }

    @Test
    fun `system language is used`() {
        init(initialLanguage = null, systemLanguage = Locale.GERMAN)
        assertEquals("de", legalRepository.getLanguage(Consent.Legal.Terms))
        assertEquals("de", legalRepository.getLanguage(Consent.Legal.Privacy))
        assertEquals("de", legalRepository.getLanguage(Consent.Legal.Tracking))
    }

    @Test
    fun `fallback language is used`() {
        init(initialLanguage = null, systemLanguage = Locale.ITALIAN)
        assertEquals("en", legalRepository.getLanguage(Consent.Legal.Terms))
        assertEquals("en", legalRepository.getLanguage(Consent.Legal.Privacy))
        assertEquals("en", legalRepository.getLanguage(Consent.Legal.Tracking))
    }

    @Test
    fun `hash and language are saved`() {
        init(newPrivacyHash = "newPrivacyHash")
        every { any<InputStream>().hash(any()) } returns "newPrivacyHash"
        legalRepository.saveHash(Consent.Legal.Privacy)

        verify {
            sharedPreferencesRepository.putValue(Consent.Legal.Privacy.hashPrefKey!!, "newPrivacyHash")
            sharedPreferencesRepository.putValue(Consent.Legal.Privacy.languagePrefKey!!, "en")
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

        every { sharedPreferencesRepository.getString(Consent.Legal.Terms.hashPrefKey!!, null) } returns acceptedTermsHash
        every { sharedPreferencesRepository.getString(Consent.Legal.Privacy.hashPrefKey!!, null) } returns acceptedPrivacyHash
        every { sharedPreferencesRepository.getString(Consent.Legal.Tracking.hashPrefKey!!, null) } returns acceptedTrackingHash
        every { sharedPreferencesRepository.getString(Consent.Legal.Terms.languagePrefKey!!, null) } returns initialLanguage
        every { sharedPreferencesRepository.getString(Consent.Legal.Privacy.languagePrefKey!!, null) } returns initialLanguage
        every { sharedPreferencesRepository.getString(Consent.Legal.Tracking.languagePrefKey!!, null) } returns initialLanguage
        every { analytics.isTrackingEnabled() } returns isTrackingEnabled

        val inputStream = mockk<InputStream>(relaxed = true)
        mockkStatic("car.pace.cofu.util.extension.FileKt")
        every { any<Context>().openAsset(any()) } returns inputStream
        every { any<InputStream>().hash(any()) } returns newTermsHash andThen newPrivacyHash andThen newTrackingHash

        val assetManager = mockk<AssetManager>(relaxed = true)
        every { context.assets } returns assetManager
        every { assetManager.list(any()) } returns arrayOf(
            Consent.Legal.Terms.getFullFileName("en"),
            Consent.Legal.Privacy.getFullFileName("en"),
            Consent.Legal.Tracking.getFullFileName("en"),
            Consent.Legal.Terms.getFullFileName("de"),
            Consent.Legal.Privacy.getFullFileName("de"),
            Consent.Legal.Tracking.getFullFileName("de")
        )

        legalRepository = LegalRepository(context, sharedPreferencesRepository, analytics)
    }
}
