package car.pace.cofu.ui.more

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.ViewModel
import car.pace.cofu.MenuEntries
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
        @StringRes val urlRes: Int? = null
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

        addAll(
            MenuEntries.entries.map {
                Route.WEBSITE.toMoreItem(it.key, it.value)
            }
        )
    }

    private fun Route.toMoreItem(
        @StringRes labelRes: Int? = null,
        @StringRes urlRes: Int? = null
    ) = MoreItem(
        route = this,
        icon = this.icon,
        labelRes = labelRes ?: this.labelRes,
        urlRes = urlRes
    )
}
