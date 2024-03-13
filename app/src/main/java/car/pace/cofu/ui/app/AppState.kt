package car.pace.cofu.ui.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.navigation.NavDestination
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import car.pace.cofu.ui.Graph
import car.pace.cofu.ui.Route
import car.pace.cofu.ui.navigate
import car.pace.cofu.util.LogAndBreadcrumb

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

    init {
        navController.addOnDestinationChangedListener { _, destination, _ ->
            val route = Route.fromRoute(destination.route)
            LogAndBreadcrumb.d("Navigation", "${route?.name ?: destination.route} gets displayed")
        }
    }

    val currentDestination: NavDestination?
        @Composable get() = navController.currentBackStackEntryAsState().value?.destination

    val currentRoute: Route?
        @Composable get() = Route.fromRoute(currentDestination?.route)

    val currentGraph: Graph?
        @Composable get() = currentRoute?.graph

    val shouldShowBottomBar: Boolean
        @Composable get() = currentRoute?.showBottomBar == true

    fun navigateToGraph(graph: Graph) {
        navController.navigate(graph.route) {
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

    fun navigateAndClearBackStack(graph: Graph) {
        navController.navigate(graph) {
            popUpTo(navController.graph.id) {
                inclusive = true
            }
        }
    }
}
