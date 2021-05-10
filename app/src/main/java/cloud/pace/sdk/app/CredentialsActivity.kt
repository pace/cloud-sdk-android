package cloud.pace.sdk.app

import android.os.Bundle
import android.text.InputType
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import cloud.pace.sdk.idkit.IDKit
import cloud.pace.sdk.utils.Completion
import cloud.pace.sdk.utils.Failure
import cloud.pace.sdk.utils.Success
import kotlinx.android.synthetic.main.activity_credentials.*

class CredentialsActivity : AppCompatActivity() {

    private var biometryRadioButtonId = R.id.radio_biometry_with_password
    private var pinRadioButtonId = R.id.radio_pin_with_biometry

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_credentials)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        is_biometry_enabled.setOnClickListener {
            Toast.makeText(this, if (IDKit.isBiometricAuthenticationEnabled()) "Biometric authentication is enabled" else "Biometric authentication is not enabled", Toast.LENGTH_SHORT).show()
        }

        is_pin_set.setOnClickListener {
            IDKit.isPINSet {
                when (it) {
                    is Success -> Toast.makeText(this, if (it.result) "PIN is set" else "PIN is not set", Toast.LENGTH_SHORT).show()
                    is Failure -> Toast.makeText(this, it.throwable.toString(), Toast.LENGTH_LONG).show()
                }
            }
        }

        is_password_set.setOnClickListener {
            IDKit.isPasswordSet {
                when (it) {
                    is Success -> Toast.makeText(this, if (it.result) "Password is set" else "Password is not set", Toast.LENGTH_SHORT).show()
                    is Failure -> Toast.makeText(this, it.throwable.toString(), Toast.LENGTH_LONG).show()
                }
            }
        }

        is_pin_or_password_set.setOnClickListener {
            IDKit.isPINOrPasswordSet {
                when (it) {
                    is Success -> {
                        val isPinSet = if (it.result.pin == true) "PIN is set" else "PIN is not set"
                        val isPasswordSet = if (it.result.password == true) "Password is set" else "Password is not set"
                        Toast.makeText(this, "$isPinSet and $isPasswordSet", Toast.LENGTH_SHORT).show()
                    }
                    is Failure -> Toast.makeText(this, it.throwable.toString(), Toast.LENGTH_LONG).show()
                }
            }
        }

        setup_biometry_button.setOnClickListener {
            val completion: (Completion<Boolean>) -> Unit = {
                when (it) {
                    is Success -> Toast.makeText(this, if (it.result) "Biometric authentication set" else "Biometric authentication not set", Toast.LENGTH_SHORT).show()
                    is Failure -> Toast.makeText(this, it.throwable.toString(), Toast.LENGTH_LONG).show()
                }
            }
            when (biometryRadioButtonId) {
                R.id.radio_biometry_with_password -> {
                    showAlert("Please enter password", InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD) {
                        if (!it.isNullOrBlank()) {
                            IDKit.enableBiometricAuthenticationWithPassword(it, completion)
                        }
                    }
                }
                R.id.radio_biometry_with_pin -> {
                    showAlert("Please enter PIN", InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD) {
                        if (!it.isNullOrBlank()) {
                            IDKit.enableBiometricAuthenticationWithPIN(it, completion)
                        }
                    }
                }
                R.id.radio_biometry_with_otp -> {
                    IDKit.sendMailOTP {
                        when (it) {
                            is Success -> {
                                Toast.makeText(this, if (it.result) "Mail was sent" else "Mail was not sent", Toast.LENGTH_SHORT).show()
                                if (it.result) {
                                    showAlert("Please enter mail OTP", InputType.TYPE_CLASS_NUMBER) { otp ->
                                        if (!otp.isNullOrBlank()) {
                                            IDKit.enableBiometricAuthenticationWithOTP(otp, completion)
                                        }
                                    }
                                }
                            }
                            is Failure -> Toast.makeText(this, it.throwable.toString(), Toast.LENGTH_LONG).show()
                        }
                    }
                }
                R.id.radio_biometry_after_login -> IDKit.enableBiometricAuthentication(completion)
            }
        }

        set_pin_button.setOnClickListener {
            val pin = pin_edit_text.text.toString()
            if (pin.isBlank()) {
                Toast.makeText(this, "Please enter PIN", Toast.LENGTH_SHORT).show()
            } else {
                val completion: (Completion<Boolean>) -> Unit = {
                    when (it) {
                        is Success -> Toast.makeText(this, if (it.result) "PIN set" else "PIN not set", Toast.LENGTH_SHORT).show()
                        is Failure -> Toast.makeText(this, it.throwable.toString(), Toast.LENGTH_LONG).show()
                    }
                }
                when (pinRadioButtonId) {
                    R.id.radio_pin_with_biometry -> IDKit.setPINWithBiometry(this, "Set PIN", "Confirm with fingerprint", pin = pin, completion = completion)
                    R.id.radio_pin_with_password -> {
                        showAlert("Please enter password", InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD) {
                            if (!it.isNullOrBlank()) {
                                IDKit.setPINWithPassword(pin, it, completion)
                            }
                        }
                    }
                    R.id.radio_pin_with_otp -> {
                        IDKit.sendMailOTP {
                            when (it) {
                                is Success -> {
                                    Toast.makeText(this, if (it.result) "Mail was sent" else "Mail was not sent", Toast.LENGTH_SHORT).show()
                                    if (it.result) {
                                        showAlert("Please enter mail OTP", InputType.TYPE_CLASS_NUMBER) { otp ->
                                            if (!otp.isNullOrBlank()) {
                                                IDKit.setPINWithOTP(pin, otp, completion)
                                            }
                                        }
                                    }
                                }
                                is Failure -> Toast.makeText(this, it.throwable.toString(), Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                }
            }
        }

        disable_biometry.setOnClickListener {
            IDKit.disableBiometricAuthentication()
        }

        send_mail_otp.setOnClickListener {
            IDKit.sendMailOTP {
                when (it) {
                    is Success -> Toast.makeText(this, if (it.result) "Mail was sent" else "Mail was not sent", Toast.LENGTH_SHORT).show()
                    is Failure -> Toast.makeText(this, it.throwable.toString(), Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun showAlert(title: String, inputType: Int, input: (String?) -> Unit) {
        val layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        layoutParams.setMargins(50, 20, 50, 10)

        val editText = EditText(this)
        editText.layoutParams = layoutParams
        editText.inputType = inputType

        val container = LinearLayout(this)
        container.orientation = LinearLayout.VERTICAL
        container.addView(editText, layoutParams)

        AlertDialog.Builder(this)
            .setView(container)
            .setTitle(title)
            .setPositiveButton("OK") { dialog, _ ->
                input(editText.text.toString())
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                input(null)
                dialog.cancel()
            }
            .create()
            .show()
    }

    fun onBiometryRadioButtonClicked(view: View) {
        if (view is RadioButton) {
            biometryRadioButtonId = view.id
        }
    }

    fun onPINRadioButtonClicked(view: View) {
        if (view is RadioButton) {
            pinRadioButtonId = view.id
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == android.R.id.home) {
            finish()
            true
        } else {
            super.onOptionsItemSelected(item)
        }
    }
}
