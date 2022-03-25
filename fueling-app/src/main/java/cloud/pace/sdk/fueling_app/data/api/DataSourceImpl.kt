package cloud.pace.sdk.fueling_app.data.api

import android.content.Context
import android.location.Location
import cloud.pace.sdk.api.API
import cloud.pace.sdk.api.fueling.FuelingAPI.fueling
import cloud.pace.sdk.api.fueling.generated.model.ApproachingResponse
import cloud.pace.sdk.api.fueling.generated.model.ProcessPaymentResponse
import cloud.pace.sdk.api.fueling.generated.model.PumpResponse
import cloud.pace.sdk.api.fueling.generated.model.TransactionRequest
import cloud.pace.sdk.api.fueling.generated.request.fueling.ApproachingAtTheForecourtAPI.approachingAtTheForecourt
import cloud.pace.sdk.api.fueling.generated.request.fueling.CancelPreAuthAPI.cancelPreAuth
import cloud.pace.sdk.api.fueling.generated.request.fueling.GetPumpAPI.getPump
import cloud.pace.sdk.api.fueling.generated.request.fueling.ProcessPaymentAPI.processPayment
import cloud.pace.sdk.api.fueling.generated.request.fueling.WaitOnPumpStatusChangeAPI
import cloud.pace.sdk.api.fueling.generated.request.fueling.WaitOnPumpStatusChangeAPI.waitOnPumpStatusChange
import cloud.pace.sdk.api.pay.PayAPI.paymentTokens
import cloud.pace.sdk.api.pay.PayAPI.paymentTransactions
import cloud.pace.sdk.api.pay.ProcessPreAuthPaymentAPI.processPreAuthPayment
import cloud.pace.sdk.api.pay.generated.model.PaymentToken
import cloud.pace.sdk.api.pay.generated.model.PaymentTokenCreateBody
import cloud.pace.sdk.api.pay.generated.model.Transaction
import cloud.pace.sdk.api.pay.generated.request.paymentTokens.AuthorizePaymentTokenAPI
import cloud.pace.sdk.api.pay.generated.request.paymentTokens.AuthorizePaymentTokenAPI.authorizePaymentToken
import cloud.pace.sdk.api.pay.generated.request.paymentTransactions.GetReceiptAPI.getReceipt
import cloud.pace.sdk.api.pay.generated.request.paymentTransactions.GetTransactionAPI
import cloud.pace.sdk.api.pay.generated.request.paymentTransactions.GetTransactionAPI.getTransaction
import cloud.pace.sdk.api.user.UserAPI.totp
import cloud.pace.sdk.api.user.generated.model.CreateOTP
import cloud.pace.sdk.api.user.generated.model.DeviceTOTP
import cloud.pace.sdk.api.user.generated.model.DeviceTOTPBody
import cloud.pace.sdk.api.user.generated.model.PinOrPassword
import cloud.pace.sdk.api.user.generated.request.totp.CreateOTPAPI.createOTP
import cloud.pace.sdk.api.user.generated.request.totp.CreateTOTPAPI
import cloud.pace.sdk.api.user.generated.request.totp.CreateTOTPAPI.createTOTP
import cloud.pace.sdk.fueling_app.util.ProductDeniedException
import cloud.pace.sdk.idkit.IDKit
import cloud.pace.sdk.poikit.POIKit
import cloud.pace.sdk.poikit.poi.GasStation
import cloud.pace.sdk.poikit.utils.ApiException
import cloud.pace.sdk.utils.*
import okhttp3.ResponseBody
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.util.*
import javax.inject.Inject

/**
 * This class implements the [data source interface][cloud.pace.sdk.fueling_app.data.api.DataSource] and is responsible for calling the respective API requests and processing its response data.
 * More information about the fueling process can be found in the [Developer Documentation](https://docs.pace.cloud/basics/flows/fueling).
 */
class DataSourceImpl @Inject constructor() : DataSource {

    /**
     * Returns a list of Connected Fueling gas stations within the [radius] of the specified [location].
     *
     * @param location The center of the search radius.
     * @param radius The search radius in meters.
     * @param completion Returns a list of [GasStation]s where Connected Fueling is available on success or a [Throwable] on failure.
     */
    override fun requestCofuGasStations(location: Location, radius: Int, completion: (Completion<List<GasStation>>) -> Unit) {
        POIKit.requestCofuGasStations(location, radius, completion)
    }

