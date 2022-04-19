package cloud.pace.sdk.fueling_app.data.repository

import android.content.Context
import android.location.Location
import cloud.pace.sdk.api.fueling.generated.model.ApproachingResponse
import cloud.pace.sdk.api.fueling.generated.model.ProcessPaymentResponse
import cloud.pace.sdk.api.fueling.generated.model.PumpResponse
import cloud.pace.sdk.api.fueling.generated.request.fueling.WaitOnPumpStatusChangeAPI
import cloud.pace.sdk.api.pay.generated.model.PaymentToken
import cloud.pace.sdk.api.pay.generated.model.Transaction
import cloud.pace.sdk.api.user.generated.model.CreateOTP
import cloud.pace.sdk.api.user.generated.model.DeviceTOTP
import cloud.pace.sdk.api.user.generated.model.PinOrPassword
import cloud.pace.sdk.fueling_app.data.api.DataSource
import cloud.pace.sdk.poikit.poi.GasStation
import cloud.pace.sdk.utils.resume
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.ResponseBody
import java.io.File
import javax.inject.Inject

class Repository @Inject constructor(
    private val dataSource: DataSource
) {

    suspend fun requestCofuGasStations(location: Location, radius: Int) =
        suspendCancellableCoroutine<List<GasStation>> { continuation ->
            dataSource.requestCofuGasStations(location, radius) {
                continuation.resume(it)
            }
        }

    suspend fun approachingAtTheForeCourt(gasStationId: String) =
        suspendCancellableCoroutine<ApproachingResponse> { continuation ->
            dataSource.approachingAtTheForeCourt(gasStationId) {
                continuation.resume(it)
            }
        }

    suspend fun getPump(gasStationId: String, pumpId: String) =
        suspendCancellableCoroutine<PumpResponse> { continuation ->
            dataSource.getPump(gasStationId, pumpId) {
                continuation.resume(it)
            }
        }

    suspend fun waitOnPumpStatusChange(gasStationId: String, pumpId: String, lastStatus: WaitOnPumpStatusChangeAPI.LastStatus?) =
        suspendCancellableCoroutine<PumpResponse> { continuation ->
            dataSource.waitOnPumpStatusChange(gasStationId, pumpId, lastStatus) {
                continuation.resume(it)
            }
        }

    suspend fun getTransaction(transactionId: String) =
        suspendCancellableCoroutine<Transaction> { continuation ->
            dataSource.getTransaction(transactionId) {
                continuation.resume(it)
            }
        }

    suspend fun cancelPreAuth(gasStationId: String, transactionId: String) =
        suspendCancellableCoroutine<ResponseBody> { continuation ->
            dataSource.cancelPreAuth(gasStationId, transactionId) {
                continuation.resume(it)
            }
        }

    suspend fun authorizePayment(paymentMethodId: String, amount: Double, purposePRNs: List<String>, otp: String? = null, currency: String = "EUR") =
        suspendCancellableCoroutine<PaymentToken> { continuation ->
            dataSource.authorizePayment(paymentMethodId, amount, purposePRNs, currency, otp) {
                continuation.resume(it)
            }
        }

    suspend fun processPostPayPayment(gasStationId: String, pumpId: String, paymentToken: String, transactionId: String) =
        suspendCancellableCoroutine<ProcessPaymentResponse> { continuation ->
            dataSource.processPostPayPayment(gasStationId, pumpId, paymentToken, transactionId) {
                continuation.resume(it)
            }
        }

    suspend fun processPreAuthPayment(gasStationId: String, pumpId: String, paymentToken: String, transactionId: String) =
        suspendCancellableCoroutine<ResponseBody> { continuation ->
            dataSource.processPreAuthPayment(gasStationId, pumpId, paymentToken, transactionId) {
                continuation.resume(it)
            }
        }

    suspend fun isPinOrPasswordSet() =
        suspendCancellableCoroutine<PinOrPassword> { continuation ->
            dataSource.isPinOrPasswordSet {
                continuation.resume(it)
            }
        }

    suspend fun createOTPWithPin(pin: String) =
        suspendCancellableCoroutine<CreateOTP> { continuation ->
            dataSource.createOTPWithPin(pin) {
                continuation.resume(it)
            }
        }

    suspend fun createOTPWithPassword(password: String) =
        suspendCancellableCoroutine<CreateOTP> { continuation ->
            dataSource.createOTPWithPassword(password) {
                continuation.resume(it)
            }
        }

    suspend fun sendMail() =
        suspendCancellableCoroutine<Boolean> { continuation ->
            dataSource.sendMail {
                continuation.resume(it)
            }
        }

    suspend fun createTOTP(mailOtp: String) =
        suspendCancellableCoroutine<DeviceTOTP> { continuation ->
            dataSource.createTOTP(mailOtp) {
                continuation.resume(it)
            }
        }

    suspend fun getReceipt(context: Context, transactionId: String) =
        suspendCancellableCoroutine<File> { continuation ->
            dataSource.getReceipt(context, transactionId) {
                continuation.resume(it)
            }
        }
}
