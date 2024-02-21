package car.pace.cofu.ui.app

import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AppContent(
    viewModel: AppContentViewModel = hiltViewModel()
) {
    AppTheme {
        val startDestination by viewModel.startDestination.collectAsStateWithLifecycle()
        val context = LocalContext.current
        val appState = rememberAppState()
        val coroutineScope = rememberCoroutineScope()

        LifecycleEventEffect(event = Lifecycle.Event.ON_RESUME) {
            coroutineScope.launch {
                val activity = context.findActivity<AppCompatActivity>()
                if (viewModel.isReLoginNeeded(activity)) {
                    appState.navigateAndClearBackStack(Graph.ONBOARDING)
                }
            }
        }

        LifecycleEventEffect(event = Lifecycle.Event.ON_CREATE) {
            viewModel.onAppStart()
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
            }
        ) { padding ->
            AppNavHost(
                navController = appState.navController,
                startDestination = startDestination,
                modifier = Modifier
                    .padding(padding)
                    .consumeWindowInsets(padding)
                    .fillMaxSize(),
                onOnboardingDone = {
                    viewModel.onOnboardingDone()
                    appState.navigateAndClearBackStack(Graph.LIST)
                },
                onLegalUpdateDone = {
                    appState.navigateAndClearBackStack(Graph.LIST)
                },
                navigateToOnboarding = {
                    appState.navigateAndClearBackStack(Graph.ONBOARDING)
                }
            )
        }
    }
}
