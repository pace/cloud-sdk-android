package cloud.pace.sdk.poikit.utils

import android.os.Build
import cloud.pace.sdk.BuildConfig
import cloud.pace.sdk.utils.DeviceUtils

object ApiUtils {
    const val USER_AGENT_HEADER = "User-Agent"
    const val ACCEPT_HEADER = "Accept"
    const val CONTENT_TYPE_HEADER = "Content-Type"
    const val API_KEY = "API-Key"

    fun getUserAgent(versionName: String = BuildConfig.VERSION_NAME, versionCode: Int = BuildConfig.VERSION_CODE): String {
        return "POIKit/$versionName.$versionCode (${DeviceUtils.getDeviceName()}; Android/${Build.VERSION.RELEASE})"
    }
}
