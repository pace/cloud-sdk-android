package car.pace.cofu.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import car.pace.cofu.ui.navigation.graph.Graph
import car.pace.cofu.ui.navigation.graph.homeGraph
import car.pace.cofu.ui.navigation.graph.moreGraph
import car.pace.cofu.ui.navigation.graph.navigate
import car.pace.cofu.ui.navigation.graph.onboardingGraph
import car.pace.cofu.ui.navigation.graph.walletGraph
import car.pace.cofu.util.Constants.TRANSITION_DURATION

@Composable
fun AppNavHost(
    navController: NavHostController,
    onboardingDone: Boolean,
    modifier: Modifier = Modifier,
    onOnboardingDone: () -> Unit,
    navigateToOnboarding: () -> Unit
) {
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
                onOnboardingDone()
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
            onNavigate = {
                navController.navigate(it)
            },
            onNavigateUp = {
                navController.navigateUp()
            },
            onLogout = {
                navigateToOnboarding()
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
