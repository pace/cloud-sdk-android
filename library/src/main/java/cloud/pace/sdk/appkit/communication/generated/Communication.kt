//
// Generated by KotlinPoet:
// https://github.com/square/kotlinpoet
//    
// Please do not edit!
//
package cloud.pace.sdk.appkit.communication.generated

import cloud.pace.sdk.appkit.communication.generated.model.request.AppRedirectRequest
import cloud.pace.sdk.appkit.communication.generated.model.request.ApplePayAvailabilityCheckRequest
import cloud.pace.sdk.appkit.communication.generated.model.request.DisableRequest
import cloud.pace.sdk.appkit.communication.generated.model.request.GetAccessTokenRequest
import cloud.pace.sdk.appkit.communication.generated.model.request.GetConfigRequest
import cloud.pace.sdk.appkit.communication.generated.model.request.GetSecureDataRequest
import cloud.pace.sdk.appkit.communication.generated.model.request.GetTOTPRequest
import cloud.pace.sdk.appkit.communication.generated.model.request.GooglePayAvailabilityCheckRequest
import cloud.pace.sdk.appkit.communication.generated.model.request.GooglePayPaymentRequest
import cloud.pace.sdk.appkit.communication.generated.model.request.ImageDataRequest
import cloud.pace.sdk.appkit.communication.generated.model.request.LogEventRequest
import cloud.pace.sdk.appkit.communication.generated.model.request.OpenURLInNewTabRequest
import cloud.pace.sdk.appkit.communication.generated.model.request.SetSecureDataRequest
import cloud.pace.sdk.appkit.communication.generated.model.request.SetTOTPRequest
import cloud.pace.sdk.appkit.communication.generated.model.request.SetUserPropertyRequest
import cloud.pace.sdk.appkit.communication.generated.model.request.ShareFileRequest
import cloud.pace.sdk.appkit.communication.generated.model.request.ShareTextRequest
import cloud.pace.sdk.appkit.communication.generated.model.request.StartNavigationRequest
import cloud.pace.sdk.appkit.communication.generated.model.request.VerifyLocationRequest
import cloud.pace.sdk.appkit.communication.generated.model.response.AppInterceptableLinkResult
import cloud.pace.sdk.appkit.communication.generated.model.response.AppRedirectResult
import cloud.pace.sdk.appkit.communication.generated.model.response.ApplePayAvailabilityCheckResult
import cloud.pace.sdk.appkit.communication.generated.model.response.BackResult
import cloud.pace.sdk.appkit.communication.generated.model.response.CloseResult
import cloud.pace.sdk.appkit.communication.generated.model.response.DisableResult
import cloud.pace.sdk.appkit.communication.generated.model.response.GetAccessTokenResult
import cloud.pace.sdk.appkit.communication.generated.model.response.GetBiometricStatusResult
import cloud.pace.sdk.appkit.communication.generated.model.response.GetConfigResult
import cloud.pace.sdk.appkit.communication.generated.model.response.GetLocationResult
import cloud.pace.sdk.appkit.communication.generated.model.response.GetSecureDataResult
import cloud.pace.sdk.appkit.communication.generated.model.response.GetTOTPResult
import cloud.pace.sdk.appkit.communication.generated.model.response.GetTraceIdResult
import cloud.pace.sdk.appkit.communication.generated.model.response.GooglePayAvailabilityCheckResult
import cloud.pace.sdk.appkit.communication.generated.model.response.GooglePayPaymentResult
import cloud.pace.sdk.appkit.communication.generated.model.response.ImageDataResult
import cloud.pace.sdk.appkit.communication.generated.model.response.IntrospectResult
import cloud.pace.sdk.appkit.communication.generated.model.response.IsBiometricAuthEnabledResult
import cloud.pace.sdk.appkit.communication.generated.model.response.IsRemoteConfigAvailableResult
import cloud.pace.sdk.appkit.communication.generated.model.response.IsSignedInResult
import cloud.pace.sdk.appkit.communication.generated.model.response.LogEventResult
import cloud.pace.sdk.appkit.communication.generated.model.response.LogoutResult
import cloud.pace.sdk.appkit.communication.generated.model.response.OpenURLInNewTabResult
import cloud.pace.sdk.appkit.communication.generated.model.response.SetSecureDataResult
import cloud.pace.sdk.appkit.communication.generated.model.response.SetTOTPResult
import cloud.pace.sdk.appkit.communication.generated.model.response.SetUserPropertyResult
import cloud.pace.sdk.appkit.communication.generated.model.response.ShareFileResult
import cloud.pace.sdk.appkit.communication.generated.model.response.ShareTextResult
import cloud.pace.sdk.appkit.communication.generated.model.response.StartNavigationResult
import cloud.pace.sdk.appkit.communication.generated.model.response.VerifyLocationResult

