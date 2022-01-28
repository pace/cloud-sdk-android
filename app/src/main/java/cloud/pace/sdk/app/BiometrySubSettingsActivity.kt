package cloud.pace.sdk.app

import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.lifecycle.lifecycleScope
import cloud.pace.sdk.app.view.mainscreen.settings.BiometrySubSettingView
import cloud.pace.sdk.idkit.IDKit
import cloud.pace.sdk.utils.Completion
import cloud.pace.sdk.utils.Failure
import cloud.pace.sdk.utils.Success
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.launch

internal var userInfo = mutableStateOf("username")
internal var isPasswordSet = mutableStateOf(false)
internal var isPinSet = mutableStateOf(false)
internal var isPinOrPasswordSet = mutableStateOf("isPinOrPasswordSet")
internal var biometryStatus = mutableStateOf(false)

class BiometrySubSettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BiometrySubSettingView(
                this@BiometrySubSettingsActivity,
                enableBioAutByPasswordAction = {
                    IDKit.enableBiometricAuthenticationWithPassword(it) { completion ->
                        when (completion) {
                            is Success -> Toast.makeText(this, if (completion.result) "Biometric authentication set" else "Biometric authentication not set", Toast.LENGTH_SHORT).show()
                            is Failure -> Toast.makeText(this, completion.throwable.toString(), Toast.LENGTH_LONG).show()
                        }
                        biometryStatus.value = IDKit.isBiometricAuthenticationEnabled()
                    }
                },
                enableBioAutByPINAction = {
                    IDKit.enableBiometricAuthenticationWithPIN(it) { completion ->
                        when (completion) {
                            is Success -> Toast.makeText(this, if (completion.result) "Biometric authentication set" else "Biometric authentication not set", Toast.LENGTH_SHORT).show()
                            is Failure -> Toast.makeText(this, completion.throwable.toString(), Toast.LENGTH_LONG).show()
                        }
                        biometryStatus.value = IDKit.isBiometricAuthenticationEnabled()
                    }
                },
                enableBioAutByOTPAction = {
                    IDKit.enableBiometricAuthenticationWithOTP(it) { completion ->
                        when (completion) {
                            is Success -> Toast.makeText(this, if (completion.result) "Biometric authentication set" else "Biometric authentication not set", Toast.LENGTH_SHORT).show()
                            is Failure -> Toast.makeText(this, completion.throwable.toString(), Toast.LENGTH_LONG).show()
                        }
                        biometryStatus.value = IDKit.isBiometricAuthenticationEnabled()
                    }
                },
                setPinWithPasswordAction = { pinInputToSetWithPassword, passwordInputToSetPin ->
                    IDKit.setPINWithPassword(pinInputToSetWithPassword, passwordInputToSetPin) {
                        when (it) {
                            is Success -> Toast.makeText(this@BiometrySubSettingsActivity, if (it.result) "PIN set" else "PIN not set", Toast.LENGTH_SHORT).show()
                            is Failure -> Toast.makeText(this@BiometrySubSettingsActivity, it.throwable.toString(), Toast.LENGTH_LONG).show()
                        }
                    }

                },
                setPinWithOTPAction = { pin, otp ->
                    IDKit.setPINWithOTP(pin, otp) {
                        when (it) {
                            is Success -> Toast.makeText(this@BiometrySubSettingsActivity, if (it.result) "PIN set" else "PIN not set", Toast.LENGTH_SHORT).show()
                            is Failure -> Toast.makeText(this@BiometrySubSettingsActivity, it.throwable.toString(), Toast.LENGTH_LONG).show()
                        }
                    }
                },
                setPinWithBiometryAction = { pin ->
                    val completion: (Completion<Boolean>) -> Unit = {
                        when (it) {
                            is Success -> Toast.makeText(this, if (it.result) "PIN set" else "PIN not set", Toast.LENGTH_SHORT).show()
                            is Failure -> Toast.makeText(this, it.throwable.toString(), Toast.LENGTH_LONG).show()
                        }
                    }
                    IDKit.setPINWithBiometry(
                        this@BiometrySubSettingsActivity,
                        "Set PIN",
                        "Confirm with fingerprint",
                        isDeviceCredentialsAllowed = false,
                        pin = pin,
                        completion = completion
                    )
                },
            )
        }

        if (IDKit.isAuthorizationValid()) {
            IDKit.refreshToken { completion ->
                showUserEmail(completion)
            }
        } else {
            lifecycleScope.launch {
                IDKit.authorize(this@BiometrySubSettingsActivity) { completion ->
                    showUserEmail(completion)
                }
            }
        }

        biometryStatus.value = IDKit.isBiometricAuthenticationEnabled()

        IDKit.isPINSet {
            when (it) {
                is Success -> isPinSet.value = it.result
                is Failure -> Toast.makeText(this@BiometrySubSettingsActivity, it.throwable.toString(), Toast.LENGTH_LONG).show()
            }
        }

        IDKit.isPasswordSet {
            when (it) {
                is Success -> isPasswordSet.value = it.result
                is Failure -> Toast.makeText(this@BiometrySubSettingsActivity, it.throwable.toString(), Toast.LENGTH_LONG).show()
            }
        }

        IDKit.isPINOrPasswordSet {
            when (it) {
                is Success -> {
                    isPinOrPasswordSet.value = if (it.result.pin == true) {
                        "PIN is set"
                    } else {
                        "PIN is not set"
                    } + " and " + if (it.result.password == true) {
                        "Password is set"
                    } else {
                        "Password is not set"
                    }
                }
                is Failure -> Toast.makeText(this@BiometrySubSettingsActivity, it.throwable.toString(), Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun showUserEmail(completion: Completion<String?>) {
        when (completion) {
            is Success -> completion.result?.let { token ->
                IDKit.userInfo(token) { response ->
                    (response as? Success)?.result?.let { userInfo.value = it.email.toString() }
                }
            }
            is Failure -> userInfo.value = "Refresh error: ${completion.throwable.message}"
        }
    }
}
