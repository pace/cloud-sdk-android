package cloud.pace.sdk.appkit.utils

import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import cloud.pace.sdk.R

object BiometricUtils {

    fun requestAuthentication(
        fragment: Fragment,
        title: String,
        subtitle: String? = null,
        cancelText: String? = null,
        onSuccess: () -> Unit,
        onFailure: (errorCode: Int, errString: CharSequence) -> Unit
    ) {
        val callback = getCallback(onSuccess, onFailure)
        val negativeText = cancelText ?: fragment.getText(R.string.general_cancel)
        val promptInfoBuilder = getPromptInfoBuilder(title, subtitle, negativeText)
        val executor = ContextCompat.getMainExecutor(fragment.context)
        val biometricPrompt = BiometricPrompt(fragment, executor, callback)
        biometricPrompt.authenticate(promptInfoBuilder.build())
    }

    fun requestAuthentication(
        activity: FragmentActivity,
        title: String,
        subtitle: String? = null,
        cancelText: String? = null,
        onSuccess: () -> Unit,
        onFailure: (errorCode: Int, errString: CharSequence) -> Unit
    ) {
        val callback = getCallback(onSuccess, onFailure)
        val negativeText = cancelText ?: activity.getText(R.string.general_cancel)
        val promptInfoBuilder = getPromptInfoBuilder(title, subtitle, negativeText)
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

    private fun getPromptInfoBuilder(title: String, subtitle: String?, negativeButtonText: CharSequence) =
        BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setNegativeButtonText(negativeButtonText)
}
