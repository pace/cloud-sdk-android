package car.pace.cofu.ui

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import car.pace.cofu.BuildConfig
import car.pace.cofu.util.Constants.TRANSITION_DURATION

@Composable
fun AppNavHost(
    navController: NavHostController,
    startDestination: Graph,
    modifier: Modifier = Modifier,
    onOnboardingDone: () -> Unit,
    onConsentDone: () -> Unit,
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

        consentGraph(
            onNavigate = {
                navController.navigate(it)
            },
            onNavigateUp = {
                navController.navigateUp()
            },
            onDone = {
                onConsentDone()
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