/**
 * Used for receiving messages from the PWA. These methods are called when the listener is
 * registered with the [CommunicationManager].
 */
public interface Communication {
    /**
     * Requests a collection of the supported version and operations.
     *
     * @param timeout The timeout of introspect in milliseconds or null if no timeout should be used
     */
    public suspend fun introspect(timeout: Long?): IntrospectResult

    /**
     * Requests to close the current PWA.
     *
     * @param timeout The timeout of close in milliseconds or null if no timeout should be used
     */
    public suspend fun close(timeout: Long?): CloseResult

    /**
     * Requests to logout the current user.
     *
     * @param timeout The timeout of logout in milliseconds or null if no timeout should be used
     */
    public suspend fun logout(timeout: Long?): LogoutResult

    /**
     * Requests the biometric status, i.e. whether biometric authentication is possible (e.g. via
     * fingerprint or face recognition).
     *
     * @param timeout The timeout of getBiometricStatus in milliseconds or null if no timeout should
     * be used
     */
    public suspend fun getBiometricStatus(timeout: Long?): GetBiometricStatusResult

    /**
     * Requests to save TOTP secret data on the device for later retrieval.
     *
     * @param timeout The timeout of setTOTP in milliseconds or null if no timeout should be used
     * @param setTOTPRequest The setTOTP request body object
     */
    public suspend fun setTOTP(timeout: Long?, setTOTPRequest: SetTOTPRequest): SetTOTPResult

    /**
     * Return a TOTP generated by previously saved TOTP secret data for the specified key. The user
     * should authenticate the access to the secret data e.g. with biometric authentication.
     *
     * @param timeout The timeout of getTOTP in milliseconds or null if no timeout should be used
     * @param getTOTPRequest The getTOTP request body object
     */
    public suspend fun getTOTP(timeout: Long?, getTOTPRequest: GetTOTPRequest): GetTOTPResult

    /**
     * Requests to save a string securely on the device for later retrieval.
     *
     * @param timeout The timeout of setSecureData in milliseconds or null if no timeout should be
     * used
     * @param setSecureDataRequest The setSecureData request body object
     */
    public suspend fun setSecureData(timeout: Long?, setSecureDataRequest: SetSecureDataRequest):
        SetSecureDataResult

    /**
     * Retrieve a previously saved string value by key. The user should authenticate the access to the
     * string e.g. with biometric authentication.
     *
     * @param timeout The timeout of getSecureData in milliseconds or null if no timeout should be
     * used
     * @param getSecureDataRequest The getSecureData request body object
     */
    public suspend fun getSecureData(timeout: Long?, getSecureDataRequest: GetSecureDataRequest):
        GetSecureDataResult

    /**
     * The current app will no longer be displayed up until the given date.
     *
     * @param timeout The timeout of disable in milliseconds or null if no timeout should be used
     * @param disableRequest The disable request body object
     */
    public suspend fun disable(timeout: Long?, disableRequest: DisableRequest): DisableResult

