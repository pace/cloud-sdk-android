package cloud.pace.sdk.idkit

object InvalidSession : Exception("The current session is either invalid or expired which may be caused by resetting the session beforehand. Authorize again to create a new session.")
object FailedRetrievingSessionWhileAuthorizing : Exception("The authorization request failed because the session couldn't be retrieved.")
object FailedRetrievingConfigurationWhileDiscovering : Exception("The discovery failed because the configuration couldn't be retrieved.")
