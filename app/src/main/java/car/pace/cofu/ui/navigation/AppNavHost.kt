package car.pace.cofu.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import car.pace.cofu.ui.app.AppState
import car.pace.cofu.ui.navigation.graph.Destination
import car.pace.cofu.ui.navigation.graph.homeGraph
import car.pace.cofu.ui.navigation.graph.moreGraph
import car.pace.cofu.ui.navigation.graph.onboardingGraph
import car.pace.cofu.ui.navigation.graph.walletGraph
import car.pace.cofu.util.SnackbarData

@Composable
fun AppNavHost(
    appState: AppState,
    modifier: Modifier = Modifier,
    viewModel: AppNavHostViewModel = hiltViewModel(),
    showSnackbar: (SnackbarData) -> Unit
) {
    val navController = appState.navController
    val onboardingDone by viewModel.onboardingDone.collectAsStateWithLifecycle()

    NavHost(
        navController = navController,
        startDestination = if (onboardingDone) Destination.Home.route else Destination.Onboarding.route,
        modifier = modifier
    ) {
        onboardingGraph(showSnackbar = showSnackbar) {
            viewModel.onboardingDone()
            navController.navigate(Destination.Home.route)
        }
        homeGraph(showSnackbar = showSnackbar)
        walletGraph {
            navController.navigate(it)
        }
        moreGraph()
    }
}
