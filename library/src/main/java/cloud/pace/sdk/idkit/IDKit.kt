package cloud.pace.sdk.idkit

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import cloud.pace.sdk.PACECloudSDK
import cloud.pace.sdk.api.API
import cloud.pace.sdk.api.pay.PayAPI.paymentMethods
import cloud.pace.sdk.api.pay.PayAPI.paymentTransactions
import cloud.pace.sdk.api.pay.generated.model.PaymentMethods
import cloud.pace.sdk.api.pay.generated.model.Transactions
import cloud.pace.sdk.api.pay.generated.request.paymentMethods.GetPaymentMethodsIncludingCreditCheckAPI
import cloud.pace.sdk.api.pay.generated.request.paymentMethods.GetPaymentMethodsIncludingCreditCheckAPI.getPaymentMethodsIncludingCreditCheck
import cloud.pace.sdk.api.pay.generated.request.paymentTransactions.ListTransactionsAPI
import cloud.pace.sdk.api.pay.generated.request.paymentTransactions.ListTransactionsAPI.listTransactions
import cloud.pace.sdk.api.user.generated.model.PinOrPassword
import cloud.pace.sdk.idkit.authorization.AuthorizationManager
import cloud.pace.sdk.idkit.credentials.CredentialsManager
import cloud.pace.sdk.idkit.model.OIDConfiguration
import cloud.pace.sdk.idkit.model.ServiceConfiguration
import cloud.pace.sdk.idkit.userinfo.UserInfoResponse
import cloud.pace.sdk.utils.CloudSDKKoinComponent
import cloud.pace.sdk.utils.Completion
import cloud.pace.sdk.utils.SetupLogger
import cloud.pace.sdk.utils.handleCallback
import org.koin.core.component.inject

object IDKit : CloudSDKKoinComponent {

    internal var isInitialized = false
    private val authorizationManager: AuthorizationManager by inject()
    private val credentialsManager: CredentialsManager by inject()

    /**
     * Checks whether `PACECloudSDK` has been set up correctly before `IDKit` is used, otherwise log SDK warnings.
     */
    init {
        SetupLogger.logSDKWarningIfNeeded()
    }

    /**
     * Sets up [IDKit] with the passed [configuration].
     *
     * @param context The context.
     * @param additionalCaching If set to `false` persistent session cookies will be shared only natively.
     * If set to `true` the session will additionally be persisted by [IDKit] to improve the change of not having to resign in again. Defaults to `true`.
     */
    internal fun setup(context: Context, configuration: OIDConfiguration, additionalCaching: Boolean = true) {
        authorizationManager.setup(configuration, additionalCaching)
        authorizationManager.setAdditionalParameters(PACECloudSDK.additionalQueryParams)
        isInitialized = true

        val applicationInfo = context.packageManager?.getApplicationInfo(context.packageName, PackageManager.GET_META_DATA)
        val appAuthRedirectScheme = applicationInfo?.metaData?.get("appAuthRedirectScheme")?.toString()

        SetupLogger.appAuthRedirectScheme = appAuthRedirectScheme
        SetupLogger.oidConfiguration = configuration
        SetupLogger.preCheckIDKitSetup()
    }

    /**
     * Replaces the additional parameters property of the [OIDConfiguration] with [this][params] values.
     */
    fun setAdditionalParameters(params: Map<String, String>?) {
        authorizationManager.setAdditionalParameters(params)
    }

    /**
     * Performs a discovery to retrieve a [ServiceConfiguration].
     *
     * @param issuerUri The issuer URI.
     * @param completion The block to be called when the discovery is complete either including the [ServiceConfiguration] or a [Throwable].
     */
    fun discoverConfiguration(issuerUri: String, completion: (Completion<ServiceConfiguration>) -> Unit) {
        authorizationManager.discoverConfiguration(issuerUri, completion)
    }

    /**
     * Sends an authorization request to the authorization service, using a [Chrome Custom Tab](https://developer.chrome.com/multidevice/android/customtabs)
     * and handles the authorization response automatically.
     *
     * @param activity The Activity to launch the authorization request [Intent].
     * @param completion The block to be called when the request is completed including either a valid `accessToken` or [Throwable].
     */
    suspend fun authorize(activity: AppCompatActivity, completion: (Completion<String?>) -> Unit = {}) {
        authorizationManager.authorize(activity, completion)
    }

