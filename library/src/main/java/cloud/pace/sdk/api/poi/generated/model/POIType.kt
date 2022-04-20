/*
 * PLEASE DO NOT EDIT!
 *
 * Generated by SwagGen with Kotlin template.
 * https://github.com/pace/SwagGen
 */

package cloud.pace.sdk.api.poi.generated.model

import com.google.gson.annotations.SerializedName
import com.squareup.moshi.Json

/* POI type this applies to */
enum class POIType(val value: String) {
    @SerializedName("GasStation")
    @Json(name = "GasStation")
    GASSTATION("GasStation"),

    @SerializedName("SpeedCamera")
    @Json(name = "SpeedCamera")
    SPEEDCAMERA("SpeedCamera")
}
