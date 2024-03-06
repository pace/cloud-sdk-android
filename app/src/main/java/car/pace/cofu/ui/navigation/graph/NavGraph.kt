package car.pace.cofu.ui.navigation.graph

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.runtime.Composable
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDeepLink
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navigation
import car.pace.cofu.ui.animation.ScaleTransitionDirection
import car.pace.cofu.ui.animation.scaleIntoContainer
import car.pace.cofu.ui.animation.scaleOutOfContainer
import car.pace.cofu.ui.animation.slideIn
import car.pace.cofu.ui.animation.slideOut
import car.pace.cofu.ui.consent.Consent
import car.pace.cofu.ui.consent.ConsentScreen
import car.pace.cofu.ui.detail.DetailScreen
import car.pace.cofu.ui.list.ListScreen
import car.pace.cofu.ui.map.MapScreen
import car.pace.cofu.ui.more.MoreScreen
import car.pace.cofu.ui.more.legal.LegalDocumentScreen
import car.pace.cofu.ui.more.licenses.LicensesScreen
import car.pace.cofu.ui.more.permissions.PermissionsScreen
import car.pace.cofu.ui.more.tracking.TrackingScreen
import car.pace.cofu.ui.onboarding.OnboardingScreen
import car.pace.cofu.ui.wallet.WalletScreen
import car.pace.cofu.ui.wallet.authorization.AuthorisationScreen
import car.pace.cofu.ui.wallet.fueltype.FuelTypeScreen
import car.pace.cofu.ui.wallet.paymentmethods.PaymentMethodsScreen

fun NavGraphBuilder.onboardingGraph(
    onNavigate: (Route) -> Unit,
    onNavigateUp: () -> Unit,
    onDone: () -> Unit
) {
    navigation(
        startDestination = Route.ONBOARDING.route,
        route = Graph.ONBOARDING.route
    ) {
        composable(
            route = Route.ONBOARDING.route,
            exitTransition = { scaleOutOfContainer(direction = ScaleTransitionDirection.INWARDS) },
            popEnterTransition = { scaleIntoContainer(direction = ScaleTransitionDirection.OUTWARDS) }
        ) {
            OnboardingScreen(
                onNavigate = onNavigate,
                onDone = onDone
            )
        }
        composable(
            route = Route.ONBOARDING_TERMS.route,
            enterTransition = { scaleIntoContainer() },
            popExitTransition = { scaleOutOfContainer() }
        ) {
            LegalDocumentScreen(
                legalConsent = Consent.Legal.Terms,
                onNavigateUp = onNavigateUp
            )
        }
        composable(
            route = Route.ONBOARDING_PRIVACY.route,
            enterTransition = { scaleIntoContainer() },
            popExitTransition = { scaleOutOfContainer() }
        ) {
            LegalDocumentScreen(
                legalConsent = Consent.Legal.Privacy,
                onNavigateUp = onNavigateUp
            )
        }
        composable(
            route = Route.ONBOARDING_ANALYSIS.route,
            enterTransition = { scaleIntoContainer() },
            popExitTransition = { scaleOutOfContainer() }
        ) {
            LegalDocumentScreen(
                legalConsent = Consent.Legal.Tracking,
                onNavigateUp = onNavigateUp
            )
        }
    }
}

fun NavGraphBuilder.consentGraph(
    onNavigate: (Route) -> Unit,
    onNavigateUp: () -> Unit,
    onDone: () -> Unit
) {
    navigation(
        startDestination = Route.CONSENT.route,
        route = Graph.CONSENT.route
    ) {
        composable(
            route = Route.CONSENT.route,
            exitTransition = { scaleOutOfContainer(direction = ScaleTransitionDirection.INWARDS) },
            popEnterTransition = { scaleIntoContainer(direction = ScaleTransitionDirection.OUTWARDS) }
        ) {
            ConsentScreen(
                onNavigate = onNavigate,
                onDone = onDone
            )
        }
        composable(
            route = Route.ONBOARDING_TERMS.route,
            enterTransition = { scaleIntoContainer() },
            popExitTransition = { scaleOutOfContainer() }
        ) {
            LegalDocumentScreen(
                legalConsent = Consent.Legal.Terms,
                onNavigateUp = onNavigateUp
            )
        }
        composable(
            route = Route.ONBOARDING_PRIVACY.route,
            enterTransition = { scaleIntoContainer() },
            popExitTransition = { scaleOutOfContainer() }
        ) {
            LegalDocumentScreen(
                legalConsent = Consent.Legal.Privacy,
                onNavigateUp = onNavigateUp
            )
        }
        composable(
            route = Route.ONBOARDING_ANALYSIS.route,
            enterTransition = { scaleIntoContainer() },
            popExitTransition = { scaleOutOfContainer() }
        ) {
            LegalDocumentScreen(
                legalConsent = Consent.Legal.Tracking,
                onNavigateUp = onNavigateUp
            )
        }
    }
}

fun NavGraphBuilder.listGraph(
    onNavigate: (String) -> Unit,
    onNavigateUp: () -> Unit
) {
    navigation(
        startDestination = Route.LIST.route,
        route = Graph.LIST.route
    ) {
        parentComposable(Route.LIST.route) {
            ListScreen {
                onNavigate("${Route.LIST_DETAIL.route}/$it")
            }
        }
        childComposable(
            route = "${Route.LIST_DETAIL.route}/{id}",
            arguments = listOf(
                navArgument("id") {
                    type = NavType.StringType
                }
            )
        ) {
            DetailScreen(
                onNavigateUp = onNavigateUp
            )
        }
    }
}

