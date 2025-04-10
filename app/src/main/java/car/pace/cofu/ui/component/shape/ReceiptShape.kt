package car.pace.cofu.ui.component.shape

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection

class ReceiptShape : Shape {

    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline = Outline.Generic(
        path = createReceiptPath(size = size)
    )

    companion object {
        fun createReceiptPath(size: Size): Path = Path().apply {
            val dentCount = 22
            // the gap between the half circles is just as wide as the radius, so the repeated pattern is 3*radius wide
            val radius = size.width / (3 * dentCount + 1)
            val gapWidth = radius
            val patternWidth = 3 * radius
            reset()

            for (i in 0..<dentCount) {
                lineTo(x = i * patternWidth + gapWidth, y = 0f)
                arcTo(
                    rect = Rect(
                        left = i * patternWidth + gapWidth,
                        top = -radius,
                        right = (i + 1) * patternWidth,
                        bottom = radius
                    ),
                    startAngleDegrees = 180.0f,
                    sweepAngleDegrees = -180.0f,
                    forceMoveTo = false
                )
            }
            lineTo(x = size.width, y = 0f)
            lineTo(x = size.width, y = size.height)

            for (i in 0..<dentCount) {
                lineTo(x = size.width - i * patternWidth - gapWidth, y = size.height)
                arcTo(
                    rect = Rect(
                        left = i * patternWidth + gapWidth,
                        top = size.height - radius,
                        right = (i + 1) * patternWidth,
                        bottom = size.height + radius
                    ),
                    startAngleDegrees = 0f,
                    sweepAngleDegrees = -180.0f,
                    forceMoveTo = false
                )
            }
            lineTo(x = 0f, y = size.height)
            close()
        }
    }
}
