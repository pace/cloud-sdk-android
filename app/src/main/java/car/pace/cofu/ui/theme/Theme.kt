package car.pace.cofu.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val LightColors = lightColorScheme(
    primary = PACEBlue,
    onPrimary = Title,
    secondary = LightGray,
    onSecondary = Subtitle,
    background = Color.White,
    onBackground = Title,
    surface = Color.White,
    onSurface = Title,
    error = Error
)

val DarkColors = darkColorScheme(
    primary = PACEBlue,
    onPrimary = Title,
    secondary = DarkGray,
    onSecondary = Subtitle,
    background = Title,
    onBackground = Color.White,
    surface = Title,
    onSurface = Color.White,
    error = Error
)

@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = Typography,
        content = content
    )
}
