package car.pace.cofu.ui.theme

import androidx.compose.material.ripple.LocalRippleTheme
import androidx.compose.material.ripple.RippleAlpha
import androidx.compose.material.ripple.RippleTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color

val LightColors = lightColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,
    primaryContainer = Background,
    secondary = Secondary,
    onSecondary = Primary,
    background = Background,
    onBackground = OnPrimary,
    surface = Surface,
    onSurface = OnSurface,
    error = Error,
    outlineVariant = Surface
)

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        typography = Typography
    ) {
        CompositionLocalProvider(
            LocalRippleTheme provides AppRippleTheme(),
            content = content
        )
    }
}

class AppRippleTheme(val color: Color? = null) : RippleTheme {

    @Composable
    override fun defaultColor(): Color {
        return color ?: MaterialTheme.colorScheme.primary
    }

    @Composable
    override fun rippleAlpha(): RippleAlpha {
        return RippleAlpha(
            pressedAlpha = 0.25f,
            focusedAlpha = 0.24f,
            draggedAlpha = 0.16f,
            hoveredAlpha = 0.08f
        )
    }
}
