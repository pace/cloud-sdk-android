package car.pace.cofu.core.mvvm

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import car.pace.cofu.R
import java.util.*

/**
 * This is a implementation of a very basic base adapter.
 *
 * It makes building simple example lists very fast, although it is not
 * a very throughout built adapter.
 * **Important:** Set the right lifecycle owner of this adapter to make use of `MutableLiveData used in item `ViewModels`.
 */
open class BaseRecyclerAdapter : RecyclerView.Adapter<BindableViewHolder>() {

    final override fun setHasStableIds(hasStableIds: Boolean) {
        super.setHasStableIds(hasStableIds)
    }

    init {
        setHasStableIds(true)
    }

    private val diffCallback: DiffUtil.ItemCallback<BaseItemViewModel> =
        object : DiffUtil.ItemCallback<BaseItemViewModel>() {

            override fun areItemsTheSame(item1: BaseItemViewModel, item2: BaseItemViewModel) =
                item1.id == item2.id

            @SuppressLint("DiffUtilEquals")
            override fun areContentsTheSame(item1: BaseItemViewModel, item2: BaseItemViewModel) =
                item1.item.equals(item2.item)
        }
    private val differ: AsyncListDiffer<BaseItemViewModel> = AsyncListDiffer(this, diffCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BindableViewHolder {
        val view = DataBindingUtil.inflate<ViewDataBinding>(
            LayoutInflater.from(parent.context),
            viewType,
            parent,
            false
        )
        return BindableViewHolder(view)
    }

    @CallSuper
    open fun onItemMove(fromPosition: Int, toPosition: Int): Boolean {
        val list = differ.currentList.toMutableList()
        Collections.swap(list, fromPosition, toPosition)
        setItems(list)
        return true
    }

    @CallSuper
    open fun onItemDismiss(position: Int) {
        val list = differ.currentList.toMutableList()
        list.removeAt(position)
        setItems(list)
    }

    fun getItems(): List<BaseItemViewModel> {
        return differ.currentList
    }

    open fun clear() {
        differ.submitList(listOf())
    }

    open fun setItems(newItems: List<BaseItemViewModel>) {
        differ.submitList(newItems)
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: BindableViewHolder, position: Int) {
        holder.bind(differ.currentList[holder.adapterPosition])

        if (holder.adapterPosition in 0 until itemCount) {
            differ.currentList[holder.adapterPosition].onAttached()
        }
    }

    override fun getItemViewType(position: Int): Int {
        return differ.currentList[position].layoutId
    }

    override fun getItemId(position: Int): Long {
        return differ.currentList[position].id.toLong()
    }

    override fun onViewRecycled(holder: BindableViewHolder) {
        if (holder.adapterPosition in 0 until itemCount) {
            differ.currentList[holder.adapterPosition].onCleared()
        }
        super.onViewRecycled(holder)
    }
}

/**
 * A base view holder is capable of setting flags for letting a item touch helper know, if the holder should be able
 * to be swiped and/or dragged. If [isDraggable] is set to true, make sure to also set a valid [swipeableView] of the
 * item.
 */
open class BindableViewHolder(private val binding: ViewDataBinding) :
    RecyclerView.ViewHolder(binding.root) {

    open var isDraggable: Boolean = false
    open var isSwipeable: Boolean = false
    open var swipeableView: View? = null

    fun bind(viewModel: BaseItemViewModel) {
        binding.setVariable(viewModel.bindVar, viewModel)
        binding.root.setTag(R.id.viewpager_item_hashcode, viewModel.hashCode())
    }

    open fun onHolderDrag() {
        // do implement in sub class
    }

    @CallSuper
    open fun onHolderSwipe(
        dX: Float,
        dY: Float,
        actionState: Int
    ) {
        swipeableView?.let {
            it.translationX = dX
        }
    }

    @CallSuper
    open fun onHolderClear() {
        swipeableView?.let {
            it.translationX = 0.0f
        }
    }
}
