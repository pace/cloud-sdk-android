package car.pace.cofu.util.views


import android.content.Context
import android.database.DataSetObserver
import android.graphics.Color
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.view.ViewParent
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import kotlin.math.min

/**
 * Base class for ViewPagerIndicators that handles finding the connected viewpager, and registering and unregistering
 * observers. The child classes are responsible for the actual drawing (by overriding [onDraw]).
 * The variables
 * [currentPosition], [totalItems] and [scrollOffset] represent the current state the drawing should base on
 */
@Suppress("NOTHING_TO_INLINE")
abstract class BaseViewPagerIndicator : View, ViewPager.OnAdapterChangeListener, ViewPager.OnPageChangeListener {

    /**
     * total items in the ViewPager
     */
    protected var totalItems = 0

    /**
     * Index of the currently selected item
     */
    protected var currentPosition = 0

    /**
     * while scrolling between two pages, this variable will assume values from 0 to 1 (exclusive)
     * Note that when scrolling backwards, the [currentPosition] will decrease immediately, and the [scrollOffset]
     * will be close to 1
     */
    var scrollOffset = 0f
        protected set

    open fun onChange() {
        invalidate()
    }

    open fun measureWidth(atMost: Int) = min(atMost, 30 * resources.displayMetrics.density.toInt())
    open fun measureHeight(atMost: Int) = min(atMost, 400 * resources.displayMetrics.density.toInt())

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)


    protected inline fun Int.mixWithColor(other: Int): Int {
        return Color.argb(
            Color.alpha(this).mixWith(Color.alpha(other)),
            Color.red(this).mixWith(Color.red(other)),
            Color.green(this).mixWith(Color.green(other)),
            Color.blue(this).mixWith(Color.blue(other))
        )
    }

    protected inline fun Int.mixWith(other: Int): Int {
        return (this + scrollOffset * (other - this)).toInt()
    }

    protected var viewPagerId = -1
    private var viewPager: ViewPager? = null
    private var setObserver: DataSetObserver? = null

    var offsetChangeListener: (() -> Unit)? = null

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        findViewPager(parent)
    }

    private fun findViewPager(viewParent: ViewParent?) {
        val isValidParent = viewParent != null && viewParent is ViewGroup && viewParent.childCount > 0

        if (!isValidParent) return

        val viewPager = findViewPager((viewParent as ViewGroup?)!!, viewPagerId)

        if (viewPager != null) {
            setViewPager(viewPager)
        } else {
            findViewPager(viewParent!!.parent)
        }
    }

    private fun findViewPager(viewGroup: ViewGroup, id: Int): ViewPager? {
        if (viewGroup.childCount <= 0) return null

        val view = viewGroup.findViewById<View>(id)
        return if (view != null && view is ViewPager) view else null
    }

    fun setViewPager(pager: ViewPager?) {
        releaseViewPager()
        if (pager == null) {
            return
        }

        viewPager = pager
        pager.addOnPageChangeListener(this)
        pager.addOnAdapterChangeListener(this)
        viewPagerId = pager.id
        //pager.setOnTouchListener(this)

        updateState()
    }

    /**
     * Release [ViewPager] and stop handling events of [ViewPager.OnPageChangeListener].
     */
    private fun releaseViewPager() {
        viewPager?.let {
            it.removeOnPageChangeListener(this)
            it.removeOnAdapterChangeListener(this)
            viewPager = null
        }
    }

    override fun onDetachedFromWindow() {
        unRegisterSetObserver()
        super.onDetachedFromWindow()
    }

    private fun registerSetObserver() {
        val viewPager = viewPager ?: return
        if (setObserver != null || viewPager.adapter == null) {
            return
        }

        setObserver = object : DataSetObserver() {
            override fun onChanged() {
                updateState()
            }
        }

        try {
            viewPager.adapter!!.registerDataSetObserver(setObserver!!)
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        }

    }

    private fun unRegisterSetObserver() {
        val viewPager = viewPager ?: return
        if (setObserver == null || viewPager.adapter == null) {
            return
        }

        try {
            viewPager.adapter!!.unregisterDataSetObserver(setObserver!!)
            setObserver = null
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        }

    }

    override fun onAdapterChanged(viewPager: ViewPager, oldAdapter: PagerAdapter?, newAdapter: PagerAdapter?) {
        if (oldAdapter != null && setObserver != null) {
            oldAdapter.unregisterDataSetObserver(setObserver!!)
            setObserver = null
        }
        registerSetObserver()
        updateState()
    }

    private fun updateState() {
        val viewPager = viewPager ?: return
        val adapter = viewPager.adapter ?: return

        val count = adapter.count
        val selectedPos = viewPager.currentItem

        if (count != totalItems || selectedPos != currentPosition) {
            totalItems = count
            currentPosition = selectedPos
            scrollOffset = 0f
            onChange()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {

        val heightSpecMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSpecSize = MeasureSpec.getSize(heightMeasureSpec)

        val height = when (heightSpecMode) {
            MeasureSpec.EXACTLY -> heightSpecSize
            MeasureSpec.AT_MOST -> measureHeight(heightSpecSize)
            else -> measureHeight(Int.MAX_VALUE)
        }

        val widthSpecMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSpecSize = MeasureSpec.getSize(widthMeasureSpec)

        val width = when (widthSpecMode) {
            MeasureSpec.EXACTLY -> widthSpecSize
            MeasureSpec.AT_MOST -> measureWidth(widthSpecSize)
            else -> measureWidth(Int.MAX_VALUE)
        }

        setMeasuredDimension(width, height)

        requestLayout()
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
        if (position != currentPosition || positionOffset != scrollOffset) {
            currentPosition = position
            scrollOffset = positionOffset
            onChange()
        }
        offsetChangeListener?.invoke()
    }

    override fun onPageSelected(position: Int) {
        if (position != currentPosition) {
            currentPosition = position
            scrollOffset = 0f
            onChange()
        }
    }

    override fun onPageScrollStateChanged(state: Int) {
        // not needed
    }

}

data class ScrollState(
    val position: Int,
    val offset: Float
)