package cloud.pace.sdk.app

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cloud.pace.sdk.appkit.model.App
import kotlinx.android.synthetic.main.app_item.view.*

class AppListAdapter(private val onItemClick: (App) -> Unit) : RecyclerView.Adapter<AppListAdapter.AppViewHolder>() {

    var entries: List<App> = emptyList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        return AppViewHolder((LayoutInflater.from(parent.context).inflate(R.layout.app_item, parent, false)))
    }

    override fun onBindViewHolder(holder: AppViewHolder, position: Int) {
        holder.bind(entries[position])
    }

    override fun getItemCount() = entries.size

    inner class AppViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val name = itemView.name
        private val description = itemView.description
        private val icon = itemView.icon

        fun bind(app: App) {
            name.text = app.name
            description.text = app.description
            app.textColor?.let {
                val textColor = Color.parseColor(it)
                name.setTextColor(textColor)
                description.setTextColor(textColor)
            }

            app.logo?.let { icon.setImageBitmap(it) }
            app.iconBackgroundColor?.let { icon.setBackgroundColor(Color.parseColor(it)) }
            app.textBackgroundColor?.let { itemView.setBackgroundColor(Color.parseColor(it)) }

            itemView.setOnClickListener { onItemClick(app) }
        }
    }
}
