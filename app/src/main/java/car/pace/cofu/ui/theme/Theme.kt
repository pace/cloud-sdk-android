package car.pace.cofu.ui.theme

import androidx.compose.material.ripple.RippleAlpha
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RippleConfiguration
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        typography = Typography
    ) {
        CompositionLocalProvider(
            LocalRippleConfiguration provides appRippleConfiguration(MaterialTheme.colorScheme.primary),
            content = content
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
fun appRippleConfiguration(color: Color) = RippleConfiguration(
    color = color,
    rippleAlpha = RippleAlpha(
        pressedAlpha = 0.25f,
        focusedAlpha = 0.24f,
        draggedAlpha = 0.16f,
        hoveredAlpha = 0.08f
    )
)
