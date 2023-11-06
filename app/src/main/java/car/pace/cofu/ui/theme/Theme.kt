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
    primary = PACEBlue,
    onPrimary = Color.White,
    secondary = LightGray,
    onSecondary = Color.Black,
    background = Color.White,
    onBackground = Color.Black,
    surface = Color.White,
    onSurface = Color.Black,
    surfaceVariant = LightGray.copy(alpha = 0.2f),
    error = Error
)

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        typography = Typography
    ) {
        CompositionLocalProvider(
            LocalRippleTheme provides AppRippleTheme,
            content = content
        )
    }
}

private object AppRippleTheme : RippleTheme {

    @Composable
    override fun defaultColor(): Color {
        return MaterialTheme.colorScheme.primary
    }

    @Composable
    override fun rippleAlpha(): RippleAlpha {
        return RippleAlpha(
            pressedAlpha = 0.34f,
            focusedAlpha = 0.34f,
            draggedAlpha = 0.2f,
            hoveredAlpha = 0.1f
        )
    }
}
