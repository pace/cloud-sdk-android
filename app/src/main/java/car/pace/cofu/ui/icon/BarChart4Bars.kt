package car.pace.cofu.ui.icon

import androidx.compose.material.icons.Icons
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType.Companion.NonZero
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap.Companion.Butt
import androidx.compose.ui.graphics.StrokeJoin.Companion.Miter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.ImageVector.Builder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val Icons.Outlined.BarChart4Bars: ImageVector
    get() {
        if (_barChart4Bars != null) {
            return _barChart4Bars!!
        }
        _barChart4Bars = Builder(
            name = "Outlined.BarChart4Bars",
            defaultWidth = 24.0.dp,
            defaultHeight = 24.0.dp,
            viewportWidth = 960.0f,
            viewportHeight = 960.0f
        ).apply {
            path(
                fill = SolidColor(Color(0xFF000000)),
                stroke = null,
                strokeLineWidth = 0.0f,
                strokeLineCap = Butt,
                strokeLineJoin = Miter,
                strokeLineMiter = 4.0f,
                pathFillType = NonZero
            ) {
                moveTo(80.0f, 840.0f)
                verticalLineToRelative(-80.0f)
                horizontalLineToRelative(800.0f)
                verticalLineToRelative(80.0f)
                lineTo(80.0f, 840.0f)
                close()
                moveTo(120.0f, 720.0f)
                verticalLineToRelative(-280.0f)
                horizontalLineToRelative(120.0f)
                verticalLineToRelative(280.0f)
                lineTo(120.0f, 720.0f)
                close()
                moveTo(320.0f, 720.0f)
                verticalLineToRelative(-480.0f)
                horizontalLineToRelative(120.0f)
                verticalLineToRelative(480.0f)
                lineTo(320.0f, 720.0f)
                close()
                moveTo(520.0f, 720.0f)
                verticalLineToRelative(-360.0f)
                horizontalLineToRelative(120.0f)
                verticalLineToRelative(360.0f)
                lineTo(520.0f, 720.0f)
                close()
                moveTo(720.0f, 720.0f)
                verticalLineToRelative(-600.0f)
                horizontalLineToRelative(120.0f)
                verticalLineToRelative(600.0f)
                lineTo(720.0f, 720.0f)
                close()
            }
        }
            .build()
        return _barChart4Bars!!
    }

private var _barChart4Bars: ImageVector? = null
