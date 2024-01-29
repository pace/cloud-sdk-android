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

val Icons.Outlined.StreamApps: ImageVector
    get() {
        if (_streamApps != null) {
            return _streamApps!!
        }
        _streamApps = Builder(
            name = "Outlined.StreamApps",
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
                moveTo(640.0f, 520.0f)
                horizontalLineToRelative(200.0f)
                verticalLineToRelative(-120.0f)
                lineTo(640.0f, 400.0f)
                verticalLineToRelative(120.0f)
                close()
                moveTo(560.0f, 680.0f)
                verticalLineToRelative(-320.0f)
                quadToRelative(0.0f, -17.0f, 11.5f, -28.5f)
                reflectiveQuadTo(600.0f, 320.0f)
                horizontalLineToRelative(280.0f)
                quadToRelative(17.0f, 0.0f, 28.5f, 11.5f)
                reflectiveQuadTo(920.0f, 360.0f)
                verticalLineToRelative(200.0f)
                quadToRelative(0.0f, 17.0f, -11.5f, 28.5f)
                reflectiveQuadTo(880.0f, 600.0f)
                lineTo(640.0f, 600.0f)
                lineToRelative(-80.0f, 80.0f)
                close()
                moveTo(280.0f, 920.0f)
                quadToRelative(-33.0f, 0.0f, -56.5f, -23.5f)
                reflectiveQuadTo(200.0f, 840.0f)
                verticalLineToRelative(-720.0f)
                quadToRelative(0.0f, -33.0f, 23.5f, -56.5f)
                reflectiveQuadTo(280.0f, 40.0f)
                horizontalLineToRelative(400.0f)
                quadToRelative(33.0f, 0.0f, 56.5f, 23.5f)
                reflectiveQuadTo(760.0f, 120.0f)
                verticalLineToRelative(160.0f)
                horizontalLineToRelative(-80.0f)
                verticalLineToRelative(-40.0f)
                lineTo(280.0f, 240.0f)
                verticalLineToRelative(480.0f)
                horizontalLineToRelative(400.0f)
                verticalLineToRelative(-40.0f)
                horizontalLineToRelative(80.0f)
                verticalLineToRelative(160.0f)
                quadToRelative(0.0f, 33.0f, -23.5f, 56.5f)
                reflectiveQuadTo(680.0f, 920.0f)
                lineTo(280.0f, 920.0f)
                close()
                moveTo(280.0f, 800.0f)
                verticalLineToRelative(40.0f)
                horizontalLineToRelative(400.0f)
                verticalLineToRelative(-40.0f)
                lineTo(280.0f, 800.0f)
                close()
                moveTo(280.0f, 160.0f)
                horizontalLineToRelative(400.0f)
                verticalLineToRelative(-40.0f)
                lineTo(280.0f, 120.0f)
                verticalLineToRelative(40.0f)
                close()
                moveTo(280.0f, 160.0f)
                verticalLineToRelative(-40.0f)
                verticalLineToRelative(40.0f)
                close()
                moveTo(280.0f, 800.0f)
                verticalLineToRelative(40.0f)
                verticalLineToRelative(-40.0f)
                close()
                moveTo(640.0f, 520.0f)
                verticalLineToRelative(-120.0f)
                verticalLineToRelative(120.0f)
                close()
            }
        }
            .build()
        return _streamApps!!
    }

private var _streamApps: ImageVector? = null
