package cloud.pace.sdk.poikit.utils

import cloud.pace.sdk.poikit.poi.LocationPoint
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.VisibleRegion

fun LatLngBounds.contains(point: LocationPoint): Boolean {
    return point.lat in southwest.latitude..northeast.latitude && point.lon in southwest.longitude..northeast.longitude
}

fun LatLngBounds.distanceTo(bounds: LatLngBounds): Double {
    return center.distanceTo(bounds.center)
}

fun LatLngBounds(center: LatLng, radius: Double): LatLngBounds {
    val southwest = center.offsetBy(radius, 225.0)
    val northeast = center.offsetBy(radius, 45.0)
    return LatLngBounds(southwest, northeast)
}

fun LatLngBounds(lat: Double, lng: Double, radius: Double): LatLngBounds {
    return LatLngBounds(LatLng(lat, lng), radius)
}

fun LatLngBoundsRadius(center: LatLng, radius: Double): LatLngBounds {
    return LatLngBounds.Builder()
        .include(center.offsetBy(radius, 0.0))
        .include(center.offsetBy(radius, 90.0))
        .include(center.offsetBy(radius, 180.0))
        .include(center.offsetBy(radius, 270.0))
        .build()
}

fun LatLngBounds.toVisibleRegion(): VisibleRegion {
    val nearLeft = southwest
    val farLeft = LatLng(northeast.latitude, southwest.longitude)
    val nearRight = LatLng(southwest.latitude, northeast.longitude)
    val farRight = northeast
    val bounds = LatLngBounds(nearLeft, farRight)
    return VisibleRegion(nearLeft, nearRight, farLeft, farRight, bounds)
}
