package car.pace.cofu.ui.app

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import car.pace.cofu.ui.component.BottomBar
import car.pace.cofu.ui.component.TopBar
import car.pace.cofu.ui.navigation.AppNavHost
import car.pace.cofu.ui.theme.AppTheme
import car.pace.cofu.util.showSnackbar
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AppContent() {
    AppTheme {
        val appState = rememberAppState()
        val snackbarHostState = remember { SnackbarHostState() }

        Scaffold(
            topBar = {
                TopBar(
                    currentRoute = appState.currentRoute,
                    onNavigateUp = appState::navigateUp
                )
            },
            bottomBar = {
                if (appState.shouldShowBottomBar) {
                    BottomBar(
                        destinations = appState.bottomBarItems,
                        currentGraph = appState.currentGraph,
                        onNavigateToGraph = appState::navigateToGraph
                    )
                }
            },
            snackbarHost = {
                SnackbarHost(snackbarHostState)
            }
        ) { padding ->
            val context = LocalContext.current
            val coroutineScope = rememberCoroutineScope()

            AppNavHost(
                navController = appState.navController,
                modifier = Modifier
                    .fillMaxSize()
                    .run {
                        if (appState.shouldDrawBehindStatusBar) {
                            // No status bar inset to draw the content behind the status bar
                            windowInsetsPadding(WindowInsets.systemBars.exclude(WindowInsets.statusBars))
                        } else {
                            // Apply Scaffold padding to all sides
                            padding(padding)
                        }
                    }
                    .consumeWindowInsets(padding)
            ) {
                coroutineScope.launch {
                    it.showSnackbar(context, snackbarHostState)
                }
            }
        }
    }
}
