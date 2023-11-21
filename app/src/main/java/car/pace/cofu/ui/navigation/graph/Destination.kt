package car.pace.cofu.ui.navigation.graph

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.List
import androidx.compose.material.icons.outlined.LocalGasStation
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.ui.graphics.vector.ImageVector
import car.pace.cofu.R

enum class Graph(
    val route: String,
    val icon: ImageVector,
    @StringRes val labelRes: Int
) {
    HOME(
        "home_graph",
        Icons.Outlined.List,
        R.string.list_tab_label
    ),
    WALLET(
        "wallet_graph",
        Icons.Outlined.AccountBalanceWallet,
        R.string.wallet_tab_label
    ),
    MORE(
        "more_graph",
        Icons.Outlined.MoreHoriz,
        R.string.more_tab_label
    )
}

enum class Route(
    val route: String,
    val graph: Graph?,
    val showBottomBar: Boolean,
    val icon: ImageVector? = null,
    @StringRes val labelRes: Int? = null
) {
    ONBOARDING("onboarding_route", null, false),
    HOME("home_route", Graph.HOME, true),
    DETAIL("detail_route", null, false),
    WALLET("wallet_route", Graph.WALLET, true),
    METHODS("methods_route", Graph.WALLET, true, Icons.Outlined.AccountBalanceWallet, R.string.payment_methods_title),
    TRANSACTIONS("transactions_route", Graph.WALLET, false, Icons.Outlined.ReceiptLong, R.string.transactions_title),
    FUEL_TYPE("fuelType_route", Graph.WALLET, true, Icons.Outlined.LocalGasStation, R.string.fuel_selection_title),
    MORE("more_route", Graph.MORE, true),
    TERMS("terms_route", Graph.MORE, true),
    PRIVACY("privacy_route", Graph.MORE, true),
    CONTACT("contact_route", Graph.MORE, true),
    IMPRINT("imprint_route", Graph.MORE, true),
    LIBRARIES("libraries_route", Graph.MORE, false);

    companion object {

        fun fromRoute(route: String?): Route? {
            return route?.let { value ->
                Route.values().find { value.contains(it.route) }
            }
        }
    }
}
