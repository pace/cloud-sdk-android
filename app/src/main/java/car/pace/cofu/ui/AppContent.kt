package car.pace.cofu.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import car.pace.cofu.ui.home.Home
import car.pace.cofu.ui.onboarding.Onboarding
import car.pace.cofu.ui.theme.AppTheme

object Destinations {
    const val ONBOARDING_ROUTE = "onboarding"
    const val HOME_ROUTE = "home"
    const val FUEL_TYPE_ROUTE = "fuelType"
    const val IMPRINT_ROUTE = "imprint"
    const val PRIVACY_POLICY_ROUTE = "privacyPolicy"
    const val LICENSES_ROUTE = "licenses"
}

@Composable
fun AppContent() {
    AppTheme {
        val navController = rememberNavController()
        val onboardingComplete by remember {
            // TODO: Get and set onboarding state
            mutableStateOf(true)
        }

        NavHost(
            navController = navController,
            startDestination = if (onboardingComplete) Destinations.HOME_ROUTE else Destinations.ONBOARDING_ROUTE
        ) {
            composable(Destinations.ONBOARDING_ROUTE) {
                Onboarding()
            }
            composable(Destinations.HOME_ROUTE) {
                Home()
            }
            composable(Destinations.FUEL_TYPE_ROUTE) {

            }
            composable(Destinations.IMPRINT_ROUTE) {

            }
            composable(Destinations.PRIVACY_POLICY_ROUTE) {

            }
            composable(Destinations.LICENSES_ROUTE) {

            }
        }
    }
}
