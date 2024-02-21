package car.pace.cofu.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import car.pace.cofu.BuildConfig
import car.pace.cofu.ui.navigation.graph.Graph
import car.pace.cofu.ui.navigation.graph.legalUpdateGraph
import car.pace.cofu.ui.navigation.graph.listGraph
import car.pace.cofu.ui.navigation.graph.mapGraph
import car.pace.cofu.ui.navigation.graph.moreGraph
import car.pace.cofu.ui.navigation.graph.navigate
import car.pace.cofu.ui.navigation.graph.onboardingGraph
import car.pace.cofu.ui.navigation.graph.walletGraph
import car.pace.cofu.util.Constants.TRANSITION_DURATION

@Composable
fun AppNavHost(
    navController: NavHostController,
    startDestination: Graph,
    modifier: Modifier = Modifier,
    onOnboardingDone: () -> Unit,
    onLegalUpdateDone: () -> Unit,
    navigateToOnboarding: () -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = startDestination.route,
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

        legalUpdateGraph(
            onNavigate = {
                navController.navigate(it)
            },
            onNavigateUp = {
                navController.navigateUp()
            },
            onDone = {
                onLegalUpdateDone()
            }
        )

        listGraph(
            onNavigate = {
                navController.navigate(it)
            },
            onNavigateUp = {
                navController.navigateUp()
            }
        )

        if (BuildConfig.MAP_ENABLED) {
            mapGraph(
                onNavigate = {
                    navController.navigate(it)
                },
                onNavigateUp = {
                    navController.navigateUp()
                }
            )
        }

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
