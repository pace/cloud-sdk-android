package car.pace.cofu.ui.map

import cloud.pace.sdk.poikit.poi.GasStation
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem

sealed class MarkerDetails(open val id: String)
data class FullMarkerDetails(val gasStation: GasStation) : MarkerDetails(gasStation.id)
data class ReducedMarkerDetails(override val id: String) : MarkerDetails(id)

data class MarkerItem(
    val markerDetails: MarkerDetails,
    val itemPosition: LatLng,
    val itemTitle: String? = null,
    val itemSnippet: String? = null,
    val itemZIndex: Float? = null
) : ClusterItem {

    override fun getPosition() = itemPosition

    override fun getTitle() = itemTitle

    override fun getSnippet() = itemSnippet

    override fun getZIndex() = itemZIndex
}
