package car.pace.cofu.util

import android.os.LocaleList
import cloud.pace.sdk.api.utils.RequestUtils.ACCEPT_LANGUAGE_HEADER

object RequestUtils {

    fun getHeaders(): Map<String, String> {
        val acceptLanguage = LocaleList.getDefault().toLanguageTags()
        return mapOf(ACCEPT_LANGUAGE_HEADER to acceptLanguage)
    }
}
