package car.pace.cofu.ui.app

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
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
                            // Apply Scaffold padding to all sides except top
                            val layoutDirection = LocalLayoutDirection.current
                            padding(
                                PaddingValues(
                                    start = padding.calculateStartPadding(layoutDirection),
                                    end = padding.calculateEndPadding(layoutDirection),
                                    bottom = padding.calculateBottomPadding()
                                )
                            )
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
