package car.pace.cofu.ui.onboarding

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricPrompt
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import car.pace.cofu.R
import car.pace.cofu.core.events.FragmentEvent
import car.pace.cofu.core.events.FragmentResultable
import car.pace.cofu.core.events.NavigateToDirection
import car.pace.cofu.core.events.ShowSnack
import car.pace.cofu.core.mvvm.BaseFragment
import car.pace.cofu.core.navigation.navigate
import car.pace.cofu.core.util.decrease
import car.pace.cofu.core.util.increase
import car.pace.cofu.core.util.isOnline
import car.pace.cofu.databinding.FragmentOnboardingBinding
import car.pace.cofu.ui.main.MainActivity
import cloud.pace.sdk.appkit.AppKit
import cloud.pace.sdk.idkit.IDKit
import cloud.pace.sdk.idkit.model.InvalidSession
import cloud.pace.sdk.utils.Failure
import cloud.pace.sdk.utils.Success
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import kotlin.math.min


class CheckLocationPermissionEvent : FragmentEvent()
class LocationPermissionCheckResult(val isGranted: Boolean) : FragmentEvent()

class RequestLocationPermissionEvent : FragmentEvent()
class LocationPermissionRequestResult(val isGranted: Boolean) : FragmentEvent()

class StartPaceIdRegistration : FragmentEvent()
class PaceIdRegistrationSuccessful : FragmentEvent()
class PaceIdRegistrationFailed(error: Throwable) : FragmentEvent()

class CheckAuthorisationMethodsEvent : FragmentEvent()
class AuthorisationMethodsCheckResultEvent(val biometryEnabled: Boolean, val pinSet: Boolean) :
    FragmentEvent()

class AuthorisationEvent(val method: AuthorisationMethod) : FragmentEvent()
class AuthorisationSetEvent : FragmentEvent()

class SelectPaymentMethodEvent : FragmentEvent()
class PaymentMethodSelectedEvent : FragmentEvent()

class NavigateToHomeEvent : FragmentEvent()

// Workaround for https://github.com/google/dagger/issues/1904
abstract class BaseOnboardingFragment :
    BaseFragment<FragmentOnboardingBinding, OnboardingViewModel>(
        R.layout.fragment_onboarding,
        OnboardingViewModel::class
    )

@AndroidEntryPoint
class OnboardingFragment : BaseOnboardingFragment(), FragmentResultable {

    private var setPaymentSelectedOnResume = false

    private val args: OnboardingFragmentArgs by navArgs()

    private val activityResultLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            viewModel.onResponse(LocationPermissionRequestResult(it))
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        (activity as? MainActivity)?.menuSwipingActive = false

        val ctx = requireContext()
        val fingerprintStatus = BiometricManager.from(ctx).canAuthenticate(BIOMETRIC_WEAK)

        viewModel.hasFingerprint = arrayOf(
            BiometricManager.BIOMETRIC_SUCCESS,
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE,
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED
        ).contains(fingerprintStatus)

        val dm = resources.displayMetrics
        val screenWidth = dm.widthPixels / dm.density
        val screenHeight = dm.heightPixels / dm.density
        val smallestWidth = min(screenWidth, screenHeight)
        viewModel.isSmallDevice = smallestWidth < 360