    /**
     * Sends an authorization request to the authorization service, using a [Chrome Custom Tab](https://developer.chrome.com/multidevice/android/customtabs)
     * and handles the authorization response automatically.
     *
     * @param fragment The Fragment to launch the authorization request [Intent].
     * @param completion The block to be called when the request is completed including either a valid `accessToken` or [Throwable].
     */
    suspend fun authorize(fragment: Fragment, completion: (Completion<String?>) -> Unit = {}) {
        authorizationManager.authorize(fragment, completion)
    }

    /**
     * Sends an authorization request to the authorization service, using a [Chrome Custom Tab](https://developer.chrome.com/multidevice/android/customtabs).
     * Upon completion of this authorization request, a [PendingIntent] of the [completedActivity] will be invoked.
     * If the user cancels the authorization request, a [PendingIntent] of the [canceledActivity] will be invoked.
     *
     * Note: Call [handleAuthorizationResponse] in [completedActivity] or [canceledActivity] when the intent is returned from Chrome Custom Tab.
     */
    fun authorize(completedActivity: Class<*>, canceledActivity: Class<*>) {
        authorizationManager.authorize(completedActivity, canceledActivity)
    }

    /**
     * Creates an authorization request [Intent] that can be launched using the Activity Result API and a StartActivityForResult contract or with [android.app.Activity.startActivityForResult]
     * to open a [Chrome Custom Tab](https://developer.chrome.com/multidevice/android/customtabs).
     *
     * Note: Call [handleAuthorizationResponse] in ActivityResultCallback if Activity Result API was used or in [android.app.Activity.onActivityResult] if
     * [android.app.Activity.startActivityForResult] was used when the authorization result is returned from Chrome Custom Tab.
     */
    fun authorize(): Intent {
        return authorizationManager.authorize()
    }

    /**
     * Sends a request to the authorization service to exchange a code granted as part of an authorization request for a token.
     *
     * @param intent Represents the [Intent] from the [Chrome Custom Tab](https://developer.chrome.com/multidevice/android/customtabs).
     * @param completion The block to be called when the request is completed including either a valid `accessToken` or [Throwable].
     */
    fun handleAuthorizationResponse(intent: Intent, completion: (Completion<String?>) -> Unit) {
        authorizationManager.handleAuthorizationResponse(intent, completion)
    }

    /**
     * Refreshes the current access token if needed.
     *
     * @param force Forces a refresh even if the current `accessToken` is still valid. Defaults to `false`.
     * @param completion The block to be called when the request is completed including either a new valid `accessToken` or a [Throwable].
     */
    @JvmOverloads
    fun refreshToken(force: Boolean = false, completion: (Completion<String?>) -> Unit = {}) {
        authorizationManager.refreshToken(force, completion)
    }

    /**
     * Sends an end session request to the authorization service, using a [Chrome Custom Tab](https://developer.chrome.com/multidevice/android/customtabs)
     * and handles the end session response automatically.
     *
     * @param activity The Activity to launch the end session request [Intent].
     * @param completion The block to be called when the session has been reset or a [Throwable] when an exception occurred.
     */
    suspend fun endSession(activity: AppCompatActivity, completion: (Completion<Unit>) -> Unit = {}) {
        authorizationManager.endSession(activity, completion)
    }

    /**
     * Sends an end session request to the authorization service, using a [Chrome Custom Tab](https://developer.chrome.com/multidevice/android/customtabs)
     * and handles the end session response automatically.
     *
     * @param fragment The Fragment to launch the end session request [Intent].
     * @param completion The block to be called when the session has been reset or a [Throwable] when an exception occurred.
     */
    suspend fun endSession(fragment: Fragment, completion: (Completion<Unit>) -> Unit = {}) {
        authorizationManager.endSession(fragment, completion)
    }

