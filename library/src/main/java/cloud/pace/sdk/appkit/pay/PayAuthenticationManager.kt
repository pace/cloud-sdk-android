package cloud.pace.sdk.appkit.pay

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK

interface PayAuthenticationManager {

    fun isFingerprintAvailable(): Boolean
}

class PayAuthenticationManagerImpl(private val context: Context) : PayAuthenticationManager {

    override fun isFingerprintAvailable(): Boolean {
        return BiometricManager.from(context).canAuthenticate(BIOMETRIC_WEAK) == BiometricManager.BIOMETRIC_SUCCESS
    }
}
