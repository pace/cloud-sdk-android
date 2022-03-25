package cloud.pace.sdk.fueling_app.ui.payment_method

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import cloud.pace.sdk.api.fueling.generated.model.ApproachingResponse
import cloud.pace.sdk.fueling_app.data.model.Pump
import cloud.pace.sdk.fueling_app.data.repository.Repository
import cloud.pace.sdk.fueling_app.util.Result
import cloud.pace.sdk.fueling_app.util.asSafeArgsPaymentMethod
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class PaymentMethodsViewModel @Inject constructor(
    private val repository: Repository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val gasStationId = PaymentMethodsFragmentArgs.fromSavedStateHandle(savedStateHandle).gasStation.id

    val pumps by lazy { MutableLiveData<Array<Pump>>() }
    val paymentMethods = liveData {
        emit(Result.Loading)

        try {
            val approachingResponse = Result.Success(repository.approachingAtTheForeCourt(gasStationId))
            val supportedUserPaymentMethods = approachingResponse.data.getPaymentMethods().map { it.asSafeArgsPaymentMethod() to true }
            // With the unsupported payment methods of the user can not be paid at the gas station and should be marked accordingly in the app
            val unsupportedUserPaymentMethods = approachingResponse.data.getUnsupportedPaymentMethods().map { it.asSafeArgsPaymentMethod() to false }
            val result = (supportedUserPaymentMethods + unsupportedUserPaymentMethods).sortedByDescending { it.second }

            setPumps(approachingResponse.data)
            emit(Result.Success(result))
        } catch (e: Exception) {
            emit(Result.Error(e))
        }
    }

    private fun setPumps(approachingResponse: ApproachingResponse) {
        pumps.value = approachingResponse
            .getGasStation()
            .getPumps()
            .sortedBy { it.identifier?.toIntOrNull() }
            .filterNotNull()
            .map { Pump(it.id, it.identifier) }
            .toTypedArray()
    }
}
