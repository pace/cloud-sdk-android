package cloud.pace.sdk.poikit.poi

import TileQueryRequestOuterClass
import android.location.Location
import android.os.Parcelable
import cloud.pace.sdk.poikit.poi.download.TileInformation
import cloud.pace.sdk.poikit.utils.GeoMathUtils
import cloud.pace.sdk.poikit.utils.GeoMathUtils.toDegrees
import cloud.pace.sdk.poikit.utils.GeoMathUtils.toRadian
import cloud.pace.sdk.poikit.utils.POIKitConfig
import com.google.android.gms.maps.model.LatLng
import kotlinx.android.parcel.Parcelize
import kotlin.math.*

/**
 * Representation of a lat/lon location on earth.
 * Similar to Google's LatLng. We use our own class for the JUnit tests.
 */
@Parcelize
data class LocationPoint(val lat: Double, val lon: Double) : Parcelable {

    fun tileInfo(zoom: Int): TileInformation {
        val tileX = GeoMathUtils.long2tilex(this.lon, zoom)
        val tileY = GeoMathUtils.lat2tiley(this.lat, zoom)

        return TileInformation(zoom, tileX, tileY)
    }

    override fun toString(): String {
        return "$lat, $lon"
    }

    override fun equals(other: Any?): Boolean {
        val locationPoint = other as? LocationPoint ?: return false
        return this.getDistanceInMetersTo(locationPoint) < 5.0
    }

    override fun hashCode(): Int {
        return toString().hashCode()
    }

    fun move(distanceKm: Double, bearingDegrees: Double): LocationPoint {
        val distanceRadians = distanceKm / POIKitConfig.EARTH_RADIUS_KM
        val bearingRadians = toRadian(bearingDegrees)
        val fromLatRadians = toRadian(this.lat)
        val fromLonRadians = toRadian(this.lon)

        val toLatRadians = asin(
            sin(fromLatRadians) * cos(distanceRadians) + cos(fromLatRadians) * sin(distanceRadians) * cos(bearingRadians)
        )

        var toLonRadians = fromLonRadians + atan2(
            sin(bearingRadians) * sin(distanceRadians) * cos(fromLatRadians),
            cos(distanceRadians) - sin(fromLatRadians) * sin(toLatRadians)
        )
        toLonRadians = (toLonRadians + 3 * Math.PI) % (2 * Math.PI) - Math.PI
        return LocationPoint(toDegrees(toLatRadians), toDegrees(toLonRadians))
    }

    fun getDistanceInMetersTo(lp: LocationPoint): Double {
        val hp = Math.PI / 180.0
        val aa =
            0.5 - cos((lp.lat - this.lat) * hp) / 2.0 + cos(this.lat * hp) * cos(lp.lat * hp) * (1.0 - cos(
                (lp.lon - this.lon) * hp
            )) / 2.0
        return 2.0 * POIKitConfig.EARTH_RADIUS_KM * 1000.0 * asin(sqrt(aa))
    }

    fun getBearingTo(locationPoint: LocationPoint): Double {
        val lat1 = toRadian(this.lat)
        val lon1 = toRadian(this.lon)
        val lat2 = toRadian(locationPoint.lat)
        val lon2 = toRadian(locationPoint.lon)

        val deltaLon = lon2 - lon1

        val tmpy = Math.sin(deltaLon) * Math.cos(lat2)
        val tmpx = Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1) * Math.cos(lat2) * Math.cos(deltaLon)
        val radiansBearing = Math.atan2(tmpy, tmpx)
        return (toDegrees(radiansBearing) + 360.0) % 360.0
    }

    // Algorithm from: https://wrf.ecse.rpi.edu//Research/Short_Notes/pnpoly.html
    // Flat earth here again - assuming small polygons.
    fun isIn(polygon: List<LocationPoint>): Boolean {
        var j = polygon.lastOrNull() ?: return false
        var isInPolygon = false
        for (i in polygon) {
            val a = (i.lon > lon) != (j.lon > lon)
            val b = lat < (j.lat - i.lat) * (lon - i.lon) / (j.lon - i.lon) + i.lat
            if (a && b) {
                isInPolygon = !isInPolygon
            }
            j = i
        }
        return isInPolygon
    }

    fun toLatLn(): LatLng {
        return LatLng(this.lat, this.lon)
    }
}

fun LatLng.toLocationPoint(): LocationPoint {
    return LocationPoint(this.latitude, this.longitude)
}

fun Location.toLocationPoint(): LocationPoint {
    return LocationPoint(this.latitude, this.longitude)
}

fun LocationPoint.toTileQueryRequest(zoomLevel: Int): TileQueryRequestOuterClass.TileQueryRequest {
    val tileInfo = tileInfo(zoomLevel)
    val tile = TileQueryRequestOuterClass.TileQueryRequest.IndividualTileQuery.newBuilder().also {
        it.geo = TileQueryRequestOuterClass.TileQueryRequest.Coordinate.newBuilder().setX(tileInfo.x).setY(tileInfo.y).build()
    }.build()

    return TileQueryRequestOuterClass.TileQueryRequest.newBuilder()
        .addTiles(tile)
        .setZoom(zoomLevel)
        .build()
}

fun Collection<LocationPoint>.toTileQueryRequest(zoomLevel: Int): TileQueryRequestOuterClass.TileQueryRequest {
    val tiles = this
        .map { it.tileInfo(zoomLevel) }
        .distinct()
        .map { tile ->
            TileQueryRequestOuterClass.TileQueryRequest.IndividualTileQuery.newBuilder().also {
                it.geo = TileQueryRequestOuterClass.TileQueryRequest.Coordinate.newBuilder().setX(tile.x).setY(tile.y).build()
            }.build()
        }

    return TileQueryRequestOuterClass.TileQueryRequest.newBuilder()
        .addAllTiles(tiles)
        .setZoom(zoomLevel)
        .build()
}
