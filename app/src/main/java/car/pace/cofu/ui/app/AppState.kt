package car.pace.cofu.ui.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.navigation.NavDestination
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import car.pace.cofu.ui.navigation.graph.BottomBarItem

@Composable
fun rememberAppState(
    navController: NavHostController = rememberNavController()
) = remember(navController) {
    AppState(navController)
}

@Stable
class AppState(
    val navController: NavHostController
) {
    val bottomBarItems = BottomBarItem.values()
    private val bottomBarItemRoutes = bottomBarItems.map(BottomBarItem::route)

    val currentDestination: NavDestination?
        @Composable get() = navController.currentBackStackEntryAsState().value?.destination

    val shouldShowBottomBar: Boolean
        @Composable get() = currentDestination?.route in bottomBarItemRoutes

    /**
     * UI logic for navigating to a bottom bar item in the app. Bottom bar items have
     * only one copy of the destination in the back stack, and save and restore state whenever you
     * navigate to and from it.
     *
     * @param bottomBarItem: The bottom bar item the app needs to navigate to.
     */
    fun navigateToBottomBarItem(bottomBarItem: BottomBarItem) {
        navController.navigate(bottomBarItem.route) {
            // Pop up to the start destination of the graph to
            // avoid building up a large stack of destinations
            // on the back stack as users select items
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            // Avoid multiple copies of the same destination when
            // reselecting the same item
            launchSingleTop = true
            // Restore state when reselecting a previously selected item
            restoreState = true
        }
    }
}
