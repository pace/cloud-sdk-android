package car.pace.cofu.ui.wallet.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.PagingData
import car.pace.cofu.ui.wallet.transactions.Transaction
import car.pace.cofu.ui.wallet.transactions.createTransaction

class TransactionsProvider : PreviewParameterProvider<PagingData<Transaction>> {
    private val transactionList = listOf(
        createTransaction(price = 39.99, stationName = "Tanke Emma"),
        createTransaction(price = 7.45, productName = "Diesel", currency = "USD", stationName = "Tankeschön"),
        createTransaction(price = 130.95, productName = "Diesel", stationName = "Gas what")
    )

    override val values = sequenceOf(
        // Data available
        PagingData.from(
            data = transactionList,
            sourceLoadStates = LoadStates(
                refresh = LoadState.NotLoading(false),
                append = LoadState.NotLoading(false),
                prepend = LoadState.NotLoading(false)
            )
        ),
        // No transactions available
        PagingData.from(
            data = listOf<Transaction>(),
            sourceLoadStates = LoadStates(
                refresh = LoadState.NotLoading(false),
                append = LoadState.NotLoading(false),
                prepend = LoadState.NotLoading(false)
            )
        ),
        // Loading
        PagingData.from(
            data = listOf<Transaction>(),
            sourceLoadStates = LoadStates(
                refresh = LoadState.Loading,
                append = LoadState.NotLoading(false),
                prepend = LoadState.NotLoading(false)
            )
        ),
        // Initial loading error
        PagingData.from(
            data = listOf<Transaction>(),
            sourceLoadStates = LoadStates(
                refresh = LoadState.Error(Throwable()),
                append = LoadState.NotLoading(false),
                prepend = LoadState.NotLoading(false)
            )
        ),
        // Append loading
        PagingData.from(
            data = transactionList,
            sourceLoadStates = LoadStates(
                refresh = LoadState.NotLoading(false),
                append = LoadState.Loading,
                prepend = LoadState.NotLoading(false)
            )
        ),
        // Append error
        PagingData.from(
            data = transactionList,
            sourceLoadStates = LoadStates(
                refresh = LoadState.NotLoading(false),
                append = LoadState.Error(Throwable()),
                prepend = LoadState.NotLoading(false)
            )
        )
    )
}
