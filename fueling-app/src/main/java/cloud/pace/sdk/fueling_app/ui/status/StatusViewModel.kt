package cloud.pace.sdk.fueling_app.ui.status

import androidx.lifecycle.*
import cloud.pace.sdk.api.fueling.generated.model.PumpResponse
import cloud.pace.sdk.api.fueling.generated.request.fueling.WaitOnPumpStatusChangeAPI
import cloud.pace.sdk.api.pay.generated.model.Transaction
import cloud.pace.sdk.fueling_app.data.model.PostPay
import cloud.pace.sdk.fueling_app.data.model.PreAuth
import cloud.pace.sdk.fueling_app.data.model.PumpStatus
import cloud.pace.sdk.fueling_app.data.repository.Repository
import cloud.pace.sdk.fueling_app.util.Result
import cloud.pace.sdk.fueling_app.util.asSafeArgsPumpResponse
import cloud.pace.sdk.poikit.utils.ApiException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.net.HttpURLConnection
import javax.inject.Inject

@HiltViewModel
class StatusViewModel @Inject constructor(
    private val repository: Repository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val args = StatusFragmentArgs.fromSavedStateHandle(savedStateHandle)
    private val gasStationId = args.gasStation.id
    private val pumpId = args.pump.id
    private var transactionId: String? = null

    // Get the initial pump state
    private val getPumpResponse = liveData {
        emit(Result.Loading)

        try {
            emit(Result.Success(repository.getPump(gasStationId, pumpId)))
        } catch (e: Exception) {
            emit(Result.Error(e))
        }
    }

    private val waitOnPumpStatusChangeResponse by lazy { MutableLiveData<Result<Pair<PumpResponse, PumpResponse.Status?>>>() }
    private val getTransactionResponse by lazy { MutableLiveData<Result<Transaction>>() }
    private val cancelPreAuthResponse by lazy { MutableLiveData<Result<Boolean>>() }

    val pumpStatus = MediatorLiveData<Result<PumpStatus>>().apply {
        addSource(getPumpResponse) {
            this.value = when (it) {
                is Result.Loading -> Result.Loading
                is Result.Success -> handlePumpResponse(it.data, null)
                is Result.Error -> Result.Error(it.exception)
            }
        }
        addSource(waitOnPumpStatusChangeResponse) {
            this.value = when (it) {
                is Result.Loading -> Result.Loading
                is Result.Success -> handlePumpResponse(it.data.first, it.data.second)
                is Result.Error -> Result.Error(it.exception)
            }
        }
        addSource(getTransactionResponse) {
            this.value = when (it) {
                is Result.Loading -> Result.Loading
                is Result.Success -> Result.Success(PreAuth.Done(it.data.id))
                is Result.Error -> Result.Error(it.exception)
            }
        }
        addSource(cancelPreAuthResponse) {
            this.value = when (it) {
                is Result.Loading -> Result.Loading
                is Result.Success -> Result.Success(PreAuth.Canceled(it.data))
                is Result.Error -> Result.Error(it.exception)
            }
        }
    }

    /**
     * You should cancel the pre auth when the user goes back and has not yet refueled (pump status is free).
     */
    fun cancelPreAuth() {
        viewModelScope.launch {
            val transactionId = transactionId
            if (transactionId != null) {
                cancelPreAuthResponse.value = Result.Loading
                cancelPreAuthResponse.value = try {
                    repository.cancelPreAuth(gasStationId, transactionId)
                    Result.Success(true)
                } catch (e: Exception) {
                    Result.Success(false)
                }
            } else {
                cancelPreAuthResponse.value = Result.Error(IllegalArgumentException("Transaction ID to cancel pre auth cannot be null"))
            }
        }
    }

    private fun waitOnPumpStatusChange(lastStatus: PumpResponse.Status?) {
        viewModelScope.launch {
            waitOnPumpStatusChangeResponse.value = try {
                val pumpResponse = repository.waitOnPumpStatusChange(gasStationId, pumpId, lastStatus?.name?.let { WaitOnPumpStatusChangeAPI.LastStatus.valueOf(it) })
                Result.Success(pumpResponse to lastStatus)
            } catch (e: Exception) {
                Result.Error(e)
            }
        }
    }

    /**
     * Call getTransaction until it returns successfully to know that the pre auth process is finished
     */
    private fun getTransaction() {
        viewModelScope.launch {
            val transactionId = transactionId
            if (transactionId != null) {
                try {
                    val transaction = repository.getTransaction(transactionId)
                    getTransactionResponse.value = Result.Success(transaction)
                } catch (e: Exception) {
                    if (e is ApiException && e.errorCode == HttpURLConnection.HTTP_NOT_FOUND) {
                        getTransaction()
                    } else if (e is ApiException && e.errorCode == HttpURLConnection.HTTP_GONE) {
                        cancelPreAuthResponse.value = Result.Success(true)
                    } else {
                        getTransactionResponse.value = Result.Error(e)
                    }
                }
            } else {
                getTransactionResponse.value = Result.Error(IllegalArgumentException("Transaction ID to cancel pre auth cannot be null"))
            }
        }
    }

    private fun handlePumpResponse(pump: PumpResponse, lastStatus: PumpResponse.Status?): Result<PumpStatus> {
        return if (pump.fuelingProcess == PumpResponse.FuelingProcess.POSTPAY) {
            /**
             * POST PAY FLOW:
             */
            when (pump.status) {
                PumpResponse.Status.FREE -> {
                    // Pump is free -> Wait on pump status change
                    waitOnPumpStatusChange(pump.status)
                    Result.Success(PostPay.Free)
                }
                PumpResponse.Status.INUSE -> {
                    // Pump is in use -> Wait on pump status change
                    waitOnPumpStatusChange(pump.status)
                    Result.Success(PostPay.InUse)
                }
                PumpResponse.Status.READYTOPAY -> {
                    // Pump is ready to pay -> Show pay screen
                    Result.Success(PostPay.ReadyToPay(pump.asSafeArgsPumpResponse()))
                }
                PumpResponse.Status.OUTOFORDER -> {
                    // Pump is out of order -> Show error
                    Result.Success(PostPay.OutOfOrder)
                }
                else -> Result.Error(IllegalStateException("Illegal post pay pump status: ${pump.status}"))
            }
        } else {
            /**
             * PRE AUTH FLOW:
             */
            val transactionId = pump.transactionId
            when {
                pump.status == PumpResponse.Status.INTRANSACTION || (transactionId == null && (pump.status == PumpResponse.Status.FREE || pump.status == PumpResponse.Status.INUSE)) -> {
                    // Someone else is currently fueling with PACE or a different system -> Show error + wait on pump status change
                    waitOnPumpStatusChange(pump.status)
                    Result.Success(PreAuth.InTransaction)
                }
                pump.status == PumpResponse.Status.LOCKED && lastStatus == PumpResponse.Status.INTRANSACTION -> {
                    // Pump was in use from someone else and is now locked for the current user -> Restart fueling process
                    Result.Success(PreAuth.Locked)
                }
                pump.status == PumpResponse.Status.OUTOFORDER -> {
                    // Pump is out of order -> Show error
                    Result.Success(PreAuth.OutOfOrder)
                }
                else -> {
                    // Pump is free/in use for the current user -> Get transaction + wait on pump status change
                    if (transactionId != null) {
                        this.transactionId = transactionId
                        getTransaction()
                        waitOnPumpStatusChange(pump.status)

                        when (pump.status) {
                            PumpResponse.Status.FREE -> Result.Success(PreAuth.Free)
                            PumpResponse.Status.INUSE -> Result.Success(PreAuth.InUse)
                            PumpResponse.Status.LOCKED -> Result.Loading
                            else -> Result.Error(IllegalStateException("Illegal pre auth pump status: ${pump.status}"))
                        }
                    } else {
                        // Transaction ID is null -> Show error
                        Result.Error(IllegalArgumentException("Pre auth transaction ID cannot be null"))
                    }
                }
            }
        }
    }
}
