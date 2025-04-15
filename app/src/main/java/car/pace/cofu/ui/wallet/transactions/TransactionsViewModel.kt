package car.pace.cofu.ui.wallet.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import car.pace.cofu.data.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class TransactionsViewModel @Inject constructor(
    transactionRepository: TransactionRepository
) : ViewModel() {
    val items = transactionRepository.getTransactionPager().flow.cachedIn(viewModelScope)
}
