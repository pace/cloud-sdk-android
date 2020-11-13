package cloud.pace.sdk.poikit.utils

import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.SphericalUtil

/**
 * Returns the distance to the [other] location in meters.
 *
 * @see SphericalUtil.computeDistanceBetween
 */
fun LatLng.distanceTo(other: LatLng): Double = SphericalUtil.computeDistanceBetween(this, other)

/**
 * Returns the heading to the [other] location.
 *
 * @see SphericalUtil.computeHeading
 */
fun LatLng.headingTo(other: LatLng): Double = SphericalUtil.computeHeading(this, other)

/**
 * Returns the location resulting from moving a distance from this
 * location in the specified heading (expressed in degrees clockwise
 * from north).
 *
 * @see SphericalUtil.computeOffset
 */
fun LatLng.offsetBy(meters: Double, heading: Double): LatLng = SphericalUtil.computeOffset(this, meters, heading)
