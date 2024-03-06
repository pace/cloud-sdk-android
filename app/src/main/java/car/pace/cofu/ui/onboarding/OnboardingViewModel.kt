package car.pace.cofu.ui.onboarding

import android.Manifest
import android.os.Build
import androidx.lifecycle.ViewModel
import car.pace.cofu.data.PermissionRepository
import car.pace.cofu.data.SharedPreferencesRepository
import car.pace.cofu.ui.onboarding.authentication.AuthenticationViewModel
import car.pace.cofu.ui.wallet.fueltype.FuelTypeGroup
import car.pace.cofu.util.BuildProvider
import car.pace.cofu.util.LogAndBreadcrumb
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val sharedPreferencesRepository: SharedPreferencesRepository,
    private val permissionRepository: PermissionRepository
) : ViewModel() {

    private val pages = buildList {
        addAll(OnboardingPage.entries)

        if (!BuildProvider.isAnalyticsEnabled()) {
            remove(OnboardingPage.TRACKING)
        }

        if (permissionRepository.isPermissionGranted(Manifest.permission.ACCESS_FINE_LOCATION)) {
            remove(OnboardingPage.LOCATION_PERMISSION)
        }

        if (!BuildProvider.isAnalyticsEnabled() ||
            BuildProvider.getSDKVersion() < Build.VERSION_CODES.TIRAMISU ||
            permissionRepository.isPermissionGranted(Manifest.permission.POST_NOTIFICATIONS)
        ) {
            remove(OnboardingPage.NOTIFICATION_PERMISSION)
        }

        if (BuildProvider.hidePrices()) {
            remove(OnboardingPage.FUEL_TYPE)
        }
    }.toMutableList()

    fun getCountOfPages() = pages.size

    fun <T> nextStep(currentPage: Int, args: T?): Int? {
        val nextPage = when (args) {
            is AuthenticationViewModel.AuthenticationResult -> {
                if (!args.twoFactorEnabled) {
                    // Skip TwoFactorPage
                    LogAndBreadcrumb.i(LogAndBreadcrumb.ONBOARDING, "Skip TwoFactorPage in Onboarding")
                    pages.remove(OnboardingPage.TWO_FACTOR)
                }

                if (!args.paymentMethodManagementEnabled || args.userHasPaymentMethods) {
                    // Skip PaymentMethodPage
                    LogAndBreadcrumb.i(LogAndBreadcrumb.ONBOARDING, "Skip PaymentMethodPage in Onboarding")
                    pages.remove(OnboardingPage.PAYMENT_METHOD)
                }
                currentPage + 1
            }

            is FuelTypeGroup -> {
                finishOnboarding(args)
                null
            }

            else -> currentPage + 1
        }

        return if (nextPage != null && nextPage == pages.size) {
            // Onboarding finished without fuel type selection
            val fuelTypeGroup = FuelTypeGroup.PETROL
            LogAndBreadcrumb.i(LogAndBreadcrumb.ONBOARDING, "Selected fuel type: $fuelTypeGroup (fallback)")
            finishOnboarding(fuelTypeGroup)
            null
        } else {
            nextPage
        }
    }

    fun getPage(index: Int): OnboardingPage? {
        return pages.getOrNull(index)
    }

    private fun finishOnboarding(fuelTypeGroup: FuelTypeGroup) {
        sharedPreferencesRepository.putValue(SharedPreferencesRepository.PREF_KEY_FUEL_TYPE, fuelTypeGroup.prefFuelType.ordinal)
    }
}
