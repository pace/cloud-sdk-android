package car.pace.cofu.ui.more.legal.update

import android.content.Context
import car.pace.cofu.data.SharedPreferencesRepository.Companion.PREF_KEY_PRIVACY_HASH
import car.pace.cofu.data.SharedPreferencesRepository.Companion.PREF_KEY_PRIVACY_LANGUAGE
import car.pace.cofu.data.SharedPreferencesRepository.Companion.PREF_KEY_TERMS_HASH
import car.pace.cofu.data.SharedPreferencesRepository.Companion.PREF_KEY_TERMS_LANGUAGE
import car.pace.cofu.data.SharedPreferencesRepository.Companion.PREF_KEY_TRACKING_HASH
import car.pace.cofu.data.SharedPreferencesRepository.Companion.PREF_KEY_TRACKING_LANGUAGE
import car.pace.cofu.util.extension.hash
import car.pace.cofu.util.extension.openAsset

enum class LegalDocument(
    val fileName: String,
    val hashPrefKey: String?,
    val languagePrefKey: String?
) {
    TERMS(fileName = "usage_terms", hashPrefKey = PREF_KEY_TERMS_HASH, languagePrefKey = PREF_KEY_TERMS_LANGUAGE),
    PRIVACY(fileName = "privacy_statement", hashPrefKey = PREF_KEY_PRIVACY_HASH, languagePrefKey = PREF_KEY_PRIVACY_LANGUAGE),
    TRACKING(fileName = "usage_analysis", hashPrefKey = PREF_KEY_TRACKING_HASH, languagePrefKey = PREF_KEY_TRACKING_LANGUAGE),
    IMPRINT(fileName = "imprint", hashPrefKey = null, languagePrefKey = null);

    fun getUrl(language: String): String {
        val fullFileName = getFullFileName(language)
        return "$FILE_SCHEME:///$ASSET_DIR/$fullFileName"
    }

    fun getFullFileName(language: String): String {
        return "${fileName}_$language.$EXTENSION"
    }

    fun getFileHash(context: Context, language: String): String? {
        val fullFileName = getFullFileName(language)
        return context.openAsset(fullFileName)?.use {
            it.hash()
        }
    }

    companion object {
        const val FILE_SCHEME = "file"
        const val ASSET_DIR = "android_asset"
        const val EXTENSION = "html"
    }
}
