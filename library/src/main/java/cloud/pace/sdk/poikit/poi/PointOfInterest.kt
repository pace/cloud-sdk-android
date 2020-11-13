package cloud.pace.sdk.poikit.poi

import androidx.room.Embedded
import androidx.room.Ignore
import androidx.room.PrimaryKey
import cloud.pace.sdk.poikit.utils.OSMKeys.OSM_TEMPORARY
import java.util.*

/**
 * Superclass of all Point of Interests.
 */
abstract class PointOfInterest(
    @PrimaryKey(autoGenerate = false)
    var id: String,
    var geometry: ArrayList<Geometry.CommandGeo>
) {
    abstract val poiLayer: POILayer

    @Ignore
    var values: HashMap<String, String>? = null
    var temporary: Boolean? = null

    @Embedded
    var address: Address? = null
    var updatedAt: Date? = null

    var latitude: Double? = null
    var longitude: Double? = null

    val center: LocationPoint?
        get() {
            val latitude = latitude ?: return null
            val longitude = longitude ?: return null
            return LocationPoint(latitude, longitude)
        }

    init {
        if (geometry.isNotEmpty()) {
            var lat = 0.0
            var lon = 0.0

            geometry.forEach {
                lat += it.locationPoint.lat
                lon += it.locationPoint.lon
            }

            lat /= geometry.size
            lon /= geometry.size

            this.latitude = lat
            this.longitude = lon
        }
    }

    open fun init(values: HashMap<String, String>) {
        this.values = values
        temporary = values[OSM_TEMPORARY]?.toInt() == 1
        address = values.toAddress()
    }

    override fun equals(other: Any?): Boolean {
        return if (other is PointOfInterest) {
            other.id == id &&
                other.poiLayer == poiLayer &&
                other.geometry.size == geometry.size &&
                other.geometry.zip(geometry).all { it.first == it.second }
        } else {
            false
        }
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun toString(): String {
        return "id: $id\n" +
            "temporary: $temporary\n"
    }
}

enum class POILayer {
    CONSTRUCTION,
    GAS_STATION,
    ROAD_HAZARD,
    TRAFFIC_ENFORCEMENT,
    UNKNOWN
}
