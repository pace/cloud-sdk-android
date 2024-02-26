package car.pace.cofu.ui.component

import android.graphics.BlurMaskFilter
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import car.pace.cofu.ui.theme.OnSurface

fun Modifier.bubbleShape(
    backgroundColor: Color,
    shadowColor: Color,
    shadowRadius: Dp,
    cornerRadius: Dp,
    arrowWidth: Dp,
    arrowHeight: Dp,
    arrowOffset: Dp
) = then(
    drawWithCache {
        val shadowRadiusPx = shadowRadius.toPx()
        val cornerRadiusPx = cornerRadius.toPx()
        val arrowWidthPx = arrowWidth.toPx()
        val arrowHeightPx = arrowHeight.toPx()
        val arrowOffsetPx = arrowOffset.toPx()

        // Shadow
        val paint = Paint()
        val frameworkPaint = paint.asFrameworkPaint()
        paint.color = shadowColor

        if (shadowRadius != 0.dp) {
            frameworkPaint.maskFilter = BlurMaskFilter(shadowRadiusPx, BlurMaskFilter.Blur.OUTER)
        }

        // Bubble shape
        val path = Path()
        val rectBottom = size.height - arrowHeightPx
        path.addRoundRect(
            RoundRect(
                rect = Rect(
                    offset = Offset.Zero,
                    size = Size(size.width, rectBottom)
                ),
                cornerRadius = CornerRadius(cornerRadiusPx, cornerRadiusPx)
            )
        )
        path.moveTo(arrowOffsetPx, rectBottom)
        path.lineTo(arrowOffsetPx + arrowWidthPx / 2, size.height)
        path.lineTo(arrowOffsetPx + arrowWidthPx, rectBottom)

        onDrawBehind {
            drawIntoCanvas {
                // Draw shadow
                it.drawPath(path, paint)
            }

            // Draw background
            drawPath(path, backgroundColor)
        }
    }
)

fun Modifier.dropShadow(
    color: Color = OnSurface.copy(alpha = 0.3f),
    offsetX: Dp = 0.dp,
    offsetY: Dp = 0.dp,
    radius: Dp = 10.dp,
    isRound: Boolean = false
) = then(
    drawBehind {
        drawIntoCanvas {
            val radiusPx = radius.toPx()
            val paint = Paint()
            val frameworkPaint = paint.asFrameworkPaint()
            paint.color = color

            if (radius != 0.dp) {
                frameworkPaint.maskFilter = BlurMaskFilter(radiusPx, BlurMaskFilter.Blur.NORMAL)
            }

            val leftPixel = offsetX.toPx()
            val topPixel = offsetY.toPx()
            val rightPixel = size.width + topPixel
            val bottomPixel = size.height + leftPixel

            if (isRound) {
                it.drawCircle(
                    center = Offset(rightPixel / 2, bottomPixel / 2),
                    radius = radiusPx,
                    paint = paint
                )
            } else {
                it.drawRoundRect(
                    left = leftPixel,
                    top = topPixel,
                    right = rightPixel,
                    bottom = bottomPixel,
                    radiusX = radiusPx,
                    radiusY = radiusPx,
                    paint = paint
                )
            }
        }
    }
)
