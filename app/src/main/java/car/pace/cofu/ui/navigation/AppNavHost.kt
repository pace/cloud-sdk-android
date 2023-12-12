package car.pace.cofu.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import car.pace.cofu.ui.animation.ScaleTransitionDirection
import car.pace.cofu.ui.animation.scaleIntoContainer
import car.pace.cofu.ui.animation.scaleOutOfContainer
import car.pace.cofu.ui.navigation.graph.Graph
import car.pace.cofu.ui.navigation.graph.Route
import car.pace.cofu.ui.navigation.graph.homeGraph
import car.pace.cofu.ui.navigation.graph.moreGraph
import car.pace.cofu.ui.navigation.graph.navigate
import car.pace.cofu.ui.navigation.graph.onboardingGraph
import car.pace.cofu.ui.navigation.graph.walletGraph
import car.pace.cofu.util.SnackbarData

@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    viewModel: AppNavHostViewModel = hiltViewModel(),
    showSnackbar: (SnackbarData) -> Unit
) {
    val onboardingDone by viewModel.onboardingDone.collectAsStateWithLifecycle()

    NavHost(
        navController = navController,
        startDestination = if (onboardingDone) Graph.HOME.route else Route.ONBOARDING.route,
        modifier = modifier,
        enterTransition = {
            scaleIntoContainer()
        },
        exitTransition = {
            scaleOutOfContainer(direction = ScaleTransitionDirection.INWARDS)
        },
        popEnterTransition = {
            scaleIntoContainer(direction = ScaleTransitionDirection.OUTWARDS)
        },
        popExitTransition = {
            scaleOutOfContainer()
        }
    ) {
        onboardingGraph(
            onNavigate = {
                navController.navigate(it)
            },
            onDone = {
                viewModel.onboardingDone(it)
                navController.navigate(Route.HOME)
            }
        )
        homeGraph(showSnackbar = showSnackbar) {
            navController.navigate(it)
        }
        walletGraph(showSnackbar = showSnackbar) {
            navController.navigate(it)
        }
        moreGraph {
            navController.navigate(it)
        }
    }
}
