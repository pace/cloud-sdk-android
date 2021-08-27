package car.pace.cofu.ui.onboarding

import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableInt
import car.pace.cofu.core.events.DismissSnackbars
import car.pace.cofu.core.events.FragmentEvent
import car.pace.cofu.core.mvvm.BaseViewModel
import car.pace.cofu.core.util.decrease
import car.pace.cofu.core.util.increase
import car.pace.cofu.repository.UserDataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(internal val userDataRepository: UserDataRepository) :
    BaseViewModel() {

    val loading = ObservableInt(0)

    private val paceAuthorisationItemViewModel =
        PaceAuthorisationItemViewModel(this@OnboardingViewModel)

    var hasFingerprint = false
        set(value) {
            field = value
            paceAuthorisationItemViewModel.hasFingerprint = value
        }

    var isSmallDevice = false
        set(value) {
            field = value
            fuelTypeSelectionItemViewModel.isSmallDevice = value
        }

    val fuelTypeSelectionItemViewModel = FuelTypeSelectionViewModel(this@OnboardingViewModel)
    val pagerItems = mutableListOf<OnboardingItemViewModel>().apply {
        add(LocationPermissionItemViewModel(this@OnboardingViewModel))
        add(PaceIdItemViewModel(this@OnboardingViewModel))
        add(paceAuthorisationItemViewModel)
        add(PaymentMethodItemViewModel(this@OnboardingViewModel))
        add(fuelTypeSelectionItemViewModel)
    }

    val showBackButton = ObservableBoolean(false)

    val selectedPage = object : ObservableInt(0) {
        override fun set(value: Int) {
            val didGoForward = value > get()
            super.set(value)
            showBackButton.set(value > 0)
            handleEvent(DismissSnackbars())
            // check if the current step can be skipped, but only if the user did not just go back
            initStep(didGoForward)
        }
    }

    private fun initStep(skipIfRedundant: Boolean) {
        pagerItems[selectedPage.get()].onInit(skipIfRedundant)
    }


    /**
     * advances to the next onboarding step or finishes onboarding when the last step has been reached
     */
    fun next() {
        if (selectedPage.get() < pagerItems.size - 1) {
            selectedPage.increase()
        } else {
            userDataRepository.onboardingDone = true
            handleEvent(NavigateToHomeEvent())
        }
    }

    fun previous() {
        if (loading.get() == 0) selectedPage.decrease()
    }

    /**
     * gets called from the [OnboardingFragment] when a response is available
     * e.g. the fingerprint authorisation has been set up or the location permission has been granted
     * or denied. The response is forwarded to [OnboardingItemViewModel.onResponse] for the
     * currently active item viewmodel
     */
    fun onResponse(response: FragmentEvent) {
        pagerItems[selectedPage.get()].onResponse(response)
    }

    init {
        initStep(true)
    }
}