    /**
     * Open the given url in a new browser tab.
     * This specifically is the case in the payment process.
     * Only works when a client schema is set and returned via `appInterceptableLink` or if integrated
     * is set to true (assuming client listens to default schema: `cloudsdk`).
     *
     * @param timeout The timeout of openURLInNewTab in milliseconds or null if no timeout should be
     * used
     * @param openURLInNewTabRequest The openURLInNewTab request body object
     */
    public suspend fun openURLInNewTab(
        timeout: Long?,
        openURLInNewTabRequest: OpenURLInNewTabRequest
    ): OpenURLInNewTabResult

    /**
     * In specific situations the app needs the user's current location to be verified in order to
     * continue its flow.
     *
     * @param timeout The timeout of verifyLocation in milliseconds or null if no timeout should be
     * used
     * @param verifyLocationRequest The verifyLocation request body object
     */
    public suspend fun verifyLocation(timeout: Long?, verifyLocationRequest: VerifyLocationRequest):
        VerifyLocationResult

    /**
     * Requests a fresh access token for the currently authenticated user.
     *
     * @param timeout The timeout of getAccessToken in milliseconds or null if no timeout should be
     * used
     * @param getAccessTokenRequest The getAccessToken request body object
     */
    public suspend fun getAccessToken(timeout: Long?, getAccessTokenRequest: GetAccessTokenRequest):
        GetAccessTokenResult

    /**
     * Send an base64 encoded png image for sharing with the user.
     *
     * @param timeout The timeout of imageData in milliseconds or null if no timeout should be used
     * @param imageDataRequest The imageData request body object
     */
    public suspend fun imageData(timeout: Long?, imageDataRequest: ImageDataRequest): ImageDataResult

    /**
     * Requests, if Apple Pay is ready to be used (enabled + cards onboarded; iOS only)
     *
     * @param timeout The timeout of applePayAvailabilityCheck in milliseconds or null if no timeout
     * should be used
     * @param applePayAvailabilityCheckRequest The applePayAvailabilityCheck request body object
     */
    public suspend fun applePayAvailabilityCheck(
        timeout: Long?,
        applePayAvailabilityCheckRequest: ApplePayAvailabilityCheckRequest
    ):
        ApplePayAvailabilityCheckResult

    /**
     * The app requests navigating back.
     *
     * @param timeout The timeout of back in milliseconds or null if no timeout should be used
     */
    public suspend fun back(timeout: Long?): BackResult

    /**
     * Some services (e.g. Paypal) will be opened independently from the web app, see
     * "openURLInNewTab".
     * On completion, the flow will be redirected to the schema provided to this request.
     * This needs to be a unique identifier, e.g. "pace.some_uuid".
     *
     * @param timeout The timeout of appInterceptableLink in milliseconds or null if no timeout should
     * be used
     */
    public suspend fun appInterceptableLink(timeout: Long?): AppInterceptableLinkResult

    /**
     * Requests to set a user property for the current user in the analytics backend (e.g. Firebase).
     *
     * @param timeout The timeout of setUserProperty in milliseconds or null if no timeout should be
     * used
     * @param setUserPropertyRequest The setUserProperty request body object
     */
    public suspend fun setUserProperty(
        timeout: Long?,
        setUserPropertyRequest: SetUserPropertyRequest
    ): SetUserPropertyResult

    /**
     * Requests to log an event to the analytics backend (e.g. Firebase).
     *
     * @param timeout The timeout of logEvent in milliseconds or null if no timeout should be used
     * @param logEventRequest The logEvent request body object
     */
    public suspend fun logEvent(timeout: Long?, logEventRequest: LogEventRequest): LogEventResult

    /**
     * Requests a configuration value which was defined externally (e.g. via Firebase).
     * Note that the value will always be returned as a string, regardless of the actual type.
     *
     * @param timeout The timeout of getConfig in milliseconds or null if no timeout should be used
     * @param getConfigRequest The getConfig request body object
     */
    public suspend fun getConfig(timeout: Long?, getConfigRequest: GetConfigRequest): GetConfigResult

