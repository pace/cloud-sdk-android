package car.pace.cofu

import car.pace.cofu.data.PermissionRepository
import car.pace.cofu.data.PermissionRepository.Companion.FINE_LOCATION_PERMISSION
import car.pace.cofu.data.PermissionRepository.Companion.NOTIFICATION_PERMISSION
import car.pace.cofu.data.SharedPreferencesRepository
import car.pace.cofu.ui.onboarding.OnboardingPage
import car.pace.cofu.ui.onboarding.OnboardingViewModel
import car.pace.cofu.ui.onboarding.authentication.AuthenticationViewModel
import car.pace.cofu.ui.wallet.fueltype.FuelTypeGroup
import car.pace.cofu.util.BuildProvider
import car.pace.cofu.util.LogAndBreadcrumb
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.verify
import junit.framework.TestCase.assertEquals
import org.junit.Test

class OnboardingViewModelTest {

    private val sharedPreferencesRepository = mockk<SharedPreferencesRepository>(relaxed = true)
    private val permissionRepository = mockk<PermissionRepository>(relaxed = true)
    private lateinit var viewModel: OnboardingViewModel

    @Test
    fun `show all onboarding pages`() {
        testOnboarding(expectedOnboardingPages = OnboardingPage.entries)
    }

    @Test
    fun `skip tracking and notification pages`() {
        val expectedOnboardingPages = buildList {
            addAll(OnboardingPage.entries)
            remove(OnboardingPage.TRACKING)
            remove(OnboardingPage.NOTIFICATION_PERMISSION)
        }
        testOnboarding(isAnalyticsEnabled = false, expectedOnboardingPages = expectedOnboardingPages)
    }

    @Test
    fun `skip LocationPage`() {
        val expectedOnboardingPages = buildList {
            addAll(OnboardingPage.entries)
            remove(OnboardingPage.LOCATION_PERMISSION)
        }
        testOnboarding(locationPermissionGiven = true, expectedOnboardingPages = expectedOnboardingPages)
    }

    @Test
    fun `skip NotificationPage`() {
        val expectedOnboardingPages = buildList {
            addAll(OnboardingPage.entries)
            remove(OnboardingPage.NOTIFICATION_PERMISSION)
        }

        // Notification permission already given
        testOnboarding(notificationPermissionGiven = true, expectedOnboardingPages = expectedOnboardingPages)

        // SDK Version too low
        testOnboarding(sdkVersion = 30, expectedOnboardingPages = expectedOnboardingPages)
    }

    @Test
    fun `skip FuelTypePage`() {
        val expectedOnboardingPages = buildList {
            addAll(OnboardingPage.entries)
            remove(OnboardingPage.FUEL_TYPE)
        }
        testOnboarding(hidePrices = true, expectedOnboardingPages = expectedOnboardingPages)
    }

    @Test
    fun `skip TwoFactorPage`() {
        val expectedOnboardingPages = buildList {
            addAll(OnboardingPage.entries)
            remove(OnboardingPage.TWO_FACTOR)
        }
        testOnboarding(isTwoFactorEnabled = false, expectedOnboardingPages = expectedOnboardingPages)
    }

    @Test
    fun `skip PaymentMethodPage if payment method is already onboarded`() {
        val expectedOnboardingPages = buildList {
            addAll(OnboardingPage.entries)
            remove(OnboardingPage.PAYMENT_METHOD)
        }
        testOnboarding(userHasPaymentMethods = true, expectedOnboardingPages = expectedOnboardingPages)
    }

    @Test
    fun `skip PaymentMethodPage if payment method management is not available`() {
        val expectedOnboardingPages = buildList {
            addAll(OnboardingPage.entries)
            remove(OnboardingPage.PAYMENT_METHOD)
        }
        testOnboarding(paymentMethodManagementEnabled = false, expectedOnboardingPages = expectedOnboardingPages)
    }

    @Suppress("IMPLICIT_CAST_TO_ANY")
    private fun testOnboarding(
        isAnalyticsEnabled: Boolean = true,
        sdkVersion: Int = 34,
        locationPermissionGiven: Boolean = false,
        notificationPermissionGiven: Boolean = false,
        hidePrices: Boolean = false,
        isTwoFactorEnabled: Boolean = true,
        paymentMethodManagementEnabled: Boolean = true,
        userHasPaymentMethods: Boolean = false,
        expectedOnboardingPages: List<OnboardingPage>
    ) {
        mockkObject(LogAndBreadcrumb)
        every { LogAndBreadcrumb.i(any(), any()) } returns Unit
        every { permissionRepository.isPermissionGranted(FINE_LOCATION_PERMISSION) } returns locationPermissionGiven
        every { permissionRepository.isPermissionGranted(NOTIFICATION_PERMISSION) } returns notificationPermissionGiven
        mockkObject(BuildProvider)
        every { BuildProvider.isAnalyticsEnabled() } returns isAnalyticsEnabled
        every { BuildProvider.hidePrices() } returns hidePrices
        every { BuildProvider.getSDKVersion() } returns sdkVersion
        viewModel = OnboardingViewModel(sharedPreferencesRepository, permissionRepository)

        val expectedInitialPages = buildList<OnboardingPage> {
            addAll(expectedOnboardingPages)
            add(OnboardingPage.TWO_FACTOR)
            add(OnboardingPage.PAYMENT_METHOD)
        }.distinct()
        assertEquals(expectedInitialPages.size, viewModel.getCountOfPages())

        val seenOnboardingPages = mutableListOf<OnboardingPage?>()
        // Iterate through pages till onboarding is done
        var currentPageIndex: Int? = 0
        while (currentPageIndex != null) {
            val currentPage = viewModel.getPage(currentPageIndex)
            seenOnboardingPages.add(currentPage)

            val args = when (currentPage) {
                OnboardingPage.AUTHENTICATION -> AuthenticationViewModel.AuthenticationResult(
                    twoFactorEnabled = isTwoFactorEnabled,
                    paymentMethodManagementEnabled = paymentMethodManagementEnabled,
                    userHasPaymentMethods = userHasPaymentMethods
                )

                OnboardingPage.FUEL_TYPE -> FuelTypeGroup.DIESEL
                else -> null
            }

            currentPageIndex = viewModel.nextStep(currentPageIndex, args)
        }

        assertEquals(expectedOnboardingPages, seenOnboardingPages)

        val fuelTypeGroup = if (hidePrices) FuelTypeGroup.PETROL else FuelTypeGroup.DIESEL
        verify { sharedPreferencesRepository.putValue(SharedPreferencesRepository.PREF_KEY_FUEL_TYPE, fuelTypeGroup.prefFuelType.ordinal) }
    }
}