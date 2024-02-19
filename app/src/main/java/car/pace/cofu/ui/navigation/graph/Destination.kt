package car.pace.cofu.ui.navigation.graph

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Domain
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.List
import androidx.compose.material.icons.outlined.LocalGasStation
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.PersonRemove
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.VpnKey
import androidx.compose.ui.graphics.vector.ImageVector
import car.pace.cofu.R
import car.pace.cofu.ui.icon.BarChart4Bars
import car.pace.cofu.ui.icon.DeveloperGuide
import car.pace.cofu.ui.icon.Signature
import car.pace.cofu.ui.icon.TwoPager

val bottomBarGraphs = listOf(Graph.HOME, Graph.WALLET, Graph.MORE)
val bottomBarRoutes = listOf(Route.HOME.route, Route.WALLET.route, Route.MORE.route)

enum class Graph(
    val route: String,
    val icon: ImageVector?,
    @StringRes val labelRes: Int?
) {
    ONBOARDING(
        "onboarding_graph",
        null,
        null
    ),
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
    val graph: Graph,
    val showBottomBar: Boolean = false,
    val icon: ImageVector? = null,
    @StringRes val labelRes: Int? = null
) {
    ONBOARDING(route = "onboarding_route", graph = Graph.ONBOARDING),
    ONBOARDING_TERMS(route = "onboarding_terms_route", graph = Graph.ONBOARDING),
    ONBOARDING_PRIVACY(route = "onboarding_privacy_route", graph = Graph.ONBOARDING),
    ONBOARDING_ANALYSIS(route = "onboarding_analysis_route", graph = Graph.ONBOARDING),
    HOME(route = "home_route", graph = Graph.HOME, showBottomBar = true),
    DETAIL(route = "detail_route", graph = Graph.HOME),
    WALLET(route = "wallet_route", graph = Graph.WALLET, showBottomBar = true),
    PAYMENT_METHODS(route = "payment_methods_route", graph = Graph.WALLET, showBottomBar = true, icon = Icons.Outlined.AccountBalanceWallet, labelRes = R.string.wallet_payment_methods_title),
    TRANSACTIONS(route = "transactions_route", graph = Graph.WALLET, icon = Icons.Outlined.ReceiptLong, labelRes = R.string.wallet_transactions_title),
    FUEL_TYPE(route = "fuelType_route", graph = Graph.WALLET, showBottomBar = true, icon = Icons.Outlined.LocalGasStation, labelRes = R.string.wallet_fuel_type_selection_title),
    MORE(route = "more_route", graph = Graph.MORE, showBottomBar = true),
    TERMS(route = "terms_route", graph = Graph.MORE, showBottomBar = true, labelRes = R.string.MENU_ITEMS_TERMS, icon = Icons.Outlined.DeveloperGuide),
    PRIVACY(route = "privacy_route", graph = Graph.MORE, showBottomBar = true, labelRes = R.string.MENU_ITEMS_PRIVACY, icon = Icons.Outlined.Lock),
    TRACKING(route = "tracking_route", graph = Graph.MORE, showBottomBar = true, labelRes = R.string.menu_items_analytics, icon = Icons.Outlined.BarChart4Bars),
    ANALYSIS(route = "analysis_route", graph = Graph.MORE, showBottomBar = true),
    IMPRINT(route = "imprint_route", graph = Graph.MORE, showBottomBar = true, labelRes = R.string.MENU_ITEMS_IMPRINT, icon = Icons.Outlined.Domain),
    LICENSES(route = "licenses_route", graph = Graph.MORE, showBottomBar = true, labelRes = R.string.MENU_ITEMS_LICENCES, icon = Icons.Outlined.TwoPager),
    WEBSITE(route = "website_route", graph = Graph.MORE, icon = Icons.Outlined.Language),
    AUTHORIZATION(route = "authorization_route", graph = Graph.WALLET, showBottomBar = true, icon = Icons.Outlined.Signature, labelRes = R.string.wallet_two_factor_authentication_title),
    DELETE_ACCOUNT("delete_account", Graph.WALLET, showBottomBar = true, icon = Icons.Outlined.PersonRemove, labelRes = R.string.wallet_account_deletion_title),
    PERMISSIONS(route = "permissions_route", graph = Graph.MORE, showBottomBar = true, labelRes = R.string.menu_items_permissions, icon = Icons.Outlined.VpnKey);
    companion object {

        fun fromRoute(route: String?): Route? {
            return route?.let { value ->
                entries.find { value.contains(it.route) }
            }
        }
    }
}
