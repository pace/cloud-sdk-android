package car.pace.cofu.ui.theme

import androidx.compose.ui.graphics.Color
import car.pace.cofu.BuildConfig

val Primary = BuildConfig.PRIMARY_COLOR.toColor()
val Secondary = BuildConfig.SECONDARY_COLOR.toColor()
val OnPrimary = Color(0xFF232729)
val Surface = Color(0xFFFFFFFF)
val OnSurface = Color(0xFF000000)
val OnSurfaceVariant = Color(0xFFA0AEB5)
val Success = Color(0XFF76B532)
val Warning = Color(0xFFFF9601)
val Error = Color(0xFFE2001A)
val Shadow = Color(0x4D000000)

fun String.toColor() = Color(android.graphics.Color.parseColor(this))
