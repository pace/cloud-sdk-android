package cloud.pace.sdk.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColors = darkColors(
    primary = PACEBlue,
    primaryVariant = LightPACEBlue,
    secondary = DarkGray,
    secondaryVariant = LightGray,
    background = Title,
    surface = Title,
    error = Error,
    onPrimary = Title,
    onSecondary = Subtitle,
    onBackground = Color.White,
    onSurface = Color.White
)

private val LightColors = lightColors(
    primary = PACEBlue,
    primaryVariant = LightPACEBlue,
    secondary = LightGray,
    secondaryVariant = DarkGray,
    background = Color.White,
    surface = Color.White,
    error = Error,
    onPrimary = Title,
    onSecondary = Subtitle,
    onBackground = Title,
    onSurface = Title
)

@Composable
fun PACETheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colors = if (darkTheme) DarkColors else LightColors,
        typography = Typography,
        content = content
    )
}
