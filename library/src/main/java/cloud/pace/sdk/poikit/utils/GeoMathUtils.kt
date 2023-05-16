package cloud.pace.sdk.poikit.utils

import cloud.pace.sdk.poikit.poi.LocationPoint
import cloud.pace.sdk.poikit.poi.tiles.TileInformation
import com.google.android.gms.maps.model.LatLng
import vector_tile.VectorTile
import java.lang.Math.ceil
import java.lang.Math.floor
import java.lang.Math.pow

/**
 * Math utilities for calculations related
 *
 * See https://wiki.openstreetmap.org/wiki/Slippy_map_tilenames#Implementations
 */

object GeoMathUtils {
    enum class Compass {
        NORTH,
        SOUTH,
        EAST,
        WEST
    }

    fun long2tilex(lon: Double, zoom: Int): Int {
        return floor(long2x(lon, zoom)).toInt()
    }

    fun lat2tiley(lat: Double, zoom: Int): Int {
        return floor(lat2y(lat, zoom)).toInt()
    }

    fun long2x(lon: Double, zoom: Int): Double {
        return (lon + 180.0) / 360.0 * pow(2.0, zoom.toDouble())
    }

    fun lat2y(lat: Double, zoom: Int): Double {
        return (
            1.0 -
                Math.log(
                Math.tan(lat * Math.PI / 180.0) + 1.0 / Math.cos(lat * Math.PI / 180.0)
            ) / Math.PI
            ) / 2.0 * pow(
            2.0,
            zoom.toDouble()
        )
    }

    fun tilex2long(tileX: Double, zoom: Int): Double {
        return tileX / Math.pow(2.0, zoom.toDouble()) * 360.0 - 180.0
    }

    fun tiley2lat(tileY: Double, zoom: Int): Double {
        val number = Math.PI - 2.0 * Math.PI * tileY / Math.pow(2.0, zoom.toDouble())
        return 180.0 / Math.PI * Math.atan(0.5 * (Math.exp(number) - Math.exp(-number)))
    }

    fun getValues(
        feature: VectorTile.Tile.Feature,
        layer: VectorTile.Tile.Layer
    ): HashMap<String, String> {
        val values = HashMap<String, String>()
        var index = 0
        while (index < feature.tagsCount) {
            val keyTag = feature.getTags(index)
            val nameTag = feature.getTags(index + 1)
            index += 2

            val key = layer.getKeys(keyTag)
            val value = layer.getValues(nameTag).stringValue

            values[key] = value
        }
        return values
    }

    fun toDegrees(rad: Double): Double {
        return rad * 180.0 / Math.PI
    }

    fun toRadian(degrees: Double): Double {
        return degrees * Math.PI / 180.0
    }

    fun getTileCount(zoom: Int): Int {
        return if (zoom == POIKitConfig.ZOOMLEVEL) {
            65536 // speed things up
        } else {
            pow(2.0, zoom.toDouble()).toInt()
        }
    }

    /**
     * Get adjacent tile by compass direction.
     *
     * @param tile the current tile
     * @param compass our heading
     *
     * @return the adjacent tile information
     */
    fun getAdjacentTile(tile: TileInformation, compass: Compass): TileInformation {
        val tileCount = getTileCount(tile.zoomLevel)
        return when (compass) {
            Compass.NORTH -> TileInformation(tile.zoomLevel, tile.x, (tile.y + tileCount - 1) % tileCount)
            Compass.SOUTH -> TileInformation(tile.zoomLevel, tile.x, (tile.y + 1) % tileCount)
            Compass.EAST -> TileInformation(tile.zoomLevel, (tile.x + 1) % tileCount, tile.y)
            Compass.WEST -> TileInformation(tile.zoomLevel, (tile.x + tileCount - 1) % tileCount, tile.y)
        }
    }

    /**
     * Get distance to the tile border on the side, based on the given heading.
     *
     * @param tile the current tile
     * @param location current precise location
     * @param heading heading as a compass direction
     *
     * Example: we are heading north, being close to the "left" border of the tile. The method should return
     * the distance to the west border of the tile in meters together with compass direction west.
     *
     * @return the distance to the border in meters and the direction
     */
    fun getDistanceToTileBorderSide(tile: TileInformation, location: LocationPoint, heading: Compass): Pair<Double, Compass> {
        return if (heading == Compass.NORTH || heading == Compass.SOUTH) {
            val locX = long2x(location.lon, tile.zoomLevel)
            val westDistance = locX - floor(locX)
            val eastDistance = ceil(locX) - locX
            if (westDistance < eastDistance) {
                Pair(
                    LocationPoint(location.lat, tilex2long(floor(locX), tile.zoomLevel)).getDistanceInMetersTo(location),
                    Compass.WEST
                )
            } else {
                Pair(
                    LocationPoint(location.lat, tilex2long(ceil(locX), tile.zoomLevel)).getDistanceInMetersTo(location),
                    Compass.EAST
                )
            }
        } else { // EAST or WEST
            val locY = lat2y(location.lat, tile.zoomLevel)
            val northDistance = locY - floor(locY)
            val southDistance = ceil(locY) - locY

            if (northDistance < southDistance) {
                Pair(
                    LocationPoint(tiley2lat(floor(locY), tile.zoomLevel), location.lon).getDistanceInMetersTo(location),
                    Compass.NORTH
                )
            } else {
                Pair(
                    LocationPoint(tiley2lat(ceil(locY), tile.zoomLevel), location.lon).getDistanceInMetersTo(location),
                    Compass.SOUTH
                )
            }
        }
    }

    private fun locationPointToLatLng(location: LocationPoint): LatLng {
        return LatLng(location.lat, location.lon)
    }
}
