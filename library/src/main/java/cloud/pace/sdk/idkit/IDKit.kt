package cloud.pace.sdk.idkit

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import cloud.pace.sdk.api.user.generated.model.PinOrPassword
import cloud.pace.sdk.idkit.authorization.AuthorizationManager
import cloud.pace.sdk.idkit.credentials.CredentialsManager
import cloud.pace.sdk.idkit.model.OIDConfiguration
import cloud.pace.sdk.idkit.model.ServiceConfiguration
import cloud.pace.sdk.idkit.userinfo.UserInfoResponse
import cloud.pace.sdk.utils.Completion
import cloud.pace.sdk.utils.IDKitKoinComponent
import cloud.pace.sdk.utils.KoinConfig
import cloud.pace.sdk.utils.SetupLogger
import org.koin.core.inject

object IDKit : IDKitKoinComponent {

    internal var isInitialized = false
    private val authorizationManager: AuthorizationManager by inject()
    private val credentialsManager: CredentialsManager by inject()

    /**
     * Sets up [IDKit] with the passed [configuration].
     *
     * @param context The context.
     * @param additionalCaching If set to `false` persistent session cookies will be shared only natively.
     * If set to `true` the session will additionally be persisted by [IDKit] to improve the change of not having to resign in again. Defaults to `true`.
     */
    @JvmOverloads
    fun setup(context: Context, configuration: OIDConfiguration, additionalCaching: Boolean = true) {
        SetupLogger.logSDKWarningIfNeeded()
        KoinConfig.setupIDKit(context)
        authorizationManager.setup(configuration, additionalCaching)
        isInitialized = true

        val applicationInfo = context.packageManager?.getApplicationInfo(context.packageName, PackageManager.GET_META_DATA)
        val appAuthRedirectScheme = applicationInfo?.metaData?.get("appAuthRedirectScheme")?.toString()

        SetupLogger.appAuthRedirectScheme = appAuthRedirectScheme
        SetupLogger.oidConfiguration = configuration
        SetupLogger.preCheckIDKitSetup()
    }

    /**
     * Performs a discovery to retrieve a [ServiceConfiguration].
     *
     * @param issuerUri The issuer URI.
     * @param completion The block to be called when the discovery is complete either including the [ServiceConfiguration] or a [Throwable].
     */
    fun discoverConfiguration(issuerUri: String, completion: (Completion<ServiceConfiguration>) -> Unit) {
        SetupLogger.logSDKWarningIfNeeded()
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
        SetupLogger.logSDKWarningIfNeeded()
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
        SetupLogger.logSDKWarningIfNeeded()
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
        SetupLogger.logSDKWarningIfNeeded()
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
        SetupLogger.logSDKWarningIfNeeded()
        return authorizationManager.authorize()
    }

    /**
     * Sends a request to the authorization service to exchange a code granted as part of an authorization request for a token.
     *
     * @param intent Represents the [Intent] from the [Chrome Custom Tab](https://developer.chrome.com/multidevice/android/customtabs).
     * @param completion The block to be called when the request is completed including either a valid `accessToken` or [Throwable].
     */
    fun handleAuthorizationResponse(intent: Intent, completion: (Completion<String?>) -> Unit) {
        SetupLogger.logSDKWarningIfNeeded()
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
        SetupLogger.logSDKWarningIfNeeded()
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
        SetupLogger.logSDKWarningIfNeeded()
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
        SetupLogger.logSDKWarningIfNeeded()
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
        SetupLogger.logSDKWarningIfNeeded()
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
        SetupLogger.logSDKWarningIfNeeded()
        return authorizationManager.endSession()
    }

    /**
     * Resets the local session object.
     *
     * @param intent Represents the [Intent] from the [Chrome Custom Tab](https://developer.chrome.com/multidevice/android/customtabs).
     * @param completion The block to be called when the session has been reset or a [Throwable] when an exception occurred.
     */
    fun handleEndSessionResponse(intent: Intent, completion: (Completion<Unit>) -> Unit) {
        SetupLogger.logSDKWarningIfNeeded()
        authorizationManager.handleEndSessionResponse(intent, completion)
    }

    /**
     * Checks the current authorization state. Returning `true` does not mean that the access is fresh - just that it was valid the last time it was used.
     *
     * @return The current state of the authorization.
     */
    fun isAuthorizationValid(): Boolean {
        SetupLogger.logSDKWarningIfNeeded()
        return authorizationManager.isAuthorizationValid()
    }

