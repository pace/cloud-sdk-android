package cloud.pace.sdk.fueling_app.ui.pump

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import cloud.pace.sdk.fueling_app.R
import cloud.pace.sdk.fueling_app.data.model.Pump

class PumpsAdapter(private val entries: Array<Pump>, private val onItemClick: (Pump) -> Unit) : RecyclerView.Adapter<PumpsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.pump_item, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(entries[position])
    }

    override fun getItemCount(): Int {
        return entries.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val identifier = itemView.findViewById<TextView>(R.id.identifier)

        fun bind(pump: Pump) {
            identifier.text = pump.identifier
            itemView.setOnClickListener { onItemClick(pump) }
        }
    }
}
