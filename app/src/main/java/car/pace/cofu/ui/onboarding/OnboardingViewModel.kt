package car.pace.cofu.ui.onboarding

import android.content.Context
import androidx.lifecycle.ViewModel
import car.pace.cofu.data.PaymentMethodRepository
import car.pace.cofu.data.SharedPreferencesRepository
import car.pace.cofu.data.SharedPreferencesRepository.Companion.PREF_KEY_FUEL_TYPE
import car.pace.cofu.data.UserRepository
import car.pace.cofu.ui.fueltype.FuelType
import car.pace.cofu.util.PermissionUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val paymentMethodRepository: PaymentMethodRepository,
    private val sharedPreferencesRepository: SharedPreferencesRepository
) : ViewModel() {

    fun setFuelType(fuelType: FuelType) {
        sharedPreferencesRepository.putValue(PREF_KEY_FUEL_TYPE, fuelType.ordinal)
    }

    suspend fun canSkipPage(currentPage: OnboardingPage?, context: Context): Boolean {
        when (currentPage) {
            OnboardingPage.LOCATION_PERMISSION -> {
                if (PermissionUtils.locationPermissionsGranted(context)) {
                    return true
                }
            }

            OnboardingPage.AUTHENTICATION -> {
                if (userRepository.isAuthorizationValid()) {
                    return true
                }
            }

            OnboardingPage.TWO_FACTOR -> {
                if (userRepository.isBiometricAuthenticationEnabled() && userRepository.isPINSet().getOrNull() == true) {
                    return true
                }
            }

            OnboardingPage.PAYMENT_METHOD -> {
                val hasPaymentMethods = !paymentMethodRepository.getPaymentMethods(true)?.getOrNull().isNullOrEmpty()
                if (hasPaymentMethods) {
                    return true
                }
            }

            else -> return false
        }

        return false
    }
}
