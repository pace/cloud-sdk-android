package cloud.pace.sdk.app.ui.theme

import androidx.annotation.DrawableRes
import cloud.pace.sdk.app.R

sealed class Screen(val route: String, val title: String, @DrawableRes val icon: Int) {
    object List : Screen(route = "list", title = "ListScreen", icon = R.drawable.ic_baseline_list_24)
    object Dashboard : Screen(route = "dashboard", title = "Dashboard", icon = R.drawable.ic_baseline_dashboard_24)
    object Drawer : Screen(route = "drawer", title = "Drawer", icon = R.drawable.icon_drawer)
    object Settings : Screen(route = "settings", title = "Settings", icon = R.drawable.ic_baseline_settings_24)
}
