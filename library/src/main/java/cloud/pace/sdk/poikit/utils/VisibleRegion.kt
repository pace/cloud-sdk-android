package cloud.pace.sdk.poikit.utils

import TileQueryRequestOuterClass
import cloud.pace.sdk.poikit.poi.toLocationPoint
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.VisibleRegion
import com.google.maps.android.SphericalUtil

/**
 * Factor that adds a small padding to account for variances in the visible region
 */
private const val BOUNDS_PADDING_FACTOR = 0.975

fun VisibleRegion.diameter(): Double {
    return nearLeft.toLocationPoint().getDistanceInMetersTo(farRight.toLocationPoint())
}

fun VisibleRegion.center(): LatLng {
    return latLngBounds.center
}

/**
 * Gets the zoomed version of [this] [VisibleRegion] so that the [diameter] is reached.
 *
 * @param diameter the diameter to zoom the region to
 *
 * @return a new [VisibleRegion] with approximately the given [diameter]
 */
fun VisibleRegion.zoomToDiameter(diameter: Double): VisibleRegion {
    // Add a negative padding because of variances in projection accuracy
    val paddedDiameter = diameter * BOUNDS_PADDING_FACTOR

    // calculate the headings to each edge
    val center = latLngBounds.center
    val nearLeft = SphericalUtil.computeOffset(center, paddedDiameter / 2.0, center.headingTo(nearLeft))
    val nearRight = SphericalUtil.computeOffset(center, paddedDiameter / 2.0, center.headingTo(nearRight))
    val farLeft = SphericalUtil.computeOffset(center, paddedDiameter / 2.0, center.headingTo(farLeft))
    val farRight = SphericalUtil.computeOffset(center, paddedDiameter / 2.0, center.headingTo(farRight))

    val bounds = LatLngBounds.Builder()
        .include(nearLeft)
        .include(nearRight)
        .include(farLeft)
        .include(farRight)
        .build()

    return VisibleRegion(nearLeft, nearRight, farLeft, farRight, bounds)
}

/**
 * Adds the [padding] to each side of [this] [VisibleRegion] and returns a new padded [VisibleRegion].
 *
 * @return A new [VisibleRegion] with the added [padding]
 */
fun VisibleRegion.addPadding(padding: Double): VisibleRegion {
    val diameter = diameter()
    val paddedDiameter = diameter + diameter * padding

    // calculate the headings to each edge
    val center = latLngBounds.center
    val nearLeft = SphericalUtil.computeOffset(center, paddedDiameter / 2.0, center.headingTo(nearLeft))
    val nearRight = SphericalUtil.computeOffset(center, paddedDiameter / 2.0, center.headingTo(nearRight))
    val farLeft = SphericalUtil.computeOffset(center, paddedDiameter / 2.0, center.headingTo(farLeft))
    val farRight = SphericalUtil.computeOffset(center, paddedDiameter / 2.0, center.headingTo(farRight))

    val bounds = LatLngBounds.Builder()
        .include(nearLeft)
        .include(nearRight)
        .include(farLeft)
        .include(farRight)
        .build()

    return VisibleRegion(nearLeft, nearRight, farLeft, farRight, bounds)
}

/**
 * Calculates the incremental padding depending on the [currentIncrement].
 * It ensures that the value lies in the specified range [minPadding]..[maxPadding].
 *
 * @param maxIncrements The maximum number of increments/steps
 */
fun incrementalPadding(maxIncrements: Int, currentIncrement: Int, maxPadding: Double = 0.85, minPadding: Double = 0.2): Double {
    val paddingDifference = maxPadding - minPadding
    val factor = maxIncrements - currentIncrement
    val relativePadding = (paddingDifference / maxIncrements) * factor
    val padding = relativePadding + minPadding

    return padding.coerceIn(minPadding, maxPadding)
}

fun VisibleRegion.toTileQueryRequest(zoomLevel: Int): TileQueryRequestOuterClass.TileQueryRequest {
    val northEast = this.latLngBounds.northeast.toLocationPoint().tileInfo(zoom = zoomLevel)
    val southWest = this.latLngBounds.southwest.toLocationPoint().tileInfo(zoom = zoomLevel)

    val areaQuery = TileQueryRequestOuterClass.TileQueryRequest.AreaQuery.newBuilder().also {
        it.northEast = TileQueryRequestOuterClass.TileQueryRequest.Coordinate.newBuilder().setX(northEast.x).setY(northEast.y).build()
        it.southWest = TileQueryRequestOuterClass.TileQueryRequest.Coordinate.newBuilder().setX(southWest.x).setY(southWest.y).build()
    }

    return TileQueryRequestOuterClass.TileQueryRequest.newBuilder()
        .addAreas(areaQuery)
        .setZoom(zoomLevel)
        .build()
}