    /**
     * Gather information when approaching at the forecourt
     * This request will:
     * - Return a list of paymentMethods of the user which can be used at the gas station.
     * - Return up-to-date price information (price structure) at the gas station.
     * - Return a list of pumps available at the gas station together with the current status (free, inUse, readyToPay, outOfOrder).
     *
     * No pumps might be returned if the list of payment methods is empty.
     * The approaching is a necessary first API call for Connected Fueling.
     * Without a valid approaching the [getPump] and [waitOnPumpStatusChange] calls may be answered with a 403 Forbidden status code.
     *
     * @param gasStationId Gas station ID.
     * @param completion Returns an [ApproachingResponse] on success or a [Throwable] on failure.
     *
     * See also: [API docs](https://developer.pace.cloud/api/fueling?version=2021-2#operation/ApproachingAtTheForecourt)
     */
    override fun approachingAtTheForeCourt(gasStationId: String, completion: (Completion<ApproachingResponse>) -> Unit) {
        API.fueling.approachingAtTheForecourt(gasStationId).handleCallback(completion)
    }

    /**
     * Returns the current pump status (free, inUse, readyToPay, outOfOrder) and identifier.
     *
     * @param gasStationId Gas station ID.
     * @param pumpId Pump ID.
     * @param completion Returns a [PumpResponse] on success or a [Throwable] on failure.
     *
     * See also: [API docs](https://developer.pace.cloud/api/fueling?version=2021-2#operation/GetPump)
     */
    override fun getPump(gasStationId: String, pumpId: String, completion: (Completion<PumpResponse>) -> Unit) {
        API.fueling.getPump(gasStationId, pumpId).handleCallback(completion)
    }

    /**
     * Uses long polling to wait for a status change on a given pump.
     * Returns as soon as the status has changed or after the number of seconds provided by the optional timeout query parameter (default timeout is 30 seconds).
     * In case of timeout (408 status code) you're safe to start the request again. Instantaneously returns if lastStatus was given and already changed between request.
     * Should only be used after approaching, otherwise it returns 403 Forbidden.
     *
     * @param gasStationId Gas station ID.
     * @param pumpId Pump ID.
     * @param lastStatus The status of the last [waitOnPumpStatusChange] or [getPump] call.
     * @param completion Returns a [PumpResponse] on success or a [Throwable] on failure.
     *
     * See also: [API docs](https://developer.pace.cloud/api/fueling?version=2021-2#operation/WaitOnPumpStatusChange)
     */
    override fun waitOnPumpStatusChange(gasStationId: String, pumpId: String, lastStatus: WaitOnPumpStatusChangeAPI.LastStatus?, completion: (Completion<PumpResponse>) -> Unit) {
        API.fueling.waitOnPumpStatusChange(gasStationId, pumpId, WaitOnPumpStatusChangeAPI.Update.LONGPOLLING, lastStatus, 120, 120).handleCallback(completion)
    }

    /**
     * Endpoint for fetching information about a single transaction. Only completed transactions will be returned.
     *
     * Note: We use this request to know when a pre auth fueling process has finished.
     *
     * @param completion Returns a completed [Transaction] on success or a [Throwable] on failure.
     *
     * See also: [API docs](https://developer.pace.cloud/api/payment?version=2021-2#operation/GetTransaction)
     */
    override fun getTransaction(transactionId: String, completion: (Completion<Transaction>) -> Unit) {
        API.paymentTransactions.getTransaction(transactionId, GetTransactionAPI.Update.LONGPOLLING, 30).handleCallback(completion)
    }

    /**
     * This request cancels a pre auth transaction.
     * This action is only permitted in case the user didn't already start the fueling process (i.e. [pump status][PumpResponse.status] != [free][PumpResponse.Status.FREE]).
     * Returns 403 Forbidden in case the fueling process has already been started.
     *
     * @param gasStationId Gas station ID.
     * @param transactionId Transaction ID from pump response [PumpResponse].
     * @param completion Returns a [ResponseBody] on success or a [Throwable] on failure.
     *
     * See also: [API docs](https://developer.pace.cloud/api/fueling?version=2021-2#operation/CancelPreAuth)
     */
    override fun cancelPreAuth(gasStationId: String, transactionId: String, completion: (Completion<ResponseBody>) -> Unit) {
        API.fueling.cancelPreAuth(gasStationId, transactionId).handleCallback(completion)
    }

