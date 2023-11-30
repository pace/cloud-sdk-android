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
import car.pace.cofu.ui.more.MenuItemAction
import car.pace.cofu.ui.more.MoreScreen
import car.pace.cofu.ui.more.WebViewScreen
import car.pace.cofu.ui.onboarding.OnboardingScreen
import car.pace.cofu.ui.wallet.WalletScreen
import car.pace.cofu.ui.wallet.fueltype.FuelTypeGroup
import car.pace.cofu.ui.wallet.fueltype.FuelTypeScreen
import car.pace.cofu.ui.wallet.paymentmethods.PaymentMethodsScreen
import car.pace.cofu.util.LegalDocument
import car.pace.cofu.util.SnackbarData
import car.pace.cofu.util.extension.encode
import com.mikepenz.aboutlibraries.ui.compose.LibrariesContainer

fun NavGraphBuilder.onboardingGraph(
    onLegalDocument: (LegalDocument) -> Unit,
    onDone: (FuelTypeGroup) -> Unit
) {
    composable(Route.ONBOARDING.route) {
        OnboardingScreen(
            onLegalDocument = onLegalDocument,
            onDone = onDone
        )
    }
    composable(
        route = "${Route.ONBOARDING_WEBVIEW_CONTENT.route}/{url}",
        arguments = listOf(
            navArgument("url") {
                type = NavType.StringType
            }
        )
    ) {
        val url = it.arguments?.getString("url")
        url?.let {
            WebViewScreen(url)
        }
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
    }
}

fun NavGraphBuilder.moreGraph(
    onNavigate: (String) -> Unit
) {
    navigation(
        startDestination = Route.MORE.route,
        route = Graph.MORE.route
    ) {
        composable(Route.MORE.route) {
            MoreScreen {
                when (it.action) {
                    is MenuItemAction.LocalContent -> {
                        onNavigate("${Route.LOCAL_WEBVIEW_CONTENT.route}/${it.action.url.encode()}")
                    }

                    is MenuItemAction.Dependencies -> {
                        onNavigate(Route.LIBRARIES.route)
                    }

                    else -> {}
                }
            }
        }
        composable(
            route = "${Route.LOCAL_WEBVIEW_CONTENT.route}/{url}",
            arguments = listOf(
                navArgument("url") {
                    type = NavType.StringType
                }
            )
        ) {
            val url = it.arguments?.getString("url")
            url?.let {
                WebViewScreen(url)
            }
        }
        composable(Route.LIBRARIES.route) {
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

fun NavHostController.navigate(
    graph: Graph,
    navOptions: NavOptions? = null,
    navigatorExtras: Navigator.Extras? = null
) {
    navigate(graph.route, navOptions, navigatorExtras)
}