    /**
     * Sends an end session request to the authorization service, using a [Chrome Custom Tab](https://developer.chrome.com/multidevice/android/customtabs).
     * Upon completion of this end session request, a [PendingIntent] of the [completedActivity] will be invoked.
     * If the user cancels the end session request, a [PendingIntent] of the [canceledActivity] will be invoked.
     *
     * Note: Call [handleEndSessionResponse] in [completedActivity] or [canceledActivity] when the intent is returned from Chrome Custom Tab.
     *
     * @return True if an end session request could be performed, false otherwise (due to an invalid session).
     */
    fun endSession(completedActivity: Class<*>, canceledActivity: Class<*>): Boolean {
        return authorizationManager.endSession(completedActivity, canceledActivity)
    }

    /**
     * Creates an end session request [Intent] that can be launched using the Activity Result API and a StartActivityForResult contract or with [android.app.Activity.startActivityForResult]
     * to open a [Chrome Custom Tab](https://developer.chrome.com/multidevice/android/customtabs).
     *
     * Note: Call [handleEndSessionResponse] in ActivityResultCallback if Activity Result API was used or in [android.app.Activity.onActivityResult] if
     * [android.app.Activity.startActivityForResult] was used when the end session result is returned from Chrome Custom Tab.
     *
     * @return The end session request [Intent] or null, if none could be created due to an invalid session.
     */
    fun endSession(): Intent? {
        return authorizationManager.endSession()
    }

    /**
     * Resets the local session object.
     *
     * @param intent Represents the [Intent] from the [Chrome Custom Tab](https://developer.chrome.com/multidevice/android/customtabs).
     * @param completion The block to be called when the session has been reset or a [Throwable] when an exception occurred.
     */
    fun handleEndSessionResponse(intent: Intent, completion: (Completion<Unit>) -> Unit) {
        authorizationManager.handleEndSessionResponse(intent, completion)
    }

    /**
     * Checks the current authorization state. Returning `true` does not mean that the access is fresh - just that it was valid the last time it was used.
     *
     * @return The current state of the authorization.
     */
    fun isAuthorizationValid(): Boolean {
        return authorizationManager.isAuthorizationValid()
    }

    /**
     * Checks if the [intent] contains an authorization response.
     *
     * @return True if the [intent] has an authorization response extra, false otherwise.
     */
    fun containsAuthorizationResponse(intent: Intent): Boolean {
        return authorizationManager.containsAuthorizationResponse(intent)
    }

    /**
     * Checks if the [intent] contains an end session response.
     *
     * @return True if the [intent] has an end session response extra, false otherwise.
     */
    fun containsEndSessionResponse(intent: Intent): Boolean {
        return authorizationManager.containsEndSessionResponse(intent)
    }

    /**
     * Checks if the [intent] contains an authorization/end session exception.
     *
     * @return True if the [intent] has an exception extra, false otherwise.
     */
    fun containsException(intent: Intent): Boolean {
        return authorizationManager.containsException(intent)
    }

    /**
     * Returns the cached accessToken of the current session or null if the session is not initialized or has no accessToken.
     */
    fun cachedToken(): String? {
        return authorizationManager.cachedToken()
    }

    /**
     * Retrieves the currently authorized user's information.
     *
     * @param accessToken The user's access token for which to retrieve the user info.
     * @param completion The block to be called when the request is completed including either a valid `userInfo` or a [Throwable].
     */
    fun userInfo(accessToken: String, completion: (Completion<UserInfoResponse>) -> Unit) {
        authorizationManager.userInfo(accessToken, completion)
    }

    /**
     * Checks if biometric authentication is enabled for the current user.
     *
     * @return The information if biometric authentication is enabled.
     */
    fun isBiometricAuthenticationEnabled(): Boolean {
        SetupLogger.logBiometryWarningIfNeeded()
        return credentialsManager.isBiometricAuthenticationEnabled()
    }

    /**
     * Enables biometric authentication for the current user using the PIN.
     *
     * @param pin The PIN of the current user.
     * @param completion The block to be called when the request is completed including either the information if biometric authentication has been enabled `successfully` or a [Throwable].
     */
    fun enableBiometricAuthenticationWithPIN(pin: String, completion: (Completion<Boolean>) -> Unit) {
        SetupLogger.logBiometryWarningIfNeeded()
        credentialsManager.enableBiometricAuthentication(pin = pin, completion = completion)
    }

