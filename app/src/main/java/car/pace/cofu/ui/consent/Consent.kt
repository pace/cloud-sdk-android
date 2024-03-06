package car.pace.cofu.ui.consent

import android.content.Context
import car.pace.cofu.data.SharedPreferencesRepository.Companion.PREF_KEY_PRIVACY_HASH
import car.pace.cofu.data.SharedPreferencesRepository.Companion.PREF_KEY_PRIVACY_LANGUAGE
import car.pace.cofu.data.SharedPreferencesRepository.Companion.PREF_KEY_TERMS_HASH
import car.pace.cofu.data.SharedPreferencesRepository.Companion.PREF_KEY_TERMS_LANGUAGE
import car.pace.cofu.data.SharedPreferencesRepository.Companion.PREF_KEY_TRACKING_HASH
import car.pace.cofu.data.SharedPreferencesRepository.Companion.PREF_KEY_TRACKING_LANGUAGE
import car.pace.cofu.util.extension.hash
import car.pace.cofu.util.extension.openAsset

sealed class Consent {

    sealed class Legal(
        val fileName: String,
        val hashPrefKey: String?,
        val languagePrefKey: String?
    ) : Consent() {

        data object Terms : Legal(fileName = "usage_terms", hashPrefKey = PREF_KEY_TERMS_HASH, languagePrefKey = PREF_KEY_TERMS_LANGUAGE)
        data object Privacy : Legal(fileName = "privacy_statement", hashPrefKey = PREF_KEY_PRIVACY_HASH, languagePrefKey = PREF_KEY_PRIVACY_LANGUAGE)
        data object Tracking : Legal(fileName = "usage_analysis", hashPrefKey = PREF_KEY_TRACKING_HASH, languagePrefKey = PREF_KEY_TRACKING_LANGUAGE)
        data object Imprint : Legal(fileName = "imprint", hashPrefKey = null, languagePrefKey = null)

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
            private const val FILE_SCHEME = "file"
            private const val ASSET_DIR = "android_asset"
            private const val EXTENSION = "html"
        }
    }

    data object Notification : Consent()
}
