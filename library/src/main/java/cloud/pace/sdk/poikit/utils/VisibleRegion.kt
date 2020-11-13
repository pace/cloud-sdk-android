package cloud.pace.sdk.poikit.utils

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