    /**
     * Enables biometric authentication for the current user using the account password.
     *
     * @param password The password of the current user.
     * @param completion The block to be called when the request is completed including either the information if biometric authentication has been enabled `successfully` or a [Throwable].
     */
    fun enableBiometricAuthenticationWithPassword(password: String, completion: (Completion<Boolean>) -> Unit) {
        SetupLogger.logBiometryWarningIfNeeded()
        credentialsManager.enableBiometricAuthentication(password = password, completion = completion)
    }

    /**
     * Enables biometric authentication for the current user using mail OTP.
     *
     * @see sendMailOTP
     *
     * @param otp The OTP for the user.
     * @param completion The block to be called when the request is completed including either the information if biometric authentication has been enabled `successfully` or a [Throwable].
     */
    fun enableBiometricAuthenticationWithOTP(otp: String, completion: (Completion<Boolean>) -> Unit) {
        SetupLogger.logBiometryWarningIfNeeded()
        credentialsManager.enableBiometricAuthentication(otp = otp, completion = completion)
    }

    /**
     * Enables biometric authentication without passing credentials for the current user.
     *
     * This request will only succeed if called within 5 minutes after a successful authorization.
     *
     * @param completion The block to be called when the request is completed including either the information if biometric authentication has been enabled `successfully` or a [Throwable].
     */
    fun enableBiometricAuthentication(completion: (Completion<Boolean>) -> Unit) {
        SetupLogger.logBiometryWarningIfNeeded()
        credentialsManager.enableBiometricAuthentication(completion = completion)
    }

    /**
     * Disables biometric authentication for the current user.
     */
    fun disableBiometricAuthentication() {
        SetupLogger.logBiometryWarningIfNeeded()
        credentialsManager.disableBiometricAuthentication()
    }

    /**
     * Checks if there is an active PIN set for the currently authenticated user.
     *
     * @param completion The block to be called when the request is completed including either the `pinStatus` or a [Throwable].
     */
    fun isPINSet(completion: (Completion<Boolean>) -> Unit) {
        credentialsManager.isPINSet(completion)
    }

    /**
     * Checks if there is an active password set for the currently authenticated user.
     *
     * @param completion The block to be called when the request is completed including either the `passwordStatus` or a [Throwable].
     */
    fun isPasswordSet(completion: (Completion<Boolean>) -> Unit) {
        credentialsManager.isPasswordSet(completion)
    }

    /**
     * Checks if there is an active PIN or password set and verified for the currently authenticated user.
     *
     * @param completion The block to be called when the request is completed including either the [PinOrPassword] status or a [Throwable].
     */
    fun isPINOrPasswordSet(completion: (Completion<PinOrPassword>) -> Unit) {
        credentialsManager.isPINOrPasswordSet(completion)
    }

    /**
     * Sets or updates the user's PIN using biometric authentication.
     *
     * @param fragment The fragment that will host the [androidx.biometric.BiometricPrompt].
     * @param title The title of the [androidx.biometric.BiometricPrompt].
     * @param subtitle The subtitle of the [androidx.biometric.BiometricPrompt].
     * @param cancelText The negative button text of the [androidx.biometric.BiometricPrompt].
     * @param isDeviceCredentialsAllowed Sets whether the user should be given the option to authenticate with their device PIN, pattern, or password instead of biometry. Defaults to `true`.
     * Note that if this option is set to `true` [cancelText] will not be set because it will replace the negative button on the BiometricPrompt.
     * @param pin The PIN to be set.
     * @param completion The block to be called when the request is completed including either the information if the PIN has been set / updated `successfully` or a [Throwable].
     */
    @JvmOverloads
    fun setPINWithBiometry(
        fragment: Fragment,
        title: String,
        subtitle: String? = null,
        cancelText: String? = null,
        isDeviceCredentialsAllowed: Boolean = true,
        pin: String,
        completion: (Completion<Boolean>) -> Unit
    ) {
        SetupLogger.logBiometryWarningIfNeeded()
        credentialsManager.setPINWithBiometry(fragment, title, subtitle, cancelText, isDeviceCredentialsAllowed, pin, completion)
    }

