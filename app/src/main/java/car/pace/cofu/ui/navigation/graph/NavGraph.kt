package car.pace.cofu.ui.navigation.graph

import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import car.pace.cofu.ui.home.HomeScreen
import car.pace.cofu.ui.more.MoreScreen
import car.pace.cofu.ui.onboarding.OnboardingScreen
import car.pace.cofu.ui.wallet.FuelTypeScreen
import car.pace.cofu.ui.wallet.MethodsScreen
import car.pace.cofu.ui.wallet.WalletScreen
import car.pace.cofu.util.SnackbarData
import cloud.pace.sdk.appkit.AppKit

fun NavGraphBuilder.onboardingGraph(
    showSnackbar: (SnackbarData) -> Unit,
    onDone: () -> Unit
) {
    composable(Destination.Onboarding.route) {
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
        startDestination = Destination.Home.List.route,
        route = Destination.Home.route
    ) {
        composable(Destination.Home.List.route) {
            HomeScreen(showSnackbar = showSnackbar)
        }
        composable(Destination.Home.Detail.route) {
            // TODO: DetailScreen
        }
    }
}

fun NavGraphBuilder.walletGraph(
    onNavigate: (String) -> Unit
) {
    navigation(
        startDestination = Destination.Wallet.List.route,
        route = Destination.Wallet.route
    ) {
        composable(Destination.Wallet.List.route) {
            WalletScreen(onNavigate = onNavigate)
        }
        composable(Destination.Wallet.Methods.route) {
            MethodsScreen()
        }
        composable(Destination.Wallet.Transactions.route) {
            val context = LocalContext.current
            AppKit.openTransactions(context)
        }
        composable(Destination.Wallet.FuelType.route) {
            FuelTypeScreen()
        }
    }
}

fun NavGraphBuilder.moreGraph() {
    navigation(
        startDestination = Destination.More.List.route,
        route = Destination.More.route
    ) {
        composable(Destination.More.List.route) {
            MoreScreen()
        }
        composable(Destination.More.Terms.route) {
            // TODO: TermsScreen
        }
        composable(Destination.More.Privacy.route) {
            // TODO: PrivacyScreen
        }
        composable(Destination.More.Contact.route) {
            // TODO: ContactScreen
        }
        composable(Destination.More.Imprint.route) {
            // TODO: ImprintScreen
        }
        composable(Destination.More.Libraries.route) {
            // TODO: Show LibrariesContainer of AboutLibraries
        }
    }
}
