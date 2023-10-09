package car.pace.cofu.ui.onboarding.twofactor.biometric

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.fragment.app.FragmentActivity

@Composable
fun rememberBiometricManager(): BiometricManager {
    val context = LocalContext.current
    return remember {
        BiometricManager.from(context)
    }
}

@Composable
fun rememberBiometricPrompt(
    onSuccess: (result: BiometricPrompt.AuthenticationResult) -> Unit,
    onError: (errorCode: Int, errString: CharSequence) -> Unit
): BiometricPrompt {
    val activity = LocalContext.current.findActivity<FragmentActivity>()

    return remember {
        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                onSuccess(result)
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                onError(errorCode, errString)
            }
        }

        BiometricPrompt(activity, callback)
    }
}

/**
 * Find the closest Activity in a given Context.
 */
inline fun <reified T : Activity> Context.findActivity(): T {
    var context = this
    while (context is ContextWrapper) {
        if (context is T) return context
        context = context.baseContext
    }
    throw IllegalStateException("Context is not of type ${T::class.simpleName}")
}