fun NavGraphBuilder.mapGraph(
    onNavigate: (String) -> Unit,
    onNavigateUp: () -> Unit
) {
    navigation(
        startDestination = Route.MAP.route,
        route = Graph.MAP.route
    ) {
        parentComposable(Route.MAP.route) {
            MapScreen {
                onNavigate("${Route.MAP_DETAIL.route}/$it")
            }
        }
        childComposable(
            route = "${Route.MAP_DETAIL.route}/{id}",
            arguments = listOf(
                navArgument("id") {
                    type = NavType.StringType
                }
            )
        ) {
            DetailScreen(
                onNavigateUp = onNavigateUp
            )
        }
    }
}

fun NavGraphBuilder.walletGraph(
    onNavigate: (Route) -> Unit,
    onNavigateUp: () -> Unit,
    onLogout: () -> Unit
) {
    navigation(
        startDestination = Route.WALLET.route,
        route = Graph.WALLET.route
    ) {
        parentComposable(Route.WALLET.route) {
            WalletScreen(
                onNavigate = onNavigate,
                onLogout = onLogout
            )
        }
        childComposable(Route.PAYMENT_METHODS.route) {
            PaymentMethodsScreen(
                onNavigateUp = onNavigateUp
            )
        }
        childComposable(Route.FUEL_TYPE.route) {
            FuelTypeScreen(
                onNavigateUp = onNavigateUp
            )
        }
        childComposable(Route.AUTHORIZATION.route) {
            AuthorisationScreen(
                onNavigateUp = onNavigateUp
            )
        }
    }
}

fun NavGraphBuilder.moreGraph(
    onNavigate: (Route) -> Unit,
    onNavigateUp: () -> Unit
) {
    navigation(
        startDestination = Route.MORE.route,
        route = Graph.MORE.route
    ) {
        parentComposable(Route.MORE.route) {
            MoreScreen(
                onNavigate = onNavigate
            )
        }
        childComposable(Route.TERMS.route) {
            LegalDocumentScreen(
                legalConsent = Consent.Legal.Terms,
                onNavigateUp = onNavigateUp
            )
        }
        childComposable(Route.PRIVACY.route) {
            LegalDocumentScreen(
                legalConsent = Consent.Legal.Privacy,
                onNavigateUp = onNavigateUp
            )
        }
        childComposable(Route.TRACKING.route) {
            TrackingScreen(
                onNavigate = onNavigate,
                onNavigateUp = onNavigateUp
            )
        }
        childComposable(Route.ANALYSIS.route) {
            LegalDocumentScreen(
                legalConsent = Consent.Legal.Tracking,
                onNavigateUp = onNavigateUp
            )
        }
        childComposable(Route.IMPRINT.route) {
            LegalDocumentScreen(
                legalConsent = Consent.Legal.Imprint,
                onNavigateUp = onNavigateUp
            )
        }
        childComposable(Route.LICENSES.route) {
            LicensesScreen(
                onNavigateUp = onNavigateUp
            )
        }
        childComposable(Route.PERMISSIONS.route) {
            PermissionsScreen(
                onNavigateUp = onNavigateUp
            )
        }
    }
}

fun NavGraphBuilder.parentComposable(
    route: String,
    arguments: List<NamedNavArgument> = emptyList(),
    deepLinks: List<NavDeepLink> = emptyList(),
    content: @Composable AnimatedContentScope.(NavBackStackEntry) -> Unit
) {
    composable(
        route = route,
        arguments = arguments,
        deepLinks = deepLinks,
        exitTransition = {
            if (targetState.destination.route !in bottomBarRoutes && initialState.destination.route.isSameGraph(targetState.destination.route)) {
                slideOut()
            } else {
                null
            }
        },
        popEnterTransition = {
            if (initialState.destination.route !in bottomBarRoutes && initialState.destination.route.isSameGraph(targetState.destination.route)) {
                slideIn(towards = AnimatedContentTransitionScope.SlideDirection.End)
            } else {
                null
            }
        },
        content = content
    )
}

fun NavGraphBuilder.childComposable(
    route: String,
    arguments: List<NamedNavArgument> = emptyList(),
    deepLinks: List<NavDeepLink> = emptyList(),
    content: @Composable AnimatedContentScope.(NavBackStackEntry) -> Unit
) {
    composable(
        route = route,
        arguments = arguments,
        deepLinks = deepLinks,
        enterTransition = {
            if (initialState.destination.route.isSameGraph(targetState.destination.route)) {
                slideIn()
            } else {
                null
            }
        },
        exitTransition = {
            if (initialState.destination.route.isSameGraph(targetState.destination.route)) {
                slideOut()
            } else {
                null
            }
        },
        popEnterTransition = {
            if (initialState.destination.route.isSameGraph(targetState.destination.route)) {
                slideIn(towards = AnimatedContentTransitionScope.SlideDirection.End)
            } else {
                null
            }
        },
        popExitTransition = {
            if (initialState.destination.route.isSameGraph(targetState.destination.route)) {
                slideOut(towards = AnimatedContentTransitionScope.SlideDirection.End)
            } else {
                null
            }
        },
        content = content
    )
}

fun String?.isSameGraph(route: String?): Boolean {
    val initialRoute = Route.fromRoute(this)
    val targetRoute = Route.fromRoute(route)

    return initialRoute?.graph == targetRoute?.graph
}

fun NavHostController.navigate(
    route: Route,
    builder: NavOptionsBuilder.() -> Unit = {}
) {
    navigate(
        route = route.route,
        builder = builder
    )
}

fun NavHostController.navigate(
    graph: Graph,
    builder: NavOptionsBuilder.() -> Unit = {}
) {
    navigate(
        route = graph.route,
        builder = builder
    )
}
