package cloud.pace.sdk.poikit.poi.download

import android.os.Parcelable
import cloud.pace.sdk.poikit.poi.LocationPoint
import cloud.pace.sdk.poikit.utils.GeoMathUtils
import cloud.pace.sdk.poikit.utils.POIKitConfig
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize

/**
 * Simple tile information
 * Can be extended as RichTileInformation for Road Pinning
 */
@Parcelize
open class TileInformation(val zoomLevel: Int, val x: Int, val y: Int) : Parcelable {

    @IgnoredOnParcel
    val id: String by lazy {
        zoomLevel.toString() + "_" + x + "_" + y
    }

    @IgnoredOnParcel
    val coordinate: LocationPoint by lazy {
        LocationPoint(
            GeoMathUtils.tiley2lat(y.toDouble(), zoomLevel),
            GeoMathUtils.tilex2long(x.toDouble(), zoomLevel)
        )
    }

    @IgnoredOnParcel
    val centerCoordinate: LocationPoint by lazy {
        LocationPoint(
            GeoMathUtils.tiley2lat(y.toDouble() + 0.5, zoomLevel),
            GeoMathUtils.tilex2long(x.toDouble() + 0.5, zoomLevel)
        )
    }

    override fun equals(other: Any?): Boolean {
        return if (other is TileInformation) {
            val tileInformation = other as TileInformation?
            tileInformation!!.zoomLevel == this.zoomLevel && tileInformation.x == this.x && tileInformation.y == this.y
        } else {
            false
        }
    }

    /**
     * Works only if zoom level is smaller or equal to 16
     */
    fun convertToStandardZoomLevel(): TileInformation {
        val b = if (zoomLevel < POIKitConfig.ZOOMLEVEL) {
            1 shl (POIKitConfig.ZOOMLEVEL - zoomLevel - 1)
        } else {
            0
        }
        return TileInformation(
            POIKitConfig.ZOOMLEVEL,
            (x shl (POIKitConfig.ZOOMLEVEL - zoomLevel)) + b,
            (y shl (POIKitConfig.ZOOMLEVEL - zoomLevel)) + b
        )
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}
