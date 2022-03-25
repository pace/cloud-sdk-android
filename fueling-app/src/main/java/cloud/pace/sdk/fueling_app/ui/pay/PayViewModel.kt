package cloud.pace.sdk.fueling_app.ui.pay

import androidx.lifecycle.*
import cloud.pace.sdk.api.fueling.generated.model.PumpResponse
import cloud.pace.sdk.appkit.app.webview.AppWebViewModel
import cloud.pace.sdk.appkit.persistence.SharedPreferencesModel
import cloud.pace.sdk.appkit.utils.EncryptionUtils
import cloud.pace.sdk.fueling_app.R
import cloud.pace.sdk.fueling_app.data.repository.Repository
import cloud.pace.sdk.fueling_app.util.Constants
import cloud.pace.sdk.fueling_app.util.Result
import cloud.pace.sdk.fueling_app.util.WrongInputException
import cloud.pace.sdk.idkit.IDKit
import cloud.pace.sdk.poikit.utils.ApiException
import cloud.pace.sdk.utils.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.net.HttpURLConnection
import java.util.*
import javax.inject.Inject

@HiltViewModel
class PayViewModel @Inject constructor(
    private val repository: Repository,
    private val sharedPreferencesModel: SharedPreferencesModel,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val args = PayFragmentArgs.fromSavedStateHandle(savedStateHandle)
    private val paymentMethod = args.paymentMethod
    private val priceIncludingVAT = args.pumpResponse.priceIncludingVAT
    private val gasStationId = args.gasStation.id
    private val pumpId = args.pumpResponse.id
    private val otp by lazy { MutableLiveData<Result<String?>>() }

    val paymentResult = otp.switchMap {
        liveData {
            when (it) {
                is Result.Loading -> emit(Result.Loading)
                is Result.Success -> {
                    priceIncludingVAT ?: return@liveData emit(Result.Error(IllegalArgumentException("Price including VAT cannot be null")))
                    emit(Result.Loading)

                    val purposePRNs = mutableListOf("prn:poi:gas-stations:$gasStationId")
                    if (paymentMethod.kind == Constants.PAYPAL) {
                        // This UUID does not work for the PRODUCTION environment. Please contact PACE if you want to offer payments with PayPal.
                        purposePRNs.add("prn:paypal:risk-correlation-ids:sBu9Sbz5AmB3AH3eJRFGaujW")
                    }

                    try {
                        // First authorize the payment to get a payment token
                        val paymentToken = repository.authorizePayment(paymentMethod.id, priceIncludingVAT, purposePRNs, it.data).value
                            ?: return@liveData emit(Result.Error(IllegalArgumentException("Payment token cannot be null")))
                        val transactionId = UUID.randomUUID().toString()

                        // And then process the payment with the payment token
                        if (args.pumpResponse.fuelingProcess == PumpResponse.FuelingProcess.POSTPAY) {
                            repository.processPostPayPayment(gasStationId, pumpId, paymentToken, transactionId)
                        } else {
                            repository.processPreAuthPayment(gasStationId, pumpId, paymentToken, transactionId)
                        }
                        emit(Result.Success(args.pumpResponse.fuelingProcess to transactionId))
                    } catch (e: Exception) {
                        // We recommend to show the user a more detailed error screen (e.g. if the payment method was rejected by the payment provider) than just the general error screen.
                        // You can see a list of all errors in the API documentation:
                        // For the authorize payment call: https://developer.pace.cloud/api/payment?version=2021-2#operation/AuthorizePaymentToken
                        // For the process payment call: https://developer.pace.cloud/api/payment?version=2021-2#operation/ProcessPayment
                        emit(Result.Error(e))
                    }
                }
                is Result.Error -> emit(Result.Error(it.exception))
            }
        }
    }
    val biometricRequest by lazy { MutableLiveData<Event<AppWebViewModel.BiometricRequest>>() }
    val showDialog by lazy { MutableLiveData<Event<Result<DialogType>>>() }

    fun processPayment() {
        if (paymentMethod.twoFactor) {
            // Try the following order of 2nd factors:
            // 1. Biometry; 2. PACE PIN; 3. PACE password; 4. Mail OTP
            if (IDKit.isBiometricAuthenticationEnabled()) {
                authorizeWithBiometry()
            } else {
                checkPinOrPassword()
            }
        } else {
            otp.value = Result.Success(null)
        }
    }

    fun setDialogInput(input: String?, dialogType: DialogType) {
        if (input != null) {
            otp.value = Result.Loading

            when (dialogType) {
                DialogType.PIN -> authorizeWithPin(input)
                DialogType.PASSWORD -> authorizeWithPassword(input)
                DialogType.MAIL -> authorizeWithMail(input)
            }
        } else {
            otp.value = Result.Error(WrongInputException())
        }
    }

    private fun checkPinOrPassword() {
        viewModelScope.launch {
            showDialog.value = Event(Result.Loading)
            showDialog.value = try {
                val isPinOrPasswordSet = repository.isPinOrPasswordSet()
                when {
                    isPinOrPasswordSet.pin == true -> Event(Result.Success(DialogType.PIN))
                    isPinOrPasswordSet.password == true -> Event(Result.Success(DialogType.PASSWORD))
                    else -> {
                        val success = repository.sendMail()
                        if (success) {
                            Event(Result.Success(DialogType.MAIL))
                        } else {
                            Event(Result.Error(IllegalStateException("Could not send mail OTP.")))
                        }
                    }
                }
            } catch (e: Exception) {
                Event(Result.Success(DialogType.MAIL))
            }
        }
    }

    private fun authorizeWithBiometry() {
        val totpSecret = sharedPreferencesModel.getTotpSecret()
        if (totpSecret != null) {
            biometricRequest.value = Event(AppWebViewModel.BiometricRequest(
                R.string.payment_biometric_prompt_title,
                onSuccess = {
                    try {
                        val decryptedSecret = EncryptionUtils.decrypt(totpSecret.encryptedSecret)
                        val otp = EncryptionUtils.generateOTP(decryptedSecret, totpSecret.digits, totpSecret.period, totpSecret.algorithm)
                        this.otp.value = Result.Success(otp)
                    } catch (e: Exception) {
                        checkPinOrPassword()
                    }
                },
                onFailure = { _, _ ->
                    checkPinOrPassword()
                }
            ))
        }
    }

    private fun authorizeWithPin(pin: String) {
        viewModelScope.launch {
            otp.value = try {
                val otp = repository.createOTPWithPin(pin).otp
                Result.Success(otp)
            } catch (e: Exception) {
                if ((e as? ApiException)?.errorCode == HttpURLConnection.HTTP_NOT_FOUND) {
                    Result.Error(WrongInputException())
                } else {
                    Result.Error(e)
                }
            }
        }
    }

    private fun authorizeWithPassword(password: String) {
        viewModelScope.launch {
            otp.value = try {
                val otp = repository.createOTPWithPassword(password).otp
                Result.Success(otp)
            } catch (e: Exception) {
                if ((e as? ApiException)?.errorCode == HttpURLConnection.HTTP_NOT_FOUND) {
                    Result.Error(WrongInputException())
                } else {
                    Result.Error(e)
                }
            }
        }
    }

    private fun authorizeWithMail(mailOtp: String) {
        viewModelScope.launch {
            otp.value = try {
                val deviceTotp = repository.createTOTP(mailOtp)
                val decryptedSecret = deviceTotp.secret
                val digits = deviceTotp.digits
                val period = deviceTotp.period
                val algorithm = deviceTotp.algorithm
                if (decryptedSecret != null && digits != null && period != null && algorithm != null) {
                    val otp = EncryptionUtils.generateOTP(decryptedSecret, digits, period, algorithm.value)
                    Result.Success(otp)
                } else {
                    Result.Error(IllegalArgumentException("Device TOTP data cannot be null"))
                }
            } catch (e: Exception) {
                if ((e as? ApiException)?.errorCode == HttpURLConnection.HTTP_FORBIDDEN) {
                    Result.Error(WrongInputException())
                } else {
                    Result.Error(e)
                }
            }
        }
    }

    enum class DialogType {
        PIN,
        PASSWORD,
        MAIL
    }
}
