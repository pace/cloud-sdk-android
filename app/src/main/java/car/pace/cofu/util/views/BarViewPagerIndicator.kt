package car.pace.cofu.util.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import car.pace.cofu.R

/**
 * A indicator for view pages.
 */
open class BarViewPagerIndicator : BaseViewPagerIndicator {

    constructor(context: Context) : super(context) {
        init(null)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(attrs, defStyleAttr)
    }

    private val paint = Paint().apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 1f
        strokeCap = Paint.Cap.ROUND
    }

    var activeColor = Color.WHITE
    var inActiveColor = Color.BLACK
    var showProgress = false

    var margin = 0f

    private fun init(attrs: AttributeSet?, defStyleAttr: Int = 0) {
        val a = context.obtainStyledAttributes(attrs, R.styleable.BarViewPagerIndicator, defStyleAttr, 0)
        viewPagerId = a.getResourceId(R.styleable.BarViewPagerIndicator_viewPager, -1)
        showProgress = a.getBoolean(R.styleable.BarViewPagerIndicator_showProgress, false)
        margin = a.getDimension(R.styleable.BarViewPagerIndicator_barMargin, 12 * context.resources.displayMetrics.density)
        paint.strokeWidth = a.getDimension(R.styleable.BarViewPagerIndicator_barHeight, 3 * context.resources.displayMetrics.density)
        activeColor = a.getColor(R.styleable.BarViewPagerIndicator_activeColor, Color.WHITE)
        inActiveColor = a.getColor(R.styleable.BarViewPagerIndicator_inactiveColor, Color.argb(125, 255, 255, 255))
        a.recycle()

        if (isInEditMode) {
            totalItems = 4
            currentPosition = 1
        }
    }

    override fun onDraw(canvas: Canvas) {
        val itemWidth = (width - paddingLeft - paddingRight - (totalItems - 1) * margin) / totalItems
        val y = height / 2 - paint.strokeWidth / 2
        var x = paddingLeft.toFloat()

        for (i in 0 until totalItems) {
            paint.color = when {
                currentPosition == i - 1 -> inActiveColor.mixWithColor(activeColor)
                currentPosition == i && !showProgress -> activeColor.mixWithColor(inActiveColor)
                currentPosition >= i && showProgress -> activeColor
                else -> inActiveColor
            }
            canvas.drawLine(x, y, x + itemWidth, y, paint)
            x += margin + itemWidth
        }

    }

}