package car.pace.cofu.ui.navigation.graph

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.List
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.ui.graphics.vector.ImageVector
import car.pace.cofu.R

enum class BottomBarItem(
    val route: String,
    val icon: ImageVector,
    @StringRes val labelRes: Int
) {
    HOME(
        Destination.Home.List.route,
        Icons.Outlined.List,
        R.string.list_tab_label
    ),
    WALLET(
        Destination.Wallet.List.route,
        Icons.Outlined.AccountBalanceWallet,
        R.string.wallet_tab_label
    ),
    MORE(
        Destination.More.List.route,
        Icons.Outlined.MoreHoriz,
        R.string.more_tab_label
    )
}
