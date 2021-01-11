package cloud.pace.sdk.appkit.model

import com.google.gson.annotations.SerializedName

class AppManifest(
    var name: String,
    @SerializedName("short_name")
    var shortName: String,
    var description: String,
    @SerializedName("start_url")
    var startUrl: String,
    var display: String,
    @SerializedName("background_color")
    var backgroundColor: String,
    var icons: Array<AppIcons>,
    @SerializedName("pace_pwa_sdk_start_url")
    var sdkStartUrl: String,
    @SerializedName("theme_color")
    var themeColor: String,
    @SerializedName("text_color")
    var textColor: String
) {
    data class AppIcons(
        var src: String,
        var sizes: String,
        var type: String
    )
}
