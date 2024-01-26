package car.pace.cofu.ui.app

import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import car.pace.cofu.ui.component.BottomBar
import car.pace.cofu.ui.navigation.AppNavHost
import car.pace.cofu.ui.navigation.graph.Graph
import car.pace.cofu.ui.navigation.graph.bottomBarGraphs
import car.pace.cofu.ui.onboarding.twofactor.biometric.findActivity
import car.pace.cofu.ui.theme.AppTheme
import car.pace.cofu.util.showSnackbar
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AppContent(
    viewModel: AppContentViewModel = hiltViewModel()
) {
    AppTheme {
        val onboardingDone by viewModel.onboardingDone.collectAsStateWithLifecycle()
        val context = LocalContext.current
        val appState = rememberAppState()
        val coroutineScope = rememberCoroutineScope()
        val snackbarHostState = remember { SnackbarHostState() }

        LifecycleEventEffect(event = Lifecycle.Event.ON_RESUME) {
            coroutineScope.launch {
                val activity = context.findActivity<AppCompatActivity>()
                if (viewModel.isReLoginNeeded(activity)) {
                    appState.navigateAndClearBackStack(Graph.ONBOARDING)
                }
            }
        }

        Scaffold(
            topBar = {
                Box(modifier = Modifier)
            },
            bottomBar = {
                if (appState.shouldShowBottomBar) {
                    BottomBar(
                        destinations = bottomBarGraphs,
                        currentGraph = appState.currentGraph,
                        onNavigateToGraph = appState::navigateToGraph
                    )
                }
            },
            snackbarHost = {
                SnackbarHost(snackbarHostState)
            }
        ) { padding ->
            AppNavHost(
                navController = appState.navController,
                onboardingDone = onboardingDone,
                modifier = Modifier
                    .padding(padding)
                    .consumeWindowInsets(padding)
                    .fillMaxSize(),
                onOnboardingDone = {
                    viewModel.onOnboardingDone(it)
                    appState.navigateAndClearBackStack(Graph.HOME)
                },
                navigateToOnboarding = {
                    appState.navigateAndClearBackStack(Graph.ONBOARDING)
                }
            ) {
                coroutineScope.launch {
                    it.showSnackbar(context, snackbarHostState)
                }
            }
        }
    }
}
