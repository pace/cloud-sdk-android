package car.pace.cofu.ui.logout

import androidx.databinding.ObservableBoolean
import car.pace.cofu.core.events.FragmentEvent
import car.pace.cofu.core.mvvm.BaseViewModel
import car.pace.cofu.repository.UserDataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class LogoutConfirmationViewModel @Inject constructor(internal val userDataRepository: UserDataRepository) :
    BaseViewModel() {

    val isLoading = ObservableBoolean()

    fun resetAppData() {
        userDataRepository.clear()
    }

    fun logout() {
        if (isLoading.get()) return
        handleEvent(LogoutEvent())
    }

    fun cancel() {
        if (isLoading.get()) return
        handleEvent(DismissDialogEvent())
    }

    class DismissDialogEvent : FragmentEvent()
    class LogoutEvent : FragmentEvent()
}
