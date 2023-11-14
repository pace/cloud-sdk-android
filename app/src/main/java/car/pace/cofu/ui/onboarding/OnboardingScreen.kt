package car.pace.cofu.ui.onboarding

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import car.pace.cofu.ui.fueltype.FuelType
import car.pace.cofu.ui.onboarding.authentication.AuthenticationPage
import car.pace.cofu.ui.onboarding.fueltype.FuelTypePage
import car.pace.cofu.ui.onboarding.paymentmethod.PaymentMethodPage
import car.pace.cofu.ui.onboarding.permission.LocationPermissionPage
import car.pace.cofu.ui.onboarding.twofactor.TwoFactorPage
import car.pace.cofu.ui.theme.AppTheme
import car.pace.cofu.util.SnackbarData
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel = hiltViewModel(),
    showSnackbar: (SnackbarData) -> Unit,
    onDone: () -> Unit
) {
    Column {
        val pages = remember { OnboardingPage.values() }
        val pagerState = rememberPagerState { pages.size }
        val coroutineScope = rememberCoroutineScope()
        val context = LocalContext.current

        fun nextStep() {
            coroutineScope.launch {
                pagerState.animateScrollToPage(pagerState.currentPage + 1)
            }
        }

        fun navigateToAuthorization() {
            coroutineScope.launch {
                pagerState.animateScrollToPage(OnboardingPage.AUTHENTICATION.ordinal)
            }
        }

        fun setFuelType(fuelType: FuelType) {
            viewModel.setFuelType(fuelType)
            onDone()
        }

        LaunchedEffect(pagerState) {
            snapshotFlow { pagerState.currentPage }.collectLatest {
                val canSkipPage = viewModel.canSkipPage(pages.getOrNull(it), context)
                if (canSkipPage) {
                    nextStep()
                }
            }
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f),
            userScrollEnabled = false
        ) { index ->
            when (pages.getOrNull(index)) {
                OnboardingPage.LOCATION_PERMISSION -> LocationPermissionPage(onNext = ::nextStep)
                OnboardingPage.AUTHENTICATION -> AuthenticationPage(showSnackbar = showSnackbar, onNext = ::nextStep)
                OnboardingPage.TWO_FACTOR -> TwoFactorPage(showSnackbar = showSnackbar, onAuthorization = ::navigateToAuthorization, onNext = ::nextStep)
                OnboardingPage.PAYMENT_METHOD -> PaymentMethodPage(onNext = ::nextStep)
                OnboardingPage.FUEL_TYPE -> FuelTypePage(onNext = ::setFuelType)
                else -> {}
            }
        }

        PageIndicator(
            modifier = Modifier.padding(start = 50.dp, top = 30.dp, end = 50.dp, bottom = 20.dp),
            pageCount = pages.size,
            currentPageIndex = pagerState.currentPage
        )
    }
}

@Composable
fun PageIndicator(modifier: Modifier = Modifier, pageCount: Int, currentPageIndex: Int) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center
    ) {
        repeat(pageCount) {
            val color = if (currentPageIndex == it) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.secondary
            Box(
                modifier = Modifier
                    .padding(horizontal = 5.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(color)
                    .weight(1f)
                    .height(3.dp)
            )
        }
    }
}

@Preview
@Composable
fun OnboardingScreenPreview() {
    AppTheme {
        OnboardingScreen(
            showSnackbar = {},
            onDone = {}
        )
    }
}

@Preview
@Composable
fun PageIndicatorPreview() {
    AppTheme {
        PageIndicator(pageCount = 6, currentPageIndex = 2)
    }
}
