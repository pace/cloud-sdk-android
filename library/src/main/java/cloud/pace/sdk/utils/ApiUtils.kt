package cloud.pace.sdk.utils

import android.os.Build
import cloud.pace.sdk.BuildConfig
import cloud.pace.sdk.utils.Constants.TAG

object ApiUtils {

    const val USER_AGENT_HEADER = "User-Agent"
    const val ACCEPT_HEADER = "Accept"
    const val CONTENT_TYPE_HEADER = "Content-Type"
    const val API_KEY_HEADER = "API-Key"
    const val AUTHORIZATION_HEADER = "Authorization"

    fun getUserAgent(versionName: String = BuildConfig.VERSION_NAME, versionCode: Int = BuildConfig.VERSION_CODE): String {
        return "$TAG/$versionName.$versionCode (${DeviceUtils.getDeviceName()}; Android/${Build.VERSION.RELEASE})"
    }
}
