package car.pace.cofu.ui.more

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.ViewModel
import car.pace.cofu.R
import car.pace.cofu.ui.navigation.graph.Route
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class MoreViewModel @Inject constructor() : ViewModel() {

    data class MoreItem(
        val id: String = UUID.randomUUID().toString(),
        val route: Route,
        val icon: ImageVector?,
        @StringRes val labelRes: Int?,
        val url: String? = null
    )

    val items = buildList {
        addAll(
            listOf(
                Route.TERMS.toMoreItem(),
                Route.PRIVACY.toMoreItem(),
                Route.IMPRINT.toMoreItem(),
                Route.LICENSES.toMoreItem()
            )
        )

        // TODO: Add dynamic WEBSITE routes from config. Because the menu label needs to be localized, specify a string resource. The menu item also needs an URL to open in the WebViewScreen e.g.:
        add(
            Route.WEBSITE.toMoreItem(R.string.MENU_ITEMS_CONTACT, "https://www.pace.car/de/drive/")
        )
    }

    private fun Route.toMoreItem(
        @StringRes labelRes: Int? = null,
        url: String? = null
    ) = MoreItem(
        route = this,
        icon = this.icon,
        labelRes = labelRes ?: this.labelRes,
        url = url
    )
}