    /**
     * Requests a unique identifier for the user session for tracing purposes.
     * This must return a new unique value on every call.
     *
     * @param timeout The timeout of getTraceId in milliseconds or null if no timeout should be used
     */
    public suspend fun getTraceId(timeout: Long?): GetTraceIdResult

    /**
     * Requests the current user location as provided by e.g. GPS.
     *
     * @param timeout The timeout of getLocation in milliseconds or null if no timeout should be used
     */
    public suspend fun getLocation(timeout: Long?): GetLocationResult

    /**
     * Asks the client for permission to redirect to another web app than the current one.
     * The client can decide whether the app switch should be allowed or disallowed and intercepted by
     * the client
     * (e.g. a client with an own map might not want the web app to redirect to the
     * fuel-station-finder).
     * By default, redirects should always be allowed.
     *
     * @param timeout The timeout of appRedirect in milliseconds or null if no timeout should be used
     * @param appRedirectRequest The appRedirect request body object
     */
    public suspend fun appRedirect(timeout: Long?, appRedirectRequest: AppRedirectRequest):
        AppRedirectResult

    /**
     * Checks if biometric authentication is enabled.
     *
     * @param timeout The timeout of isBiometricAuthEnabled in milliseconds or null if no timeout
     * should be used
     */
    public suspend fun isBiometricAuthEnabled(timeout: Long?): IsBiometricAuthEnabledResult

    /**
     * Checks if user is signed in.
     *
     * @param timeout The timeout of isSignedIn in milliseconds or null if no timeout should be used
     */
    public suspend fun isSignedIn(timeout: Long?): IsSignedInResult

    /**
     * Checks if remote config is available.
     *
     * @param timeout The timeout of isRemoteConfigAvailable in milliseconds or null if no timeout
     * should be used
     */
    public suspend fun isRemoteConfigAvailable(timeout: Long?): IsRemoteConfigAvailableResult

    /**
     * Request to offer the user to share a text via the native share sheet
     *
     * @param timeout The timeout of shareText in milliseconds or null if no timeout should be used
     * @param shareTextRequest The shareText request body object
     */
    public suspend fun shareText(timeout: Long?, shareTextRequest: ShareTextRequest): ShareTextResult

    /**
     * Requests, if Google Pay is ready to be used (enabled + cards onboarded; Android only)
     *
     * @param timeout The timeout of googlePayAvailabilityCheck in milliseconds or null if no timeout
     * should be used
     * @param googlePayAvailabilityCheckRequest The googlePayAvailabilityCheck request body object
     */
    public suspend fun googlePayAvailabilityCheck(
        timeout: Long?,
        googlePayAvailabilityCheckRequest: GooglePayAvailabilityCheckRequest
    ):
        GooglePayAvailabilityCheckResult

    /**
     * The Google Pay payment request to be handled (Android only).
     *
     * @param timeout The timeout of googlePayPayment in milliseconds or null if no timeout should be
     * used
     * @param googlePayPaymentRequest The googlePayPayment request body object
     */
    public suspend fun googlePayPayment(
        timeout: Long?,
        googlePayPaymentRequest: GooglePayPaymentRequest
    ): GooglePayPaymentResult

    /**
     * Requests to start the navigation to the specified coordinates.
     *
     * @param timeout The timeout of startNavigation in milliseconds or null if no timeout should be
     * used
     * @param startNavigationRequest The startNavigation request body object
     */
    public suspend fun startNavigation(
        timeout: Long?,
        startNavigationRequest: StartNavigationRequest
    ): StartNavigationResult

    /**
     * Send an base64 encoded file for sharing with the user.
     *
     * @param timeout The timeout of shareFile in milliseconds or null if no timeout should be used
     * @param shareFileRequest The shareFile request body object
     */
    public suspend fun shareFile(timeout: Long?, shareFileRequest: ShareFileRequest): ShareFileResult
}
