package car.pace.cofu.ui.onboarding.dialog

import androidx.core.os.bundleOf
import car.pace.cofu.R
import car.pace.cofu.core.events.FragmentEvent
import car.pace.cofu.core.events.ShowSnack
import car.pace.cofu.core.mvvm.BaseDialogFragment
import car.pace.cofu.core.mvvm.BaseViewModel
import car.pace.cofu.databinding.FragmentOnboardingBottomSheetBinding
import car.pace.cofu.ui.onboarding.OnboardingFragment
import kotlin.reflect.KClass

/**
 * Base class for onboarding bottom sheets.
 */
abstract class BaseOnboardingBottomSheetFragment<E : BaseViewModel>(clazz: KClass<E>) :
    BaseDialogFragment<FragmentOnboardingBottomSheetBinding, E>(
        R.layout.fragment_onboarding_bottom_sheet,
        clazz,
        dialogMode = DialogMode.BOTTOM_SHEET
    ) {

    override fun onHandleFragmentEvent(event: FragmentEvent) {
        when (event) {
            is BaseOnboardingBottomSheetViewModel.AuthorisationSetEvent -> {
                dismissDialogWithResult(
                    OnboardingFragment::class.java.simpleName, bundleOf(
                        OnboardingFragment.KEY_SUCCESSFUL to true
                    )
                )
            }
            is BaseOnboardingBottomSheetViewModel.SendingOTPFailedEvent -> {
                handleEvent(ShowSnack(messageRes = R.string.onboarding_network_error))
                dismiss()
            }
            else -> super.onHandleFragmentEvent(event)
        }
    }

}
