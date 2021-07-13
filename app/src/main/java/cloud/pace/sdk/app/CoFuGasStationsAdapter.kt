package cloud.pace.sdk.app

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cloud.pace.sdk.poikit.poi.GasStation
import kotlinx.android.synthetic.main.cofu_gas_station.view.*

class CoFuGasStationsAdapter(private val onItemClick: (String) -> Unit) : RecyclerView.Adapter<CoFuGasStationsAdapter.CoFuGasStationsViewHolder>() {

    var entries: List<GasStation> = emptyList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CoFuGasStationsViewHolder {
        return CoFuGasStationsViewHolder((LayoutInflater.from(parent.context).inflate(R.layout.cofu_gas_station, parent, false)))
    }

    override fun onBindViewHolder(holder: CoFuGasStationsViewHolder, position: Int) {
        holder.bind(entries[position])
    }

    override fun getItemCount() = entries.size

    inner class CoFuGasStationsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val name = itemView.name
        private val prices = itemView.prices

        fun bind(gasStation: GasStation) {
            name.text = gasStation.name
            prices.text = gasStation.prices.joinToString("\n")

            itemView.setOnClickListener { onItemClick(gasStation.id) }
        }
    }
}
