package car.pace.cofu.ui.more

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.ViewModel
import car.pace.cofu.DataSource
import car.pace.cofu.MenuEntries
import car.pace.cofu.ui.Route
import car.pace.cofu.util.BuildProvider
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
        val dataSource: DataSource? = null
    )

    val items = buildList {
        addAll(
            listOf(
                Route.TERMS.toMoreItem(),
                Route.PRIVACY.toMoreItem()
            )
        )

        if (BuildProvider.isAnalyticsEnabled()) {
            add(Route.TRACKING.toMoreItem())
        }

        add(Route.PERMISSIONS.toMoreItem())

        addAll(
            listOf(
                Route.IMPRINT.toMoreItem(),
                Route.LICENSES.toMoreItem()
            )
        )

        addAll(
            MenuEntries.entries.map {
                Route.WEB_CONTENT.toMoreItem(it.key, it.value)
            }
        )
    }

    private fun Route.toMoreItem(
        @StringRes labelRes: Int? = null,
        dataSource: DataSource? = null
    ) = MoreItem(
        route = this,
        icon = this.icon,
        labelRes = labelRes ?: this.labelRes,
        dataSource = dataSource
    )
}
