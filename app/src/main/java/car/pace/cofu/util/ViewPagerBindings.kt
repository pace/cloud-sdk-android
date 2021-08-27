package car.pace.cofu.util

import androidx.databinding.BindingAdapter
import androidx.databinding.InverseBindingAdapter
import androidx.databinding.InverseBindingListener
import androidx.viewpager.widget.ViewPager
import car.pace.cofu.core.mvvm.BaseItemViewModel
import car.pace.cofu.core.mvvm.BasePagerAdapter

@BindingAdapter("pagerItems", "pageWidth", "selectedPage", requireAll = false)
fun ViewPager.setPagerItems(_items: List<BaseItemViewModel>, _pageWidth: Float = 1.0f, page: Int? = null) {
    val newPageWidth = if (_pageWidth == 0f) 1f else _pageWidth
    if (adapter is BasePagerAdapter) {
        (adapter as BasePagerAdapter).apply {
            items = ArrayList(_items)
            pageWidth = newPageWidth
        }
    } else {
        adapter = BasePagerAdapter(_items, newPageWidth)
    }

    if (page != null) {
        val isInBounds = page >= 0 && page < adapter!!.count
        if (isInBounds) setCurrentItem(page, true)
    }

}

@BindingAdapter("selectedPageAttrChanged")
fun listenForSelectedViewpagerPage(viewPager: ViewPager, listener: InverseBindingListener) {
    viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
        override fun onPageScrollStateChanged(state: Int) {}

        override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

        override fun onPageSelected(position: Int) {
            listener.onChange()
        }
    })
}

@InverseBindingAdapter(attribute = "selectedPage")
fun getSelectedViewpagerPage(viewPager: ViewPager) = viewPager.currentItem