    /**
     * Sets or updates the user's PIN using biometric authentication.
     *
     * @param activity The activity that will host the [androidx.biometric.BiometricPrompt].
     * @param title The title of the [androidx.biometric.BiometricPrompt].
     * @param subtitle The subtitle of the [androidx.biometric.BiometricPrompt].
     * @param cancelText The negative button text of the [androidx.biometric.BiometricPrompt].
     * @param isDeviceCredentialsAllowed Sets whether the user should be given the option to authenticate with their device PIN, pattern, or password instead of biometry. Defaults to `true`.
     * Note that if this option is set to `true` [cancelText] will not be set because it will replace the negative button on the BiometricPrompt.
     * @param pin The PIN to be set.
     * @param completion The block to be called when the request is completed including either the information if the PIN has been set / updated `successfully` or a [Throwable].
     */
    @JvmOverloads
    fun setPINWithBiometry(
        activity: FragmentActivity,
        title: String,
        subtitle: String? = null,
        cancelText: String? = null,
        isDeviceCredentialsAllowed: Boolean = true,
        pin: String,
        completion: (Completion<Boolean>) -> Unit
    ) {
        SetupLogger.logBiometryWarningIfNeeded()
        credentialsManager.setPINWithBiometry(activity, title, subtitle, cancelText, isDeviceCredentialsAllowed, pin, completion)
    }

    /**
     * Sets or updates the user's PIN using the account password.
     *
     * @param pin The PIN to be set.
     * @param password The account password that additionally needs to be provided to successfully set or update the PIN.
     * @param completion The block to be called when the request is completed including either the information if the PIN has been set / updated `successfully` or a [Throwable].
     */
    fun setPINWithPassword(pin: String, password: String, completion: (Completion<Boolean>) -> Unit) {
        credentialsManager.setPINWithPassword(pin, password, completion)
    }

    /**
     * Sets or updates the user's PIN using mail OTP.
     *
     * @see sendMailOTP
     *
     * @param pin The PIN to be set.
     * @param otp The OTP that additionally needs to be provided to successfully set or update the PIN.
     * @param completion The block to be called when the request is completed including either the information if the PIN has been set / updated `successfully` or a [Throwable].
     */
    fun setPINWithOTP(pin: String, otp: String, completion: (Completion<Boolean>) -> Unit) {
        credentialsManager.setPINWithOTP(pin, otp, completion)
    }

    /**
     * Checks if the [pin] is valid.
     * The following rules apply to verify the PIN:
     * - Must be 4 digits long
     * - Must use 3 different digits
     * - Must not be a numerical series (e.g. 1234, 4321, ...)
     *
     * @return True if the PIN is valid, false otherwise
     */
    fun isPINValid(pin: String): Boolean {
        return credentialsManager.isPINValid(pin)
    }

    /**
     * Sends a mail to the user that provides an OTP.
     *
     * @param completion The block to be called when the request is completed including either the information if the mail has been sent `successfully` or a [Throwable].
     */
    fun sendMailOTP(completion: (Completion<Boolean>) -> Unit) {
        credentialsManager.sendMailOTP(completion)
    }

    /**
     * Fetches a list of valid payment methods for the current user.
     *
     * @param completion The block to be called when the request is completed including either the payment methods or a [Throwable].
     */
    fun getValidPaymentMethods(completion: (Completion<PaymentMethods>) -> Unit) {
        API.paymentMethods.getPaymentMethodsIncludingCreditCheck(GetPaymentMethodsIncludingCreditCheckAPI.Filterstatus.VALID).handleCallback(completion)
    }

    /**
     * Fetches a list of transactions for the current user sorted in descending order by creation date.
     *
     * @param completion The block to be called when the request is completed including either the transactions or a [Throwable].
     */
    fun getTransactions(completion: (Completion<Transactions>) -> Unit) {
        API.paymentTransactions.listTransactions(sort = ListTransactionsAPI.Sort.CREATEDATDESCENDING).handleCallback(completion)
    }
}
