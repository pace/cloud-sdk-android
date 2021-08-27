package car.pace.cofu.util.views

import android.content.Context
import android.graphics.Canvas
import android.text.Layout
import android.util.AttributeSet
import com.google.android.material.button.MaterialButton
import kotlin.math.ceil

/**
 * A material button that does not automatically fill the parent when it is multiline
 * but orientates on the widest line while still being compatible with icons
 * Inspired by https://stackoverflow.com/a/13203729/2549828
 */
class WrapWidthMaterialButton : MaterialButton {

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int)
            : super(context, attrs, defStyleAttr)

    private var actualWidthToParentWidthDifference = 0
    private var isDrawing = false

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        if (layout == null || layout.lineCount < 2) return

        val actualWidth = getActualWidth(layout)
        if (actualWidth < measuredWidth) {
            actualWidthToParentWidthDifference = measuredWidth - actualWidth
            setMeasuredDimension(actualWidth, measuredHeight)
        }

    }

    /**
     * computes the actual width the view should be measured to by using the width
     * of the widest line plus the paddings for the compound drawables
     */
    private fun getActualWidth(layout: Layout): Int {
        val maxLineWidth = (0 until layout.lineCount)
            .map { layout.getLineWidth(it) }
            .maxOrNull() ?: 0.0f
        return ceil(maxLineWidth).toInt() + compoundPaddingLeft + compoundPaddingRight
    }

    override fun onDraw(canvas: Canvas) {
        isDrawing = true
        super.onDraw(canvas)
        isDrawing = false
    }

    /**
     * a workaround to make the TextView.onDraw method draw the text in the new centre of the view
     * by substracting half of the actual width difference from the returned value
     * This should only be done during drawing however, since otherwise the initial width calculation will fail
     */
    override fun getCompoundPaddingLeft(): Int {
        return super.getCompoundPaddingLeft().let { value ->
            if (isDrawing) value - actualWidthToParentWidthDifference / 2 else value
        }
    }


}

