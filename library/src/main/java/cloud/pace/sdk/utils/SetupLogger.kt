package cloud.pace.sdk.utils

import cloud.pace.sdk.idkit.model.OIDConfiguration
import timber.log.Timber

object SetupLogger {
    var apiKey: String? = null
    var redirectScheme: String? = null
    var environment: Environment? = null
    var domainACL: List<String>? = null
    var checkRedirectScheme: Boolean? = null
    var missingValues: MutableList<String> = mutableListOf()

    var appAuthRedirectScheme: String? = null
    var oidConfiguration: OIDConfiguration? = null
    var missingIDKitValues: MutableList<String> = mutableListOf()

    fun preCheckSetup() {
        if (apiKey.isNullOrEmpty())
            missingValues.add("API key")

        if (redirectScheme.isNullOrEmpty() && checkRedirectScheme == true)
            missingValues.add("Redirect scheme")

        if (environment == null)
            missingValues.add("Environment")

        if (environment != Environment.PRODUCTION)
            Timber.w("Current environment is not set to 'production' but to: ${environment?.name}")

        if (missingValues.isEmpty())
            Timber.i(
                "PACECloudSDK setup successful. You are currently running the SDK as follows:\nAPI key is set\nRedirect scheme: ${
                    redirectScheme ?: "disabled"
                }\nEnviromment: ${environment?.name}"
            )
        else
            Timber.w("We've noticed PACECloudSDK setup is missing values for: ${missingValues.joinToString(", ")}")
    }

    fun preCheckIDKitSetup() {
        if (appAuthRedirectScheme.isNullOrEmpty())
            missingIDKitValues.add("appAuthRedirectScheme")

        if (oidConfiguration == null)
            missingIDKitValues.add("oidConfiguration")

        if (missingIDKitValues.isEmpty())
            Timber.i("IDKit setup successful")
        else
            Timber.w("We've noticed IDKit setup is missing values for: ${missingIDKitValues.joinToString(", ")}")
    }

    fun logSDKWarningIfNeeded() {
        if (missingValues.isNotEmpty()) {
            Timber.w("You haven't set any PACECloudSDK values for: ${missingValues.joinToString(", ")}")
        }
    }

    fun logBiometryWarningIfNeeded() {
        if (domainACL.isNullOrEmpty()) {
            Timber.w("We've noticed that you are using IDKits's 2FA methods but haven't set up a valid 'domainACL' yet. Please do so in your PACECloudSDK's configuration")
        }
    }
}