    /**
     * Checks if the [intent] contains an authorization response.
     *
     * @return True if the [intent] has an authorization response extra, false otherwise.
     */
    fun containsAuthorizationResponse(intent: Intent): Boolean {
        SetupLogger.logSDKWarningIfNeeded()
        return authorizationManager.containsAuthorizationResponse(intent)
    }

    /**
     * Checks if the [intent] contains an end session response.
     *
     * @return True if the [intent] has an end session response extra, false otherwise.
     */
    fun containsEndSessionResponse(intent: Intent): Boolean {
        SetupLogger.logSDKWarningIfNeeded()
        return authorizationManager.containsEndSessionResponse(intent)
    }

    /**
     * Checks if the [intent] contains an authorization/end session exception.
     *
     * @return True if the [intent] has an exception extra, false otherwise.
     */
    fun containsException(intent: Intent): Boolean {
        SetupLogger.logSDKWarningIfNeeded()
        return authorizationManager.containsException(intent)
    }

    /**
     * Returns the cached accessToken of the current session or null if the session is not initialized or has no accessToken.
     */
    fun cachedToken(): String? {
        SetupLogger.logSDKWarningIfNeeded()
        return authorizationManager.cachedToken()
    }

    /**
     * Retrieves the currently authorized user's information.
     *
     * @param accessToken The user's access token for which to retrieve the user info.
     * @param completion The block to be called when the request is completed including either a valid `userInfo` or a [Throwable].
     */
    fun userInfo(accessToken: String, completion: (Completion<UserInfoResponse>) -> Unit) {
        SetupLogger.logSDKWarningIfNeeded()
        authorizationManager.userInfo(accessToken, completion)
    }

    /**
     * Checks if biometric authentication is enabled for the current user.
     *
     * @return The information if biometric authentication is enabled.
     */
    fun isBiometricAuthenticationEnabled(): Boolean {
        SetupLogger.logSDKWarningIfNeeded()
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
        SetupLogger.logSDKWarningIfNeeded()
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
        SetupLogger.logSDKWarningIfNeeded()
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
        SetupLogger.logSDKWarningIfNeeded()
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
        SetupLogger.logSDKWarningIfNeeded()
        SetupLogger.logBiometryWarningIfNeeded()
        credentialsManager.enableBiometricAuthentication(completion = completion)
    }

    /**
     * Disables biometric authentication for the current user.
     */
    fun disableBiometricAuthentication() {
        SetupLogger.logSDKWarningIfNeeded()
        SetupLogger.logBiometryWarningIfNeeded()
        credentialsManager.disableBiometricAuthentication()
    }

    /**
     * Checks if there is an active PIN set for the currently authenticated user.
     *
     * @param completion The block to be called when the request is completed including either the `pinStatus` or a [Throwable].
     */
    fun isPINSet(completion: (Completion<Boolean>) -> Unit) {
        SetupLogger.logSDKWarningIfNeeded()
        credentialsManager.isPINSet(completion)
    }

    /**
     * Checks if there is an active password set for the currently authenticated user.
     *
     * @param completion The block to be called when the request is completed including either the `passwordStatus` or a [Throwable].
     */
    fun isPasswordSet(completion: (Completion<Boolean>) -> Unit) {
        SetupLogger.logSDKWarningIfNeeded()
        credentialsManager.isPasswordSet(completion)
    }

    /**
     * Checks if there is an active PIN or password set and verified for the currently authenticated user.
     *
     * @param completion The block to be called when the request is completed including either the [PinOrPassword] status or a [Throwable].
     */
    fun isPINOrPasswordSet(completion: (Completion<PinOrPassword>) -> Unit) {
        SetupLogger.logSDKWarningIfNeeded()
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
        SetupLogger.logSDKWarningIfNeeded()
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
        SetupLogger.logSDKWarningIfNeeded()
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
        SetupLogger.logSDKWarningIfNeeded()
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
        SetupLogger.logSDKWarningIfNeeded()
        credentialsManager.setPINWithOTP(pin, otp, completion)
    }

    /**
     * Sends a mail to the user that provides an OTP.
     *
     * @param completion The block to be called when the request is completed including either the information if the mail has been sent `successfully` or a [Throwable].
     */
    fun sendMailOTP(completion: (Completion<Boolean>) -> Unit) {
        SetupLogger.logSDKWarningIfNeeded()
        credentialsManager.sendMailOTP(completion)
    }
}
