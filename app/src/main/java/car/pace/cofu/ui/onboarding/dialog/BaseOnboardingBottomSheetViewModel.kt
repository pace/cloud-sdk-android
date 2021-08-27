package car.pace.cofu.ui.onboarding.dialog

import android.text.InputType
import android.util.Log
import androidx.databinding.ObservableField
import androidx.databinding.ObservableInt
import car.pace.cofu.R
import car.pace.cofu.core.events.FragmentEvent
import car.pace.cofu.core.mvvm.BaseViewModel
import car.pace.cofu.core.util.decrease
import car.pace.cofu.core.util.increase
import cloud.pace.sdk.idkit.IDKit
import cloud.pace.sdk.idkit.model.InternalError
import cloud.pace.sdk.poikit.utils.ApiException
import cloud.pace.sdk.utils.Failure
import cloud.pace.sdk.utils.Success
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * Base class for onboarding bottom sheet view models.
 */
abstract class BaseOnboardingBottomSheetViewModel : BaseViewModel() {
    val inputType =
        ObservableInt(InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD)
    val loading = ObservableInt(0)

    val title = ObservableInt()
    val description = ObservableInt()
    val errorText = ObservableInt()
    val input = ObservableField<String>()

    /**
     * helper function that can be called when an api error occurs.
     * It sets the error text depending on the [throwable]
     */
    internal open fun handleApiFailure(throwable: Throwable) {
        Log.w("ConfigureBiometricAuth", throwable)
        when (throwable) {
            is InternalError -> errorText.set(R.string.onboarding_error_authorisation)
            is UnknownHostException, is SocketTimeoutException -> errorText.set(R.string.onboarding_network_error)
            is ApiException -> errorText.set(if(throwable.errorCode == 403) R.string.onboarding_error_authorisation else R.string.onboarding_unknown_error)
            else -> errorText.set(R.string.onboarding_unknown_error)
        }

    }


    /**
     * updates the UI elements to ask for a previously sent one time password
     */
    internal open fun askForOTP() {
        inputType.set(InputType.TYPE_CLASS_NUMBER)
        title.set(R.string.onboarding_enter_otp)
        description.set(R.string.onboarding_enter_otp_description)
    }

    /**
     * calls the API to request a one time password to be sent to the user's email,
     * then calls [askForOTP]
     */
    internal fun sendOTP() {
        loading.increase()
        IDKit.sendMailOTP {
            loading.decrease()
            when (it) {
                is Success -> when (it.result) {
                    true -> askForOTP()
                    // when sending the otp fails, we cannot proceed. Close the bottom sheet and let the user retry
                    false -> handleEvent(SendingOTPFailedEvent())
                }
                is Failure -> handleEvent(SendingOTPFailedEvent())
            }
        }
    }

    /**
     * sets the result to successful (indicating the OnboardingFragment the next step can be shown)
     * and dismisses the dialog
     */
    internal fun done() {
        handleEvent(AuthorisationSetEvent())
    }

    abstract fun onButtonClick()

    class SendingOTPFailedEvent: FragmentEvent()
    class AuthorisationSetEvent : FragmentEvent()
}