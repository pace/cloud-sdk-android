package car.pace.cofu.ui.onboarding

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import car.pace.cofu.data.PaymentMethodRepository
import car.pace.cofu.data.SharedPreferencesRepository
import car.pace.cofu.data.SharedPreferencesRepository.Companion.PREF_KEY_FUEL_TYPE
import car.pace.cofu.data.UserRepository
import car.pace.cofu.repository.FuelType
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val paymentMethodRepository: PaymentMethodRepository,
    private val sharedPreferencesRepository: SharedPreferencesRepository
) : ViewModel() {

    var currentIndex by mutableIntStateOf(0)

    fun nextStep() {
        currentIndex++
    }

    fun navigateToAuthorization() {
        currentIndex = OnboardingPage.AUTHENTICATION.ordinal
    }

    fun setFuelType(fuelType: FuelType) {
        sharedPreferencesRepository.putValue(PREF_KEY_FUEL_TYPE, fuelType.ordinal)
        nextStep()
    }

    suspend fun skipPageIfNeeded(currentPage: OnboardingPage?, context: Context) {
        when (currentPage) {
            OnboardingPage.LOCATION_PERMISSION -> {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    nextStep()
                }
            }

            OnboardingPage.AUTHENTICATION -> {
                if (userRepository.isAuthorizationValid()) {
                    nextStep()
                }
            }

            OnboardingPage.TWO_FACTOR -> {
                if (userRepository.isBiometricAuthenticationEnabled() && userRepository.isPINSet().getOrNull() == true) {
                    nextStep()
                }
            }

            OnboardingPage.PAYMENT_METHOD -> {
                val hasPaymentMethods = !paymentMethodRepository.getPaymentMethods().getOrNull().isNullOrEmpty()
                if (hasPaymentMethods) {
                    nextStep()
                }
            }

            else -> {}
        }
    }
}
