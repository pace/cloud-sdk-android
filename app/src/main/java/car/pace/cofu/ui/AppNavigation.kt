package car.pace.cofu.ui

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import car.pace.cofu.ui.home.Home
import car.pace.cofu.ui.onboarding.Onboarding
import car.pace.cofu.ui.theme.AppTheme
import car.pace.cofu.util.SnackbarData

object Destinations {
    const val ONBOARDING_ROUTE = "onboarding"
    const val HOME_ROUTE = "home"
    const val FUEL_TYPE_ROUTE = "fuelType"
    const val IMPRINT_ROUTE = "imprint"
    const val PRIVACY_POLICY_ROUTE = "privacyPolicy"
    const val LICENSES_ROUTE = "licenses"
}

@Composable
fun AppNavigation(
    viewModel: AppContentViewModel = hiltViewModel(),
    snackbarHostState: SnackbarHostState,
    showSnackbar: (SnackbarData) -> Unit
) {
    AppTheme {
        val navController = rememberNavController()
        val onboardingDone by viewModel.onboardingDone.collectAsStateWithLifecycle()

        NavHost(
            navController = navController,
            startDestination = if (onboardingDone) Destinations.HOME_ROUTE else Destinations.ONBOARDING_ROUTE
        ) {
            composable(Destinations.ONBOARDING_ROUTE) {
                Onboarding(
                    snackbarHostState = snackbarHostState,
                    showSnackbar = showSnackbar
                ) {
                    viewModel.onboardingDone()
                    navController.navigate(Destinations.HOME_ROUTE)
                }
            }
            composable(Destinations.HOME_ROUTE) {
                Home(
                    snackbarHostState = snackbarHostState,
                    showSnackbar = showSnackbar
                )
            }
            composable(Destinations.FUEL_TYPE_ROUTE) {}
            composable(Destinations.IMPRINT_ROUTE) {}
            composable(Destinations.PRIVACY_POLICY_ROUTE) {}
            composable(Destinations.LICENSES_ROUTE) {}
        }
    }
}
