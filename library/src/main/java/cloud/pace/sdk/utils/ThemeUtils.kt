package cloud.pace.sdk.utils

import androidx.appcompat.app.AppCompatDelegate

object ThemeUtils {
    fun getTheme(): Theme {
        return when (AppCompatDelegate.getDefaultNightMode()) {
            AppCompatDelegate.MODE_NIGHT_YES -> Theme.DARK
            AppCompatDelegate.MODE_NIGHT_NO -> Theme.LIGHT
            AppCompatDelegate.MODE_NIGHT_UNSPECIFIED -> Theme.LIGHT
            else -> Theme.LIGHT
        }
    }
}
