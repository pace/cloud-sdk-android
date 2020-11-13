package cloud.pace.sdk.poikit.poi

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * Point representing a coordinate (not necessarily lat/lon).
 */

@Parcelize
data class Point(var coordX: Double, var coordY: Double) : Parcelable {

    override fun equals(other: Any?): Boolean {
        return if (other is Point) {
            val epsilon = 0.0001
            Math.abs(other.coordX - coordX) < epsilon && Math.abs(other.coordY - coordY) < epsilon
        } else {
            false
        }
    }

    override fun hashCode(): Int {
        var result = coordX.hashCode()
        result = 31 * result + coordY.hashCode()
        return result
    }
}
