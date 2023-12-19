package car.pace.cofu.util.menu

import androidx.annotation.StringRes

data class MenuItem(
    @StringRes val label: Int,
    val url: String
)
