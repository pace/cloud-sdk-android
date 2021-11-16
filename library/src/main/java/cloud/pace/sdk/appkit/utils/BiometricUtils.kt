package cloud.pace.sdk.appkit.utils

import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import cloud.pace.sdk.R

object BiometricUtils {

    /**
     * If [isDeviceCredentialsAllowed] is set to `true` [setNegativeButtonText][BiometricPrompt.PromptInfo.Builder.setNegativeButtonText] is not allowed
     * because it will replace the negative button on the prompt. If [isDeviceCredentialsAllowed] is set to `false`, the negative button text must be set with the [cancelText] parameter
     * or omit it to use the fallback cancel text.
     */
    fun requestAuthentication(
        fragment: Fragment,
        title: String,
        subtitle: String? = null,
        cancelText: String? = null,
        isDeviceCredentialsAllowed: Boolean = true,
        onSuccess: () -> Unit,
        onFailure: (errorCode: Int, errString: CharSequence) -> Unit
    ) {
        val callback = getCallback(onSuccess, onFailure)
        val negativeText = cancelText ?: fragment.getText(R.string.general_cancel)
        val promptInfoBuilder = getPromptInfoBuilder(title, subtitle, negativeText, isDeviceCredentialsAllowed)
        fragment.context?.let {
            val executor = ContextCompat.getMainExecutor(it)
            val biometricPrompt = BiometricPrompt(fragment, executor, callback)
            biometricPrompt.authenticate(promptInfoBuilder.build())
        }
    }

    /**
     * If [isDeviceCredentialsAllowed] is set to `true` [setNegativeButtonText][BiometricPrompt.PromptInfo.Builder.setNegativeButtonText] is not allowed
     * because it will replace the negative button on the prompt. If [isDeviceCredentialsAllowed] is set to `false`, the negative button text must be set with the [cancelText] parameter
     * or omit it to use the fallback cancel text.
     */
    fun requestAuthentication(
        activity: FragmentActivity,
        title: String,
        subtitle: String? = null,
        cancelText: String? = null,
        isDeviceCredentialsAllowed: Boolean = true,
        onSuccess: () -> Unit,
        onFailure: (errorCode: Int, errString: CharSequence) -> Unit
    ) {
        val callback = getCallback(onSuccess, onFailure)
        val negativeText = cancelText ?: activity.getText(R.string.general_cancel)
        val promptInfoBuilder = getPromptInfoBuilder(title, subtitle, negativeText, isDeviceCredentialsAllowed)
        val executor = ContextCompat.getMainExecutor(activity)
        val biometricPrompt = BiometricPrompt(activity, executor, callback)
        biometricPrompt.authenticate(promptInfoBuilder.build())
    }

    private fun getCallback(onSuccess: () -> Unit, onFailure: (errorCode: Int, errString: CharSequence) -> Unit) =
        object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                onSuccess()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                onFailure(errorCode, errString)
            }
        }

    private fun getPromptInfoBuilder(title: String, subtitle: String?, negativeButtonText: CharSequence, isDeviceCredentialsAllowed: Boolean): BiometricPrompt.PromptInfo.Builder {
        val builder = BiometricPrompt.PromptInfo.Builder().setTitle(title).setSubtitle(subtitle)

        if (isDeviceCredentialsAllowed) {
            // This combination of authenticator types should be allowed on all API levels we support
            builder.setAllowedAuthenticators(BIOMETRIC_WEAK or DEVICE_CREDENTIAL)
        } else {
            builder.setNegativeButtonText(negativeButtonText)
        }

        return builder
    }
}
