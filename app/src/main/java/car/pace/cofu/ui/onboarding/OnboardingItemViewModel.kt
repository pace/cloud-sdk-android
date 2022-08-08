package car.pace.cofu.ui.onboarding

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.databinding.ObservableArrayList
import car.pace.cofu.R
import car.pace.cofu.core.events.FragmentEvent
import car.pace.cofu.core.mvvm.BaseItemViewModel
import car.pace.cofu.repository.FuelType

abstract class OnboardingItemViewModel(internal val parent: OnboardingViewModel) :
    BaseItemViewModel() {
    override val layoutId = R.layout.item_onboarding

    abstract val imageRes: Int?
    abstract val textRes: Int
    abstract val titleRes: Int
    abstract val isFuelTypeSelection: Boolean

    override val item: Any get() = textRes

    val buttons = ObservableArrayList<OnboardingButtonViewModel>()

    /**
     * gets called from the [OnboardingViewModel] when a response from the fragment is available
     * e.g. the fingerprint authorisation has been set up or the location permission has been granted
     * or denied. The default implementation is empty.
     */
    open fun onResponse(response: FragmentEvent) {
        // to be overridden by item viewmodels
    }

    /**
     * called when this onboarding step is displayed
     * each step should check initially whether the step can be skipped, e.g. if the location
     * permission was already granted
     * @param skipIfRedundant if set to true, you should call [OnboardingViewModel.next] if this
     * step can be skipped. Will be set to false when the user actively navigated back
     */
    abstract fun onInit(skipIfRedundant: Boolean)

    /**
     * called when radio buttons for fuel type selection are clicked
     * updates current selected fuel type [OnboardingViewmodel.fuelType]
     */
    open fun onDieselSelected() {
        parent.fuelType = FuelType.DIESEL
    }

    open fun onSuperSelected() {
        parent.fuelType = FuelType.SUPER
    }

    open fun onSuperE10Selected() {
        parent.fuelType = FuelType.E10
    }

    open fun onSuperPlusSelected() {
        parent.fuelType = FuelType.SUPER_PLUS
    }
}

open class OnboardingButtonViewModel(
    internal val parent: OnboardingItemViewModel,
    @StringRes val textRes: Int,
    private val onClick: () -> Unit,
    isPrimary: Boolean = true
) : BaseItemViewModel() {

    override val item: Any get() = textRes

    override val layoutId =
        if (isPrimary) R.layout.item_onboarding_button else R.layout.item_onboarding_text_button

    fun onClick() {
        if (parent.parent.loading.get() == 0) onClick.invoke()
    }
}

class OnboardingImageButtonViewModel(
    parent: OnboardingItemViewModel,
    @StringRes textRes: Int,
    @DrawableRes val imageRes: Int,
    onClick: () -> Unit,
) : OnboardingButtonViewModel(parent, textRes, onClick, true) {
    override val layoutId = R.layout.item_onboarding_image_button
}

/**
 * Can be used when an onboarding screen has no "skip" button, but the buttons above it
 * should be put at the same position
 */
class OnboardingPlaceholderButtonViewModel(
    parent: OnboardingItemViewModel,
) : OnboardingButtonViewModel(parent, 0, {}, true) {
    override val layoutId = R.layout.item_onboarding_placeholder
}