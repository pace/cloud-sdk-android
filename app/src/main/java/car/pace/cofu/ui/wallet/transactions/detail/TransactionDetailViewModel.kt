package car.pace.cofu.ui.wallet.transactions.detail

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import car.pace.cofu.data.PaymentMethodRepository
import car.pace.cofu.data.TransactionRepository
import car.pace.cofu.util.Constants.STOP_TIMEOUT_MILLIS
import car.pace.cofu.util.FileUtils
import car.pace.cofu.util.IntentUtils
import car.pace.cofu.util.UiState
import car.pace.cofu.util.UiState.Loading.toUiState
import car.pace.cofu.util.data
import car.pace.cofu.util.extension.toPaymentMethodItem
import cloud.pace.sdk.api.pay.generated.request.paymentTransactions.GetReceiptByFormatAPI
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.File
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class TransactionDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val transactionRepository: TransactionRepository,
    paymentMethodRepository: PaymentMethodRepository
) : ViewModel() {

    private val id: String = checkNotNull(savedStateHandle["id"])
    private val refresh = MutableSharedFlow<Unit>(replay = 1).apply {
        tryEmit(Unit)
    }

    val transaction = refresh.mapLatest {
        transactionRepository.getTransaction(id).toUiState()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = UiState.Loading
    )

    val paymentMethod = transaction.mapLatest { transaction ->
        transaction.data?.paymentMethodId?.let { id ->
            paymentMethodRepository.getPaymentMethod(id).map { it.toPaymentMethodItem() }
        }?.toUiState() ?: UiState.Error(Exception("Could not retrieve payment method ID"))
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = UiState.Loading
    )

    val receiptImage = refresh.mapLatest {
        transactionRepository.getTransactionReceipt(id, GetReceiptByFormatAPI.FileFormat.PNG).mapCatching {
            val ba = it.byteStream()
            // might be null even though not declared as nullable
            BitmapFactory.decodeStream(ba) ?: throw Error("Receipt image decoded to null")
        }.toUiState()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = UiState.Loading
    )

    fun refresh() {
        viewModelScope.launch {
            refresh.emit(Unit)
        }
    }

    fun downloadTransaction(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            transactionRepository.getTransactionReceipt(id, GetReceiptByFormatAPI.FileFormat.PDF).mapCatching {
                val transactionsDir = File(context.filesDir.absolutePath, TRANSACTIONS_DIR)
                if (!transactionsDir.exists()) {
                    transactionsDir.mkdirs()
                }

                val file = File(transactionsDir, "transaction_$id.pdf").also { file ->
                    FileUtils.writeFile(it.byteStream(), file)
                }

                val shareIntent = IntentUtils.getShareFileIntent(context, file)
                context.startActivity(Intent.createChooser(shareIntent, null))
            }.onFailure(Timber::e)
        }
    }

    companion object {
        private const val TRANSACTIONS_DIR = "transactions"
    }
}
