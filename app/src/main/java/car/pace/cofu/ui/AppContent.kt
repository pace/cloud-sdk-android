package car.pace.cofu.ui

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import car.pace.cofu.ui.theme.AppTheme
import car.pace.cofu.util.showSnackbar
import kotlinx.coroutines.launch

@Composable
fun AppContent() {
    AppTheme {
        val snackbarHostState = remember { SnackbarHostState() }
        val context = LocalContext.current
        val coroutineScope = rememberCoroutineScope()

        AppNavigation(
            snackbarHostState = snackbarHostState,
        ) {
            coroutineScope.launch {
                it.showSnackbar(context, snackbarHostState)
            }
        }
    }
}
