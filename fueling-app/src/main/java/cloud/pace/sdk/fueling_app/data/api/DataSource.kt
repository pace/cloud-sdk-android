package cloud.pace.sdk.fueling_app.data.api

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
import cloud.pace.sdk.poikit.poi.GasStation
import cloud.pace.sdk.utils.Completion
import okhttp3.ResponseBody
import java.io.File
import java.util.*

/**
 * This interface describes all data sources of the fueling process.
 */
interface DataSource {

    fun requestCofuGasStations(location: Location, radius: Int, completion: (Completion<List<GasStation>>) -> Unit)
    fun approachingAtTheForeCourt(gasStationId: String, completion: (Completion<ApproachingResponse>) -> Unit)
    fun getPump(gasStationId: String, pumpId: String, completion: (Completion<PumpResponse>) -> Unit)
    fun waitOnPumpStatusChange(gasStationId: String, pumpId: String, lastStatus: WaitOnPumpStatusChangeAPI.LastStatus?, completion: (Completion<PumpResponse>) -> Unit)
    fun getTransaction(transactionId: String, completion: (Completion<Transaction>) -> Unit)
    fun cancelPreAuth(gasStationId: String, transactionId: String, completion: (Completion<ResponseBody>) -> Unit)
    fun authorizePayment(paymentMethodId: String, amount: Double, purposePRNs: List<String>, currency: String = "EUR", otp: String? = null, completion: (Completion<PaymentToken>) -> Unit)
    fun processPostPayPayment(
        gasStationId: String,
        pumpId: String,
        paymentToken: String,
        transactionId: String = UUID.randomUUID().toString(),
        completion: (Completion<ProcessPaymentResponse>) -> Unit
    )

    fun processPreAuthPayment(gasStationId: String, pumpId: String, paymentToken: String, transactionId: String = UUID.randomUUID().toString(), completion: (Completion<ResponseBody>) -> Unit)
    fun isPinOrPasswordSet(completion: (Completion<PinOrPassword>) -> Unit)
    fun createOTPWithPin(pin: String, completion: (Completion<CreateOTP>) -> Unit)
    fun createOTPWithPassword(password: String, completion: (Completion<CreateOTP>) -> Unit)
    fun sendMail(completion: (Completion<Boolean>) -> Unit)
    fun createTOTP(mailOtp: String, completion: (Completion<DeviceTOTP>) -> Unit)
    fun getReceipt(context: Context, transactionId: String, completion: (Completion<File>) -> Unit)
}
