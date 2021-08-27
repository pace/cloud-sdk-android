package car.pace.cofu.util.views

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.viewpager.widget.ViewPager

/**
 * A ViewPager where scrolling by the user can be enabled or disabled during runtime.
 */
class DisableSwipeViewPager : ViewPager {
    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    var swipingEnabled = false

    inline val canScroll: Boolean get() = swipingEnabled && (canScrollHorizontally(1) || canScrollHorizontally(-1))

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        return if (canScroll) {
            // Fix for PhotoView that has problems within ViewPagers; see https://github.com/chrisbanes/PhotoView#issues-with-viewgroups
            return try {
                super.onInterceptTouchEvent(event)
            } catch (e :IllegalArgumentException) {
                //uncomment if you really want to see these errors
                //e.printStackTrace()
                false
            }
        } else false
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return if (canScroll) super.onTouchEvent(event) else false
    }
}