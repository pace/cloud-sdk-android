package car.pace.cofu.ui.onboarding

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import car.pace.cofu.ui.navigation.graph.Route
import car.pace.cofu.ui.onboarding.authentication.AuthenticationPage
import car.pace.cofu.ui.onboarding.fueltype.FuelTypePage
import car.pace.cofu.ui.onboarding.legal.LegalPage
import car.pace.cofu.ui.onboarding.paymentmethod.PaymentMethodPage
import car.pace.cofu.ui.onboarding.permission.location.LocationPermissionPage
import car.pace.cofu.ui.onboarding.permission.notification.NotificationPermissionPage
import car.pace.cofu.ui.onboarding.tracking.TrackingPage
import car.pace.cofu.ui.onboarding.twofactor.TwoFactorPage
import car.pace.cofu.ui.theme.AppTheme
import kotlin.math.absoluteValue
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel = hiltViewModel(),
    onNavigate: (Route) -> Unit,
    onDone: () -> Unit
) {
    val pagerState = rememberPagerState { viewModel.getCountOfPages() }
    val coroutineScope = rememberCoroutineScope()

    fun nextStep(nextPage: Int? = viewModel.nextStep(pagerState.currentPage, null)) {
        coroutineScope.launch {
            if (nextPage == null) {
                onDone()
            } else {
                pagerState.animateScrollToPage(nextPage)
            }
        }
    }

    fun navigateToAuthorization() {
        coroutineScope.launch {
            pagerState.animateScrollToPage(OnboardingPage.AUTHENTICATION.ordinal)
        }
    }

    HorizontalPager(
        state = pagerState,
        userScrollEnabled = false
    ) { index ->
        if ((pagerState.currentPage - index).absoluteValue > 1) {
            // Prevent preloading
            return@HorizontalPager
        }

        when (viewModel.getPage(index)) {
            OnboardingPage.LEGAL -> LegalPage(onNavigate = onNavigate, onNext = ::nextStep)
            OnboardingPage.TRACKING -> TrackingPage(onNavigate = onNavigate, onNext = ::nextStep)
            OnboardingPage.NOTIFICATION_PERMISSION -> NotificationPermissionPage(onNext = ::nextStep)
            OnboardingPage.LOCATION_PERMISSION -> LocationPermissionPage(onNext = ::nextStep)
            OnboardingPage.AUTHENTICATION -> AuthenticationPage { authenticationResult ->
                val nextPage = viewModel.nextStep(pagerState.currentPage, authenticationResult)
                nextStep(nextPage)
            }
            OnboardingPage.TWO_FACTOR -> TwoFactorPage(onAuthorization = ::navigateToAuthorization, onNext = ::nextStep)
            OnboardingPage.PAYMENT_METHOD -> PaymentMethodPage(onNext = ::nextStep)
            OnboardingPage.FUEL_TYPE -> FuelTypePage { fuelTypeGroup ->
                val nextPage = viewModel.nextStep(pagerState.currentPage, fuelTypeGroup)
                nextStep(nextPage)
            }
            else -> {}
        }
    }
}

@Preview
@Composable
fun OnboardingScreenPreview() {
    AppTheme {
        OnboardingScreen(
            onNavigate = {},
            onDone = {}
        )
    }
}