        if (args.logoutComplete) {
            // if you want an informing snack bar, uncomment
            // handleEvent(ShowSnack(messageRes = R.string.logout_complete_snackbar))
        }
    }

    override fun onHandleFragmentEvent(event: FragmentEvent) {
        when (event) {
            is CheckLocationPermissionEvent -> checkLocationPermission()
            is RequestLocationPermissionEvent -> requestLocationPermission()
            is StartPaceIdRegistration -> startPaceIdRegistration()
            is CheckAuthorisationMethodsEvent -> checkAuthorisationMethods()
            is AuthorisationEvent -> setAuthorisation(event.method)
            is SelectPaymentMethodEvent -> selectPaymentMethod()
            is NavigateToHomeEvent -> navigate(
                NavigateToDirection(
                    OnboardingFragmentDirections.onboardingToHome(),
                    clearBackStack = true
                )
            )
        }
    }

    override fun onResume() {
        super.onResume()
        if (setPaymentSelectedOnResume) {
            viewModel.onResponse(PaymentMethodSelectedEvent())
            setPaymentSelectedOnResume = false
        }
    }

    override fun onConsumeBackPress(): Boolean {
        if (viewModel.selectedPage.get() > 0) {
            viewModel.selectedPage.decrease()
            return true
        }
        return false
    }

    private fun selectPaymentMethod() {
        if (requireContext().isOnline) {
            AppKit.openPaymentApp(requireContext())
            setPaymentSelectedOnResume = true
        } else {
            handleEvent(
                ShowSnack(
                    messageRes = R.string.onboarding_network_error,
                    actionText = getString(R.string.onboarding_retry),
                    actionListener = ::selectPaymentMethod
                )
            )
        }
    }

    private fun setAuthorisation(method: AuthorisationMethod) {
        when (method) {
            AuthorisationMethod.FINGERPRINT -> setupFingerprintAuthorisation()
            AuthorisationMethod.EXISTING_PIN -> setupPinAuthorisation(pinAlreadySet = true)
            AuthorisationMethod.NEW_PIN -> setupPinAuthorisation(pinAlreadySet = false)
        }
    }

    private fun setupFingerprintAuthorisation() {
        viewModel.loading.increase()
        BiometricPrompt(this, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                viewModel.loading.decrease()
                setupFingerprintAuthorisationOnServer()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                viewModel.loading.decrease()
                if (errorCode == BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED) {
                    showNoFingerprintsSavedDialog()
                } else {
                    val text = "${getString(R.string.onboarding_fingerprint_error)} $errString"
                    handleEvent(ShowSnack(text))
                }
            }

        }).authenticate(
            BiometricPrompt.PromptInfo.Builder()
                .setTitle(getString(R.string.onboarding_authorisation_request_fingerprint))
                .setNegativeButtonText(getString(R.string.onboarding_back))
                .build()
        )
    }

    private fun setupFingerprintAuthorisationOnServer() {
        if (IDKit.isBiometricAuthenticationEnabled()) {
            viewModel.onResponse(AuthorisationSetEvent())
            return
        }

        viewModel.loading.increase()
        IDKit.enableBiometricAuthentication {
            viewModel.loading.decrease()
            when (it) {
                is Success -> {
                    when (it.result) {
                        // setting biometric authentication only works without password for 5 minutes after login
                        true -> viewModel.onResponse(AuthorisationSetEvent())
                        false -> navigate(NavigateToDirection(OnboardingFragmentDirections.onboardingConfigureBiometry()))
                    }
                }
                is Failure -> handleApiFailure(
                    it.throwable,
                    ::setupFingerprintAuthorisationOnServer
                )
            }

        }
    }

    private fun checkAuthorisationMethods() {
        val biometricAuthEnabled = IDKit.isBiometricAuthenticationEnabled()

        viewModel.loading.increase()
        IDKit.isPINSet {
            viewModel.loading.decrease()
            when (it) {
                is Success -> {
                    viewModel.onResponse(
                        AuthorisationMethodsCheckResultEvent(
                            biometryEnabled = biometricAuthEnabled,
                            pinSet = it.result
                        )
                    )
                }
                is Failure -> handleApiFailure(it.throwable, ::checkAuthorisationMethods)
            }
        }
    }

    private fun setupPinAuthorisation(pinAlreadySet: Boolean) {
        if (pinAlreadySet) {
            viewModel.onResponse(AuthorisationSetEvent())
        } else {
            navigate(NavigateToDirection(OnboardingFragmentDirections.onboardingConfigurePin()))
        }
    }

    private fun handleApiFailure(throwable: Throwable, retryAction: () -> Unit) {
        Log.w("OnboardingFragment", throwable)
        val event = when (throwable) {
            is InvalidSession -> ShowSnack(
                messageRes = R.string.onboarding_invalid_session,
                actionText = getString(R.string.onboarding_retry_login),
                actionListener = ::startPaceIdRegistration
            )
            is UnknownHostException, is SocketTimeoutException -> ShowSnack(
                messageRes = R.string.onboarding_network_error,
                actionText = getString(R.string.onboarding_retry),
                actionListener = retryAction
            )
            else -> ShowSnack(getString(R.string.onboarding_unknown_error))
        }
        handleEvent(event)
    }

    private fun showNoFingerprintsSavedDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.onboarding_fingerprint_none_saved_title)
            .setNegativeButton(R.string.onboarding_back) { dialog, _ -> dialog.dismiss() }
            .setPositiveButton(R.string.onboarding_fingerprint_save) { _, _ ->
                val intent = when {
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> Intent(Settings.ACTION_BIOMETRIC_ENROLL)
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.P -> Intent(Settings.ACTION_FINGERPRINT_ENROLL)
                    else -> Intent(Settings.ACTION_SECURITY_SETTINGS)
                }
                startActivity(intent)
            }
            .create()
            .show()
    }

    private fun startPaceIdRegistration() {
        if (IDKit.isAuthorizationValid()) {
            viewModel.onResponse(PaceIdRegistrationSuccessful())
        } else if (!requireContext().isOnline) {
            handleEvent(
                ShowSnack(
                    messageRes = R.string.onboarding_network_error,
                    actionText = getString(R.string.onboarding_retry),
                    actionListener = ::startPaceIdRegistration
                )
            )
        } else {
            lifecycleScope.launch(Dispatchers.Main) {
                IDKit.authorize(this@OnboardingFragment) {
                    when (it) {
                        is Success -> viewModel.onResponse(PaceIdRegistrationSuccessful())
                        is Failure -> viewModel.onResponse(PaceIdRegistrationFailed(it.throwable))
                    }
                }
            }
        }
    }

    private fun checkLocationPermission() {
        val isGranted =
            requireActivity().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        viewModel.onResponse(LocationPermissionCheckResult(isGranted))
    }

    private fun requestLocationPermission() {
        if (requireActivity().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            activityResultLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            // permission already granted
            viewModel.onResponse(LocationPermissionRequestResult(true))
        }
    }

    override fun getResultRequestKey(): String = javaClass.simpleName

    override fun onFragmentResult(resultBundle: Bundle) {
        if (resultBundle.getBoolean(KEY_SUCCESSFUL)) {
            viewModel.onResponse(AuthorisationSetEvent())
        }
    }

    companion object {
        const val KEY_SUCCESSFUL = "success"
    }
}


