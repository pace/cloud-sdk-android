package cloud.pace.sdk.fueling_app.ui.payment_method

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isGone
import androidx.recyclerview.widget.RecyclerView
import cloud.pace.sdk.fueling_app.R
import cloud.pace.sdk.fueling_app.data.model.PaymentMethod
import cloud.pace.sdk.fueling_app.util.localizedKind

class PaymentMethodsAdapter(private val onItemClick: (PaymentMethod) -> Unit) : RecyclerView.Adapter<PaymentMethodsAdapter.ViewHolder>() {

    var entries: List<Pair<PaymentMethod, Boolean>> = emptyList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.payment_method_item, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(entries[position])
    }

    override fun getItemCount(): Int {
        return entries.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val kind = itemView.findViewById<TextView>(R.id.kind)
        private val alias = itemView.findViewById<TextView>(R.id.alias)
        private val error = itemView.findViewById<TextView>(R.id.error)

        fun bind(pair: Pair<PaymentMethod, Boolean>) {
            val (paymentMethod, isSupported) = pair
            kind.text = paymentMethod.localizedKind(itemView.context)
            alias.text = paymentMethod.alias ?: paymentMethod.identificationString
            error.isGone = isSupported
            if (isSupported) {
                itemView.setOnClickListener { onItemClick(paymentMethod) }
            }
        }
    }
}
