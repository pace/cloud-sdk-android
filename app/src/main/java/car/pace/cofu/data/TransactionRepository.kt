package car.pace.cofu.data

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingSource
import androidx.paging.PagingState
import car.pace.cofu.ui.wallet.transactions.Transaction
import car.pace.cofu.ui.wallet.transactions.toTransaction
import car.pace.cofu.util.RequestUtils.getHeaders
import cloud.pace.sdk.api.API
import cloud.pace.sdk.api.pay.PayAPI.paymentTransactions
import cloud.pace.sdk.api.pay.generated.request.paymentTransactions.GetReceiptByFormatAPI
import cloud.pace.sdk.api.pay.generated.request.paymentTransactions.GetReceiptByFormatAPI.getReceiptByFormat
import cloud.pace.sdk.api.pay.generated.request.paymentTransactions.GetTransactionAPI.getTransaction
import cloud.pace.sdk.api.pay.generated.request.paymentTransactions.ListTransactionsAPI
import cloud.pace.sdk.api.pay.generated.request.paymentTransactions.ListTransactionsAPI.listTransactions
import javax.inject.Inject
import javax.inject.Singleton
import okhttp3.ResponseBody
import retrofit2.await

@Singleton
class TransactionRepository @Inject constructor() {

    suspend fun getTransaction(id: String): Result<Transaction> = runCatching {
        API.paymentTransactions.getTransaction(transactionId = id, additionalHeaders = getHeaders()).await().toTransaction() ?: throw Exception("Transaction cast failed")
    }

    suspend fun getTransactionReceipt(id: String, format: GetReceiptByFormatAPI.FileFormat): Result<ResponseBody> = runCatching {
        API.paymentTransactions.getReceiptByFormat(transactionID = id, fileFormat = format, additionalHeaders = getHeaders()).await()
    }

    fun getTransactionPager() = Pager(
        config = PagingConfig(pageSize = PAGE_SIZE),
        pagingSourceFactory = { TransactionPagingSource() }
    )

    private suspend fun getTransactions(paymentMethodId: String? = null, pageNumber: Int? = null, pageSize: Int? = null): Result<List<Transaction>> = runCatching {
        API.paymentTransactions.listTransactions(
            sort = ListTransactionsAPI.Sort.CREATEDATDESCENDING,
            filterpaymentMethodId = paymentMethodId,
            pagenumber = pageNumber,
            pagesize = pageSize
        ).await().mapNotNull { it.toTransaction() }
    }

    inner class TransactionPagingSource : PagingSource<Int, Transaction>() {
        override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Transaction> {
            return try {
                val currentPage = params.key ?: 0
                val transactions = getTransactions(pageNumber = currentPage, pageSize = params.loadSize).getOrNull() ?: throw Exception("Transactions call failed")
                LoadResult.Page(
                    data = transactions,
                    prevKey = if (currentPage == 0) null else currentPage - 1,
                    nextKey = if (transactions.isEmpty()) null else currentPage + 1
                )
            } catch (exception: Exception) {
                return LoadResult.Error(exception)
            }
        }

        override fun getRefreshKey(state: PagingState<Int, Transaction>): Int? {
            return state.anchorPosition
        }
    }

    companion object {
        const val PAGE_SIZE = 25
    }
}
