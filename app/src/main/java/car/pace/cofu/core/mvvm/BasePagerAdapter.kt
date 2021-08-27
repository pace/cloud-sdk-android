package car.pace.cofu.core.mvvm

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.viewpager.widget.PagerAdapter
import car.pace.cofu.R

/**
 * A simple pager adapter implementation for binding itemViewModels.
 */
class BasePagerAdapter(
    _items: List<BaseItemViewModel>,
    var pageWidth: Float = 1f,
    private val titles: List<String>? = null
) : PagerAdapter() {

    var items: List<BaseItemViewModel> = _items
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun isViewFromObject(view: View, obj: Any): Boolean = obj === view

    override fun getCount() = items.size

    override fun getItemPosition(obj: Any): Int {
        val hashCode = (obj as? View)?.getTag(R.id.viewpager_item_hashcode) ?: return POSITION_NONE
        return if (items.asSequence().map { it.hashCode() }.contains(hashCode)) POSITION_UNCHANGED else POSITION_NONE
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return if (!titles.isNullOrEmpty()) {
            return titles[position]
        } else {
            null
        }
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val item = items[position]
        val inflater = LayoutInflater.from(container.context)
        val binding: ViewDataBinding = DataBindingUtil.inflate(inflater, item.layoutId, container, true)
        binding.setVariable(item.bindVar, item)
        binding.root.setTag(R.id.viewpager_item_hashcode, item.hashCode())
        return binding.root
    }

    override fun destroyItem(container: ViewGroup, position: Int, obj: Any) {
        container.removeView(obj as View)
    }

    override fun getPageWidth(position: Int) = pageWidth
}