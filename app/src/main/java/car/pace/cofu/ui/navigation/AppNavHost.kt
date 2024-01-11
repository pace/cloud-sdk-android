package car.pace.cofu.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import car.pace.cofu.ui.navigation.graph.Graph
import car.pace.cofu.ui.navigation.graph.homeGraph
import car.pace.cofu.ui.navigation.graph.moreGraph
import car.pace.cofu.ui.navigation.graph.navigate
import car.pace.cofu.ui.navigation.graph.onboardingGraph
import car.pace.cofu.ui.navigation.graph.walletGraph
import car.pace.cofu.util.Constants.TRANSITION_DURATION
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
        startDestination = if (onboardingDone) Graph.HOME.route else Graph.ONBOARDING.route,
        modifier = modifier,
        enterTransition = {
            fadeIn(animationSpec = tween(TRANSITION_DURATION))
        },
        exitTransition = {
            fadeOut(animationSpec = tween(TRANSITION_DURATION))
        }
    ) {
        onboardingGraph(
            onNavigate = {
                navController.navigate(it)
            },
            onNavigateUp = {
                navController.navigateUp()
            },
            onDone = {
                viewModel.onboardingDone(it)
                navController.navigate(Graph.HOME) {
                    popUpTo(navController.graph.id) {
                        inclusive = true
                    }
                }
            }
        )
        homeGraph(
            onNavigate = {
                navController.navigate(it)
            },
            onNavigateUp = {
                navController.navigateUp()
            }
        )
        walletGraph(
            showSnackbar = showSnackbar,
            onNavigate = {
                navController.navigate(it)
            },
            onNavigateUp = {
                navController.navigateUp()
            },
            onLogout = {
                navController.navigate(Graph.ONBOARDING) {
                    popUpTo(navController.graph.id) {
                        inclusive = true
                    }
                }
            }
        )
        moreGraph(
            onNavigate = {
                navController.navigate(it)
            },
            onNavigateUp = {
                navController.navigateUp()
            }
        )
    }
}
