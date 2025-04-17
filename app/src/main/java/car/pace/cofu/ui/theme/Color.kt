package car.pace.cofu.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import car.pace.cofu.BuildConfig
import car.pace.cofu.util.LogAndBreadcrumb

val Primary = BuildConfig.PRIMARY_COLOR.toColor() ?: Color(0xFF00CCF0)
val Secondary = BuildConfig.SECONDARY_COLOR.toColor() ?: Color(0xFF00CCF0)
val OnPrimary = Color(0xFF232729)
val Surface = Color(0xFFEDF1F2)
val OnSurface = Color(0xFFA0AEB5)
val Background = Color(0xFFFFFFFF)
val Success = Color(0XFF76B532)
val Warning = Color(0xFFFF9601)
val Error = Color(0xFFE2001A)
val Shadow = Color(0x4D000000)
val ShadowLight = Color(0xFFECEFF0)
val PrimaryButtonText = Primary.contrastColor

val Color.contrastColor: Color
    get() {
        return if (luminance() > 0.49) OnPrimary else Background
    }

fun String.toColor(): Color? {
    return try {
        Color(android.graphics.Color.parseColor(this))
    } catch (e: Exception) {
        LogAndBreadcrumb.d("Color config", "Could not parse color string $this to Color object")
        null
    }
}
