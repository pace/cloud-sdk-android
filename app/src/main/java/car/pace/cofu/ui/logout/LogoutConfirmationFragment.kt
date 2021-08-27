package car.pace.cofu.ui.logout

import androidx.lifecycle.lifecycleScope
import car.pace.cofu.R
import car.pace.cofu.core.events.FragmentEvent
import car.pace.cofu.core.events.NavigateToDirection
import car.pace.cofu.core.events.ShowSnack
import car.pace.cofu.core.mvvm.BaseDialogFragment
import car.pace.cofu.core.navigation.navigate
import car.pace.cofu.databinding.FragmentLogoutConfirmationBinding
import cloud.pace.sdk.idkit.IDKit
import cloud.pace.sdk.utils.Failure
import cloud.pace.sdk.utils.Success
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// Workaround for https://github.com/google/dagger/issues/1904
abstract class BaseLogoutConfirmationFragment :
    BaseDialogFragment<FragmentLogoutConfirmationBinding, LogoutConfirmationViewModel>(
        R.layout.fragment_logout_confirmation,
        LogoutConfirmationViewModel::class,
        dialogMode = DialogMode.BOTTOM_SHEET
    )

@AndroidEntryPoint
class LogoutConfirmationFragment : BaseLogoutConfirmationFragment() {

    override fun onHandleFragmentEvent(event: FragmentEvent) {
        when (event) {
            is LogoutConfirmationViewModel.DismissDialogEvent -> dismiss()
            is LogoutConfirmationViewModel.LogoutEvent -> logoutAndResetAppData()
            else -> super.onHandleFragmentEvent(event)
        }
    }

    private fun logoutAndResetAppData() {
        viewModel.isLoading.set(true)

        lifecycleScope.launch(Dispatchers.Main) {
            viewModel.isLoading.set(false)
            IDKit.endSession(this@LogoutConfirmationFragment) {
                when (it) {
                    is Success -> onLogoutSuccessful()
                    is Failure -> handleEvent(ShowSnack(messageRes = R.string.onboarding_network_error))
                }
            }
        }
    }

    private fun onLogoutSuccessful() {
        viewModel.resetAppData()
        navigate(
            NavigateToDirection(
                LogoutConfirmationFragmentDirections.redirectBackToOnboarding(true),
                clearBackStack = true
            )
        )
    }
}


