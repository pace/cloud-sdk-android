package car.pace.cofu.ui.icon

import androidx.compose.material.icons.Icons
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType.Companion.NonZero
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap.Companion.Butt
import androidx.compose.ui.graphics.StrokeJoin.Companion.Miter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.ImageVector.Builder
import androidx.compose.ui.graphics.vector.group
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val Icons.Outlined.Map: ImageVector
    get() {
        if (_map != null) {
            return _map!!
        }
        _map = Builder(
            name = "Outlined.Map",
            defaultWidth = 24.0.dp,
            defaultHeight = 24.0.dp,
            viewportWidth = 24.0f,
            viewportHeight = 24.0f
        ).apply {
            group {
                path(
                    fill = SolidColor(Color(0xFF000000)),
                    stroke = null,
                    strokeLineWidth = 0.0f,
                    strokeLineCap = Butt,
                    strokeLineJoin = Miter,
                    strokeLineMiter = 4.0f,
                    pathFillType = NonZero
                ) {
                    moveTo(20.5f, 3.0f)
                    lineTo(20.34f, 3.03f)
                    lineTo(15.0f, 5.1f)
                    lineTo(9.0f, 3.0f)
                    lineTo(3.36f, 4.9f)
                    curveTo(3.15f, 4.97f, 3.0f, 5.15f, 3.0f, 5.38f)
                    verticalLineTo(20.5f)
                    curveTo(3.0f, 20.78f, 3.22f, 21.0f, 3.5f, 21.0f)
                    lineTo(3.66f, 20.97f)
                    lineTo(9.0f, 18.9f)
                    lineTo(15.0f, 21.0f)
                    lineTo(20.64f, 19.1f)
                    curveTo(20.85f, 19.03f, 21.0f, 18.85f, 21.0f, 18.62f)
                    verticalLineTo(3.5f)
                    curveTo(21.0f, 3.22f, 20.78f, 3.0f, 20.5f, 3.0f)
                    close()
                    moveTo(15.0f, 19.0f)
                    lineTo(9.0f, 16.89f)
                    verticalLineTo(5.0f)
                    lineTo(15.0f, 7.11f)
                    verticalLineTo(19.0f)
                    close()
                }
            }
        }
            .build()
        return _map!!
    }

private var _map: ImageVector? = null
