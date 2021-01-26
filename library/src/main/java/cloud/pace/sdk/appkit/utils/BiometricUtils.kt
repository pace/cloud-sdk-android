package cloud.pace.sdk.appkit.utils

import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import cloud.pace.sdk.R

object BiometricUtils {

    fun requestAuthentication(fragment: Fragment, title: String, subtitle: String = "", onSuccess: () -> Unit, onFailure: (errorCode: Int, errString: CharSequence) -> Unit) {
        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                onSuccess()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                onFailure(errorCode, errString)
            }
        }

        val promptInfoBuilder = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setNegativeButtonText(fragment.getText(R.string.general_cancel))

        val executor = ContextCompat.getMainExecutor(fragment.context)
        val biometricPrompt = BiometricPrompt(fragment, executor, callback)
        biometricPrompt.authenticate(promptInfoBuilder.build())
    }
}
