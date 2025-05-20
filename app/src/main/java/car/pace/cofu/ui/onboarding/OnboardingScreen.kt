package car.pace.cofu.ui.onboarding

import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.hilt.navigation.compose.hiltViewModel
import car.pace.cofu.ui.Route
import car.pace.cofu.ui.onboarding.authentication.AuthenticationPage
import car.pace.cofu.ui.onboarding.fueltype.FuelTypePage
import car.pace.cofu.ui.onboarding.legal.LegalPage
import car.pace.cofu.ui.onboarding.paymentmethod.PaymentMethodPage
import car.pace.cofu.ui.onboarding.permission.location.LocationPermissionPage
import car.pace.cofu.ui.onboarding.permission.notification.NotificationPermissionPage
import car.pace.cofu.ui.onboarding.tracking.TrackingPage
import car.pace.cofu.ui.onboarding.twofactor.TwoFactorPage
import kotlin.math.absoluteValue
import kotlinx.coroutines.launch

@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel = hiltViewModel(),
    onNavigate: (Route) -> Unit,
    onDone: () -> Unit
) {
    val pagerState = rememberPagerState { viewModel.getCountOfPages() }
    val coroutineScope = rememberCoroutineScope()

    fun nextStep(args: Any? = null) {
        coroutineScope.launch {
            val nextPage = viewModel.nextStep(pagerState.currentPage, args)
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
            OnboardingPage.AUTHENTICATION -> AuthenticationPage(onNext = ::nextStep)
            OnboardingPage.TWO_FACTOR -> TwoFactorPage(onAuthorization = ::navigateToAuthorization, onNext = ::nextStep)
            OnboardingPage.PAYMENT_METHOD -> PaymentMethodPage(onNext = ::nextStep)
            OnboardingPage.FUEL_TYPE -> FuelTypePage(onNext = ::nextStep)
            else -> {}
        }
    }
}
