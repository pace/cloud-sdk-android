package car.pace.cofu.data

import car.pace.cofu.util.RequestUtils.getHeaders
import cloud.pace.sdk.api.API
import cloud.pace.sdk.api.pay.PayAPI.paymentTransactions
import cloud.pace.sdk.api.pay.generated.model.Transaction
import cloud.pace.sdk.api.pay.generated.request.paymentTransactions.GetReceiptByFormatAPI
import cloud.pace.sdk.api.pay.generated.request.paymentTransactions.GetReceiptByFormatAPI.getReceiptByFormat
import cloud.pace.sdk.api.pay.generated.request.paymentTransactions.GetTransactionAPI.getTransaction
import javax.inject.Inject
import javax.inject.Singleton
import okhttp3.ResponseBody
import retrofit2.await

@Singleton
class TransactionRepository @Inject constructor() {

    suspend fun getTransaction(id: String): Result<Transaction> = runCatching {
        API.paymentTransactions.getTransaction(transactionId = id, additionalHeaders = getHeaders()).await()
    }

    suspend fun getTransactionReceipt(id: String, format: GetReceiptByFormatAPI.FileFormat): Result<ResponseBody> = runCatching {
        API.paymentTransactions.getReceiptByFormat(transactionID = id, fileFormat = format, additionalHeaders = getHeaders()).await()
    }
}
