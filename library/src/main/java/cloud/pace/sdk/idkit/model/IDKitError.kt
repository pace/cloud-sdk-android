package cloud.pace.sdk.idkit.model

object InvalidSession : Exception("The current session is either invalid or expired which may be caused by resetting the session beforehand. Authorize again to create a new session.")
object FailedRetrievingSessionWhileAuthorizing : Exception("The authorization request failed because the session couldn't be retrieved.")
object FailedRetrievingConfigurationWhileDiscovering : Exception("The discovery failed because the configuration couldn't be retrieved.")
object FailedRetrievingSessionWhileEnding : Exception("The end session request failed because the session couldn't be retrieved.")
object UserEndpointNotDefined : Exception("The user endpoint was not defined.")
object InternalError : Exception("An internal error occurred.")
object BiometricAuthenticationNotSupported : Exception("The device doesn't have biometric authentication.")
object BiometricAuthenticationNotSet : Exception("Biometric authentication is not set.")
object PINNotSecure : Exception("PIN not secure.")
object OperationCanceled : Exception("Operation was canceled by the user.")
object NoSupportedBrowser : Exception("There is no supported custom tab browser installed on the device.")
