package cloud.pace.sdk.fueling_app.ui.main

import android.location.Location
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isGone
import androidx.recyclerview.widget.RecyclerView
import cloud.pace.sdk.fueling_app.R
import cloud.pace.sdk.fueling_app.util.addressTwoLines
import cloud.pace.sdk.poikit.poi.GasStation
import cloud.pace.sdk.poikit.poi.toLocationPoint
import kotlin.math.roundToInt

class GasStationsAdapter(private val onItemClick: (GasStation) -> Unit) : RecyclerView.Adapter<GasStationsAdapter.ViewHolder>() {

    var entries: List<GasStation> = emptyList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }
    var userLocation: Location? = null
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.gas_station_item, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(entries[position])
    }

    override fun getItemCount(): Int {
        return entries.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val name = itemView.findViewById<TextView>(R.id.name)
        private val address = itemView.findViewById<TextView>(R.id.address)
        private val distance = itemView.findViewById<TextView>(R.id.distance)

        fun bind(gasStation: GasStation) {
            name.text = gasStation.name
            address.text = gasStation.addressTwoLines

            val userLocation = userLocation?.toLocationPoint()
            val gasStationLocation = gasStation.center
            if (userLocation != null && gasStationLocation != null) {
                distance.text = itemView.context.getString(R.string.distance, userLocation.getDistanceInMetersTo(gasStationLocation).roundToInt())
            } else {
                distance.isGone = true
            }

            itemView.setOnClickListener { onItemClick(gasStation) }
        }
    }
}
