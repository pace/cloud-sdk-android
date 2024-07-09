package cloud.pace.sdk.appkit.model

data class App(
    val name: String? = null,
    val shortName: String? = null,
    val description: String? = null,
    val url: String,
    val iconUrl: String? = null,
    val iconBackgroundColor: String? = null,
    val textBackgroundColor: String? = null,
    val textColor: String? = null,
    val display: String? = null,
    val poiId: String? = null,
    val distance: Int? = null,
    val brandUrl: String? = null,
    val locationAccuracy: Double? = null
)