    /**
     * Authorizes a payment using the specified payment method.
     *
     * @param paymentMethodId Payment method ID.
     * @param amount Amount, for which the payment should be authorized.
     * @param purposePRNs PACE resource name(s) of one or multiple resources, for which the payment should be authorized.
     * @param currency Currency of the amount as specified in ISO-4217.
     * @param otp One time password for the authorization (if two factor is required by the payment method).
     * @param completion Returns a [PaymentToken] on success or a [Throwable] on failure.
     *
     * See also: [API docs](https://developer.pace.cloud/api/payment?version=2021-2#operation/AuthorizePaymentToken)
     */
    override fun authorizePayment(paymentMethodId: String, amount: Double, purposePRNs: List<String>, currency: String, otp: String?, completion: (Completion<PaymentToken>) -> Unit) {
        val requestBody = AuthorizePaymentTokenAPI.Body().apply {
            data = PaymentTokenCreateBody().apply {
                type = PaymentTokenCreateBody.Type.PAYMENTTOKEN
                attributes = PaymentTokenCreateBody.Attributes().apply {
                    this.amount = amount
                    this.purposePRNs = purposePRNs
                    this.currency = currency
                    this.twoFactor = PaymentTokenCreateBody.Attributes.TwoFactor().apply {
                        this.otp = otp
                        this.method = "pin"
                    }
                }
            }
        }

        API.paymentTokens.authorizePaymentToken(paymentMethodId, requestBody).enqueue {
            onResponse = {
                val body = it.body()
                if (it.isSuccessful && body != null) {
                    completion(Success(body))
                } else {
                    // Here we check whether the authorization of the payment was rejected due to an incorrect fuel type.
                    // All other error types can be found in the API documentation (https://developer.pace.cloud/api/payment?version=2021-2#operation/AuthorizePaymentToken)
                    // and can be read out from the error body in the same way as in the following example:
                    val exception = if (it.code() == HttpURLConnection.HTTP_FORBIDDEN) {
                        try {
                            it.errorBody()?.string()?.let { errorBodyString ->
                                val code = JSONObject(errorBodyString).getJSONArray("errors").getJSONObject(0).getString("code")
                                if (code == "rule:product-denied") {
                                    ProductDeniedException()
                                } else {
                                    ApiException(it.code(), it.message())
                                }
                            } ?: ApiException(it.code(), it.message())
                        } catch (e: Exception) {
                            e
                        }
                    } else {
                        ApiException(it.code(), it.message())
                    }

                    completion(Failure(exception))
                }
            }

            onFailure = {
                completion(Failure(it ?: Exception("Unknown exception")))
            }
        }
    }

    /**
     * Process a post pay payment and notify user (payment receipt) if transaction is finished successfully.
     *
     * Should only be used after approaching, otherwise it returns 403 Forbidden.
     *
     * @param gasStationId Gas station ID.
     * @param pumpId Pump ID.
     * @param paymentToken The payment token obtained from the [authorize payment][authorizePayment] call.
     * @param transactionId Pass a unique UUID here or generate one with [UUID.randomUUID]. This UUID is also returned as transaction ID in the response.
     * @param completion Returns a [ProcessPaymentResponse] on success or a [Throwable] on failure.
     *
     * See also: [API docs](https://developer.pace.cloud/api/payment?version=2021-2#operation/ProcessPayment)
     */
    override fun processPostPayPayment(gasStationId: String, pumpId: String, paymentToken: String, transactionId: String, completion: (Completion<ProcessPaymentResponse>) -> Unit) {
        val transactionRequest = TransactionRequest().apply {
            this.id = transactionId
            this.type = "transaction"
            this.pumpId = pumpId
            this.paymentToken = paymentToken
        }

        API.fueling.processPayment(gasStationId, transactionRequest).handleCallback(completion)
    }

    /**
     * Process a pre auth payment and notify user (payment receipt) if transaction is finished successfully.
     *
     * Should only be used after approaching, otherwise it returns 403 Forbidden.
     *
     * @param gasStationId Gas station ID.
     * @param pumpId Pump ID.
     * @param paymentToken The payment token obtained from the [authorize payment][authorizePayment] call.
     * @param transactionId Pass a unique UUID here or generate one with [UUID.randomUUID]. This UUID is also returned as transaction ID in the response.
     * @param completion Returns a [ResponseBody] on success or a [Throwable] on failure.
     *
     * See also: [API docs](https://developer.pace.cloud/api/payment?version=2021-2#operation/ProcessPayment)
     */
    override fun processPreAuthPayment(gasStationId: String, pumpId: String, paymentToken: String, transactionId: String, completion: (Completion<ResponseBody>) -> Unit) {
        val transactionRequest = TransactionRequest().apply {
            this.id = transactionId
            this.type = "transaction"
            this.pumpId = pumpId
            this.paymentToken = paymentToken
        }

        API.fueling.processPreAuthPayment(gasStationId, transactionRequest).handleCallback(completion)
    }

