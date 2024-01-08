package car.pace.cofu.ui.theme

import androidx.compose.ui.graphics.Color
import car.pace.cofu.BuildConfig
import timber.log.Timber

val Primary = BuildConfig.PRIMARY_COLOR.toColor() ?: Color(0xFF00CCF0)
val Secondary = BuildConfig.SECONDARY_COLOR.toColor() ?: Color(0xFF00CCF0)
val OnPrimary = Color(0xFF232729)
val Surface = Color(0xFFEDF1F2)
val OnSurface = Color(0xFFA0AEB5)
val Background = Color(0xFFFFFFFF)
val Success = Color(0XFF76B532)
val Warning = Color(0xFFFF9601)
val Error = Color(0xFFE2001A)

fun String.toColor(): Color? {
    return try {
        Color(android.graphics.Color.parseColor(this))
    } catch (e: Exception) {
        Timber.d("Could not parse color string $this to Color object")
        null
    }
}
