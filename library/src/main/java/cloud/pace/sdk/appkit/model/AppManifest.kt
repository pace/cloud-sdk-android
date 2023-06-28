package cloud.pace.sdk.appkit.model

import com.squareup.moshi.Json

data class AppManifest(
    @Json(name = "short_name")
    val shortName: String? = null,
    val name: String? = null,
    val description: String? = null,
    val icons: List<AppIcon>? = null,
    @Json(name = "start_url")
    val startUrl: String? = null,
    val display: String? = null,
    @Json(name = "pace_pwa_sdk_start_url")
    val sdkStartUrl: String? = null,
    @Json(name = "theme_color")
    val themeColor: String? = null,
    @Json(name = "text_color")
    val textColor: String? = null,
    @Json(name = "background_color")
    val backgroundColor: String? = null
)

data class AppIcon(
    val src: String? = null,
    val sizes: String? = null,
    val type: String? = null
)