    /**
     * Checks if there is an active PACE PIN or password set and verified for the currently authenticated user.
     *
     * @param completion Returns a [PinOrPassword] on success or a [Throwable] on failure.
     *
     * See also: [API docs](https://developer.pace.cloud/api/user?version=2021-2#operation/CheckUserPinOrPassword)
     */
    override fun isPinOrPasswordSet(completion: (Completion<PinOrPassword>) -> Unit) {
        IDKit.isPINOrPasswordSet(completion)
    }

    /**
     * Verifies that the passed PACE PIN is valid for the currently authenticated user and generates a one time password (OTP).
     *
     * @param completion Returns a [CreateOTP] on success or a [Throwable] on failure.
     *
     * See also: [API docs](https://developer.pace.cloud/api/user?version=2021-2#operation/CreateOTP)
     */
    override fun createOTPWithPin(pin: String, completion: (Completion<CreateOTP>) -> Unit) {
        API.totp.createOTP(CreateOTP().apply { this.pin = pin }).handleCallback(completion)
    }

    /**
     * Verifies that the passed password is valid for the currently authenticated user and generates a one time password (OTP).
     *
     * @param completion Returns a [CreateOTP] on success or a [Throwable] on failure.
     *
     * See also: [API docs](https://developer.pace.cloud/api/user?version=2021-2#operation/CreateOTP)
     */
    override fun createOTPWithPassword(password: String, completion: (Completion<CreateOTP>) -> Unit) {
        API.totp.createOTP(CreateOTP().apply { this.password = password }).handleCallback(completion)
    }

    /**
     * Generates a one time password (OTP) and sends it to the user via mail.
     *
     * @param completion Returns `true` if the mail could be sent, `false` if the mail could not be sent or a [Throwable] on failure.
     *
     * See also: [API docs](https://developer.pace.cloud/api/user?version=2021-2#operation/SendmailOTP)
     */
    override fun sendMail(completion: (Completion<Boolean>) -> Unit) {
        IDKit.sendMailOTP(completion)
    }

    /**
     * Returns time-based one time password (TOTP) information to generate an OTP on the device.
     * The generated OTP must be passed to the [authorize payment][authorizePayment] call if the payment method requires two factor authentication.
     *
     * @param completion Returns a [DeviceTOTP] on success or a [Throwable] on failure.
     *
     * See also: [API docs](https://developer.pace.cloud/api/user?version=2021-2#operation/CreateTOTP)
     */
    override fun createTOTP(mailOtp: String, completion: (Completion<DeviceTOTP>) -> Unit) {
        val deviceTotpBody = DeviceTOTPBody().apply {
            id = UUID.randomUUID().toString()
            type = DeviceTOTPBody.Type.DEVICETOTP
            attributes = DeviceTOTPBody.Attributes().apply { otp = mailOtp }
        }
        API.totp.createTOTP(CreateTOTPAPI.Body().apply { data = deviceTotpBody }).handleCallback(completion)
    }

    /**
     * Returns the receipt that has also been sent via email (when processing the payment) as PNG file.
     *
     * @param context Activity context to save the receipt PNG file.
     * @param transactionId Transaction ID.
     * @param completion Returns a [File] on success or a [Throwable] on failure.
     *
     * See also: [API docs](https://developer.pace.cloud/api/payment?version=2021-2#operation/GetReceipt)
     */
    override fun getReceipt(context: Context, transactionId: String, completion: (Completion<File>) -> Unit) {
        API.paymentTransactions.getReceipt(transactionId, Locale.getDefault().language).enqueue {
            onResponse = {
                val byteStream = it.body()?.byteStream()
                if (it.isSuccessful && byteStream != null) {
                    // Write the byte stream to a PNG file
                    val cachePath = File(context.cacheDir, "images")
                    cachePath.mkdirs()
                    val receipt = File(cachePath, "receipt.png")

                    try {
                        receipt.outputStream().use { output ->
                            byteStream.copyTo(output)
                        }
                        completion(Success(receipt))
                    } catch (e: Exception) {
                        completion(Failure(e))
                    }
                } else {
                    completion(Failure(ApiException(it.code(), it.message(), it.requestId)))
                }
            }

            onFailure = {
                completion(Failure(it ?: Exception("Unknown exception")))
            }
        }
    }
}
