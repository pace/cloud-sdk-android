package car.pace.cofu.ui.navigation.graph

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavOptions
import androidx.navigation.NavType
import androidx.navigation.Navigator
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navigation
import car.pace.cofu.ui.detail.DetailScreen
import car.pace.cofu.ui.home.HomeScreen
import car.pace.cofu.ui.more.MoreScreen
import car.pace.cofu.ui.more.WebViewScreen
import car.pace.cofu.ui.onboarding.OnboardingScreen
import car.pace.cofu.ui.wallet.WalletScreen
import car.pace.cofu.ui.wallet.authorization.AuthorisationScreen
import car.pace.cofu.ui.wallet.fueltype.FuelTypeGroup
import car.pace.cofu.ui.wallet.fueltype.FuelTypeScreen
import car.pace.cofu.ui.wallet.paymentmethods.PaymentMethodsScreen
import car.pace.cofu.util.Constants.ANALYSIS_URI
import car.pace.cofu.util.Constants.IMPRINT_URI
import car.pace.cofu.util.Constants.PRIVACY_URI
import car.pace.cofu.util.Constants.TERMS_URI
import car.pace.cofu.util.SnackbarData
import com.mikepenz.aboutlibraries.ui.compose.LibrariesContainer

fun NavGraphBuilder.onboardingGraph(
    onNavigate: (Route) -> Unit,
    onDone: (FuelTypeGroup) -> Unit
) {
    composable(Route.ONBOARDING.route) {
        OnboardingScreen(
            onNavigate = onNavigate,
            onDone = onDone
        )
    }
    composable(Route.ONBOARDING_TERMS.route) {
        WebViewScreen(url = TERMS_URI)
    }
    composable(Route.ONBOARDING_PRIVACY.route) {
        WebViewScreen(url = PRIVACY_URI)
    }
    composable(Route.ANALYSIS.route) {
        WebViewScreen(url = ANALYSIS_URI)
    }
}

fun NavGraphBuilder.homeGraph(
    showSnackbar: (SnackbarData) -> Unit,
    onNavigate: (String) -> Unit
) {
    navigation(
        startDestination = Route.HOME.route,
        route = Graph.HOME.route
    ) {
        composable(Route.HOME.route) {
            HomeScreen(
                showSnackbar = showSnackbar
            ) {
                onNavigate("${Route.DETAIL.route}/$it")
            }
        }
        composable(
            route = "${Route.DETAIL.route}/{id}",
            arguments = listOf(
                navArgument("id") {
                    type = NavType.StringType
                }
            )
        ) {
            DetailScreen()
        }
    }
}

fun NavGraphBuilder.walletGraph(
    showSnackbar: (SnackbarData) -> Unit,
    onNavigate: (Route) -> Unit
) {
    navigation(
        startDestination = Route.WALLET.route,
        route = Graph.WALLET.route
    ) {
        composable(Route.WALLET.route) {
            WalletScreen(onNavigate = onNavigate)
        }
        composable(Route.PAYMENT_METHODS.route) {
            PaymentMethodsScreen()
        }
        composable(Route.FUEL_TYPE.route) {
            FuelTypeScreen()
        }
        composable(Route.AUTHORIZATION.route) {
            AuthorisationScreen(showSnackbar = showSnackbar)
        }
    }
}

fun NavGraphBuilder.moreGraph(
    onNavigate: (Route) -> Unit
) {
    navigation(
        startDestination = Route.MORE.route,
        route = Graph.MORE.route
    ) {
        composable(Route.MORE.route) {
            MoreScreen(onNavigate = onNavigate)
        }
        composable(Route.TERMS.route) {
            WebViewScreen(url = TERMS_URI)
        }
        composable(Route.PRIVACY.route) {
            WebViewScreen(url = PRIVACY_URI)
        }
        composable(Route.IMPRINT.route) {
            WebViewScreen(url = IMPRINT_URI)
        }
        composable(Route.LICENSES.route) {
            LibrariesContainer(
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

fun NavHostController.navigate(
    route: Route,
    navOptions: NavOptions? = null,
    navigatorExtras: Navigator.Extras? = null
) {
    navigate(route.route, navOptions, navigatorExtras)
}
