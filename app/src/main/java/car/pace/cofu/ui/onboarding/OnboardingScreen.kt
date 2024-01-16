package car.pace.cofu.ui.onboarding

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.tooling.preview.Preview
import car.pace.cofu.BuildConfig
import car.pace.cofu.ui.navigation.graph.Route
import car.pace.cofu.ui.onboarding.authentication.AuthenticationPage
import car.pace.cofu.ui.onboarding.fueltype.FuelTypePage
import car.pace.cofu.ui.onboarding.legal.LegalPage
import car.pace.cofu.ui.onboarding.paymentmethod.PaymentMethodPage
import car.pace.cofu.ui.onboarding.permission.LocationPermissionPage
import car.pace.cofu.ui.onboarding.tracking.TrackingPage
import car.pace.cofu.ui.onboarding.twofactor.TwoFactorPage
import car.pace.cofu.ui.theme.AppTheme
import car.pace.cofu.ui.wallet.fueltype.FuelTypeGroup
import car.pace.cofu.util.LogAndBreadcrumb
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    onNavigate: (Route) -> Unit,
    onDone: (FuelTypeGroup) -> Unit
) {
    val pages = remember {
        val values = OnboardingPage.entries
        if (BuildConfig.HIDE_PRICES) {
            values.filterNot { it == OnboardingPage.FUEL_TYPE }
        } else {
            values
        }
    }
    val pagerState = rememberPagerState { pages.size }
    val coroutineScope = rememberCoroutineScope()

    fun nextStep() {
        coroutineScope.launch {
            val newPage = pagerState.currentPage + 1
            if (newPage >= pages.size) {
                val fuelType = FuelTypeGroup.PETROL
                LogAndBreadcrumb.i(LogAndBreadcrumb.ONBOARDING, "Selected fuel type: $fuelType (fallback)")
                onDone(fuelType) // Petrol is fallback, if fuel type selection was not displayed
            } else {
                pagerState.animateScrollToPage(newPage)
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
        when (pages.getOrNull(index)) {
            OnboardingPage.LEGAL -> LegalPage(onNavigate = onNavigate, onNext = ::nextStep)
            OnboardingPage.TRACKING -> TrackingPage(onNavigate = onNavigate, onNext = ::nextStep)
            OnboardingPage.LOCATION_PERMISSION -> LocationPermissionPage(onNext = ::nextStep)
            OnboardingPage.AUTHENTICATION -> AuthenticationPage(onNext = ::nextStep)
            OnboardingPage.TWO_FACTOR -> TwoFactorPage(onAuthorization = ::navigateToAuthorization, onNext = ::nextStep)
            OnboardingPage.PAYMENT_METHOD -> PaymentMethodPage(onNext = ::nextStep)
            OnboardingPage.FUEL_TYPE -> FuelTypePage(onNext = onDone)
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
