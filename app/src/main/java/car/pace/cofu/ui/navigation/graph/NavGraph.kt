package car.pace.cofu.ui.navigation.graph

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavOptions
import androidx.navigation.NavType
import androidx.navigation.Navigator
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navigation
import car.pace.cofu.ui.home.HomeScreen
import car.pace.cofu.ui.more.MoreScreen
import car.pace.cofu.ui.onboarding.OnboardingScreen
import car.pace.cofu.ui.wallet.WalletScreen
import car.pace.cofu.ui.wallet.fueltype.FuelTypeScreen
import car.pace.cofu.ui.wallet.paymentmethods.PaymentMethodsScreen
import car.pace.cofu.util.SnackbarData

fun NavGraphBuilder.onboardingGraph(
    showSnackbar: (SnackbarData) -> Unit,
    onDone: () -> Unit
) {
    composable(Route.ONBOARDING.route) {
        OnboardingScreen(
            showSnackbar = showSnackbar,
            onDone = onDone
        )
    }
}

fun NavGraphBuilder.homeGraph(
    showSnackbar: (SnackbarData) -> Unit
) {
    navigation(
        startDestination = Route.HOME.route,
        route = Graph.HOME.route
    ) {
        composable(Route.HOME.route) {
            HomeScreen(showSnackbar = showSnackbar)
        }
        composable(
            route = Route.DETAIL.route,
            arguments = listOf(
                navArgument("id") { type = NavType.StringType }
            )
        ) {
            val id = it.arguments?.getString("id")
            // TODO: DetailScreen
        }
    }
}

fun NavGraphBuilder.walletGraph(
    onNavigate: (Route) -> Unit
) {
    navigation(
        startDestination = Route.WALLET.route,
        route = Graph.WALLET.route
    ) {
        composable(Route.WALLET.route) {
            WalletScreen(onNavigate = onNavigate)
        }
        composable(Route.METHODS.route) {
            PaymentMethodsScreen()
        }
        composable(Route.FUEL_TYPE.route) {
            FuelTypeScreen()
        }
    }
}

fun NavGraphBuilder.moreGraph() {
    navigation(
        startDestination = Route.MORE.route,
        route = Graph.MORE.route
    ) {
        composable(Route.MORE.route) {
            MoreScreen()
        }
        composable(Route.TERMS.route) {
            // TODO: TermsScreen
        }
        composable(Route.PRIVACY.route) {
            // TODO: PrivacyScreen
        }
        composable(Route.CONTACT.route) {
            // TODO: ContactScreen
        }
        composable(Route.IMPRINT.route) {
            // TODO: ImprintScreen
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

fun NavHostController.navigate(
    graph: Graph,
    navOptions: NavOptions? = null,
    navigatorExtras: Navigator.Extras? = null
) {
    navigate(graph.route, navOptions, navigatorExtras)
}
