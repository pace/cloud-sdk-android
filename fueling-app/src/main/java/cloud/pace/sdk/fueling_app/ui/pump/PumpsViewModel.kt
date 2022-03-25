package cloud.pace.sdk.fueling_app.ui.pump

import androidx.lifecycle.*
import cloud.pace.sdk.api.fueling.generated.model.PumpResponse
import cloud.pace.sdk.fueling_app.data.model.Pump
import cloud.pace.sdk.fueling_app.data.repository.Repository
import cloud.pace.sdk.fueling_app.util.Result
import cloud.pace.sdk.fueling_app.util.asSafeArgsPumpResponse
import cloud.pace.sdk.utils.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class PumpsViewModel @Inject constructor(
    private val repository: Repository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val args = PumpsFragmentArgs.fromSavedStateHandle(savedStateHandle)

    val selectedPump by lazy { MutableLiveData<Pump>() }
    val navigateTo = selectedPump.switchMap {
        liveData {
            emit(Event(Result.Loading))

            try {
                val pumpResponse = repository.getPump(args.gasStation.id, it.id)
                val direction = if (pumpResponse.fuelingProcess != PumpResponse.FuelingProcess.POSTPAY && pumpResponse.status == PumpResponse.Status.LOCKED) {
                    // Pump is a pre auth pump and needs to be unlocked first
                    PumpsFragmentDirections.actionPumpsFragmentToAmountFragment(args.gasStation, args.paymentMethod, it, pumpResponse.asSafeArgsPumpResponse())
                } else {
                    // Pump is a post pay pump (no unlocking necessary) OR a unlocked pre auth pump
                    PumpsFragmentDirections.actionPumpsFragmentToStatusFragment(args.gasStation, it, args.paymentMethod)
                }
                emit(Event(Result.Success(direction)))
            } catch (e: Exception) {
                emit(Event(Result.Error(e)))
            }
        }
    }
}
