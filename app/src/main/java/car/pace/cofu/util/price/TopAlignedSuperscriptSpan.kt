package car.pace.cofu.util.price

import android.graphics.Rect
import android.text.TextPaint
import android.text.style.MetricAffectingSpan

class TopAlignedSuperscriptSpan(val scale: Float) : MetricAffectingSpan() {

    override fun updateDrawState(paint: TextPaint) {
        updateAnyState(paint)
    }

    override fun updateMeasureState(paint: TextPaint) {
        updateAnyState(paint)
    }

    private fun updateAnyState(paint: TextPaint) {
        val bounds = Rect()
        paint.getTextBounds("1A", 0, 2, bounds)
        var shift = bounds.top - bounds.bottom
        paint.textSize = paint.textSize * scale
        paint.getTextBounds("1A", 0, 2, bounds)
        shift += bounds.bottom - bounds.top
        paint.baselineShift += shift
        paint.letterSpacing = 0.2f
    }
}
