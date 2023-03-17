package cloud.pace.sdk.app.ui.components.settings

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cloud.pace.sdk.app.BiometrySubSettingsActivity
import cloud.pace.sdk.app.biometryStatus
import cloud.pace.sdk.app.isPasswordSet
import cloud.pace.sdk.app.isPinOrPasswordSet
import cloud.pace.sdk.app.isPinSet
import cloud.pace.sdk.app.ui.theme.ButtonCornerShape
import cloud.pace.sdk.app.userInfo
import cloud.pace.sdk.idkit.IDKit
import cloud.pace.sdk.utils.Failure
import cloud.pace.sdk.utils.Success

@Composable
fun BiometrySubSettingView(
    activity: BiometrySubSettingsActivity,
    enableBioAutByPasswordAction: (String) -> Unit,
    enableBioAutByPINAction: (String) -> Unit,
    enableBioAutByOTPAction: (String) -> Unit,
    setPinWithPasswordAction: (String, String) -> Unit,
    setPinWithOTPAction: (String, String) -> Unit,
    setPinWithBiometryAction: (String) -> Unit
) {
    val openEnableBiometryWithPinDialog = remember { mutableStateOf(false) }
    val openEnableBiometryWithPasswordDialog = remember { mutableStateOf(false) }
    val openBiometryWithOTPDialog = remember { mutableStateOf(false) }
    val openSetPinWithOTPDialog = remember { mutableStateOf(false) }
    val openSetPinWithBiometryDialog = remember { mutableStateOf(false) }
    val openSetPinWithPasswordDialog = remember { mutableStateOf(false) }

    var passwordInput by remember { mutableStateOf("") }
    var pinInput by remember { mutableStateOf("") }
    var otpInput by remember { mutableStateOf("") }
    var textFieldInputVisibility by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        // userEmailDisplay row:
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "User email:",
                fontSize = 16.sp,
                modifier = Modifier
                    .padding(8.dp)
            )
            Text(
                text = userInfo.value,
                fontSize = 16.sp,
                modifier = Modifier
                    .padding(8.dp)
            )
        }

        Divider(
            color = Color.Black,
            thickness = 3.dp
        )
        // biometry status row:
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "biometry status: ",
                Modifier.padding(8.dp)
            )

            Text(
                text = if (biometryStatus.value) "biometry is enabled" else "biometry is disabled",
                Modifier.padding(8.dp)
            )
        }

        Divider(
            color = Color.Black,
            thickness = 1.dp,
            modifier = Modifier.padding(8.dp, 0.dp)
        )
        // isPinSetStatus row:
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "pin status:",
                Modifier.padding(8.dp)
            )

            Text(
                text = if (isPinSet.value) "pin is set" else "pin is not set",
                Modifier.padding(8.dp)
            )
        }

        Divider(
            color = Color.Black,
            thickness = 1.dp,
            modifier = Modifier.padding(8.dp, 0.dp)
        )
        // isPasswordSet row:
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "password status:",
                Modifier.padding(8.dp)
            )
            Text(
                text = if (isPasswordSet.value) "password is set" else "password is not set",
                Modifier.padding(8.dp)
            )
        }

        Divider(
            color = Color.Black,
            thickness = 1.dp,
            modifier = Modifier.padding(8.dp, 0.dp)
        )
        // isPinOrPasswordSet row:
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "pinOrPasswordStatus:",
                Modifier.padding(8.dp)
            )

            Text(
                text = isPinOrPasswordSet.value,
                Modifier.padding(8.dp)
            )
        }

        Divider(
            color = Color.Black,
            thickness = 3.dp
        )

        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())

        ) {
            CustomButton(text = "Enable biometry with pin") {
                openEnableBiometryWithPinDialog.value = true
            }

            if (openEnableBiometryWithPinDialog.value) {
                AlertDialog(
                    onDismissRequest = {
                        openEnableBiometryWithPinDialog.value = false
                    },
                    title = {
                        Text(
                            text = "Enabling biometric authentication!",
                            fontWeight = FontWeight.Bold
                        )
                    },
                    text = {
                        Column {
                            Text(
                                text = "To enable biometric authentication, you need to enter your PIN:",
                                modifier = Modifier
                                    .padding(0.dp, 4.dp)
                            )
                            TextField(
                                modifier = Modifier
                                    .padding(0.dp, 4.dp),
                                value = pinInput,
                                onValueChange = { pinInput = it },
                                label = { Text("Pin") },
                                visualTransformation = if (textFieldInputVisibility) VisualTransformation.None else PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                trailingIcon = {
                                    val image = if (textFieldInputVisibility) {
                                        Icons.Filled.Visibility
                                    } else {
                                        Icons.Filled.VisibilityOff
                                    }

                                    IconButton(onClick = {
                                        textFieldInputVisibility = !textFieldInputVisibility
                                    }) {
                                        Icon(imageVector = image, "")
                                    }
                                }
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                enableBioAutByPINAction(pinInput)
                            }
                        ) {
                            Text("Enable")
                        }
                    },
                    dismissButton = {
                        Button(
                            onClick = {
                                openEnableBiometryWithPinDialog.value = false
                            }
                        ) {
                            Text("Cancel")
                        }
                    }
                )
            }
            CustomButton(text = "Enable biometry with password") {
                openEnableBiometryWithPasswordDialog.value = true
            }

            if (openEnableBiometryWithPasswordDialog.value) {
                AlertDialog(
                    onDismissRequest = {
                        openEnableBiometryWithPasswordDialog.value = false
                    },
                    title = {
                        Text(
                            text = "Enabling biometric authentication!",
                            fontWeight = FontWeight.Bold
                        )
                    },
                    text = {
                        Column {
                            Text(
                                text = "To enable biometric authentication, you need to enter your password:",
                                modifier = Modifier
                                    .padding(0.dp, 4.dp)
                            )
                            TextField(
                                modifier = Modifier
                                    .padding(0.dp, 4.dp),
                                value = passwordInput,
                                onValueChange = { passwordInput = it },
                                label = { Text("Password") },
                                visualTransformation = if (textFieldInputVisibility) VisualTransformation.None else PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                trailingIcon = {
                                    val image = if (textFieldInputVisibility) {
                                        Icons.Filled.Visibility
                                    } else {
                                        Icons.Filled.VisibilityOff
                                    }

                                    IconButton(onClick = {
                                        textFieldInputVisibility = !textFieldInputVisibility
                                    }) {
                                        Icon(imageVector = image, "")
                                    }
                                }
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = { enableBioAutByPasswordAction(passwordInput) }
                        ) {
                            Text("Enable")
                        }
                    },
                    dismissButton = {
                        Button(
                            onClick = {
                                openEnableBiometryWithPasswordDialog.value = false
                            }
                        ) {
                            Text("Cancel")
                        }
                    }
                )
            }
            CustomButton(text = "Enable biometry with otp") {
                openBiometryWithOTPDialog.value = true
            }

            if (openBiometryWithOTPDialog.value) {
                AlertDialog(
                    onDismissRequest = {
                        openBiometryWithOTPDialog.value = false
                    },
                    title = {
                        Text(
                            text = "Enabling biometric authentication!",
                            fontWeight = FontWeight.Bold
                        )
                    },
                    text = {
                        Column {
                            Text(
                                text = "To enable biometric authentication by otp you need to enter the code that was sent to you by mail:",
                                modifier = Modifier
                                    .padding(0.dp, 4.dp)
                            )
                            TextField(
                                value = otpInput,
                                onValueChange = { otpInput = it },
                                label = { Text("OTP") },
                                modifier = Modifier
                                    .padding(0.dp, 4.dp)
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                enableBioAutByOTPAction(otpInput)
                            }
                        ) {
                            Text("Enable")
                        }
                    },
                    dismissButton = {
                        Button(
                            onClick = {
                                openBiometryWithOTPDialog.value = false
                            }
                        ) {
                            Text("Cancel")
                        }
                    }
                )
            }

            CustomButton(text = "Resend Mail OTP") {
                IDKit.sendMailOTP {
                    when (it) {
                        is Success -> Toast.makeText(activity, it.result.toString(), Toast.LENGTH_SHORT).show()
                        is Failure -> Toast.makeText(activity, it.toString(), Toast.LENGTH_SHORT).show()
                    }
                }
            }

            CustomButton(text = "Disable Biometry") {
                IDKit.disableBiometricAuthentication()
                biometryStatus.value = false
            }

            CustomButton(text = "Set pin with otp") {
                openSetPinWithOTPDialog.value = true
            }

            if (openSetPinWithOTPDialog.value) {
                AlertDialog(
                    onDismissRequest = {
                        openSetPinWithOTPDialog.value = false
                    },
                    title = {
                        Text(
                            text = "Setting a new pin by OTP!",
                            fontWeight = FontWeight.Bold
                        )
                    },
                    text = {
                        Column {
                            Text(
                                text = "Enter your desired pin to set:",
                                modifier = Modifier
                                    .padding(0.dp, 4.dp)
                            )
                            TextField(
                                modifier = Modifier
                                    .padding(0.dp, 4.dp),
                                value = pinInput,
                                onValueChange = { pinInput = it },
                                label = { Text("new Pin") },
                                visualTransformation = if (textFieldInputVisibility) VisualTransformation.None else PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                trailingIcon = {
                                    val image = if (textFieldInputVisibility) {
                                        Icons.Filled.Visibility
                                    } else {
                                        Icons.Filled.VisibilityOff
                                    }
                                    IconButton(onClick = {
                                        textFieldInputVisibility = !textFieldInputVisibility
                                    }) {
                                        Icon(imageVector = image, "")
                                    }
                                }
                            )
                            Text(
                                text = "Enter the otp send by email to allow setting the pin:",
                                modifier = Modifier
                                    .padding(0.dp, 4.dp)
                            )
                            TextField(
                                modifier = Modifier
                                    .padding(0.dp, 4.dp),
                                value = otpInput,
                                onValueChange = { otpInput = it },
                                label = { Text("OTP") }
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = { setPinWithOTPAction(pinInput, otpInput) }
                        ) {
                            Text("set Pin")
                        }
                    },
                    dismissButton = {
                        Button(
                            onClick = {
                                openSetPinWithOTPDialog.value = false
                            }
                        ) {
                            Text("Cancel")
                        }
                    }
                )
            }
            CustomButton(text = "Set pin with biometry") {
                openSetPinWithBiometryDialog.value = true
            }

            if (openSetPinWithBiometryDialog.value) {
                AlertDialog(
                    onDismissRequest = {
                        openSetPinWithBiometryDialog.value = false
                    },
                    title = {
                        Text(
                            text = "Set pin with biometry!",
                            fontWeight = FontWeight.Bold
                        )
                    },
                    text = {
                        Column {
                            Text(
                                text = "Enter your desired pin:",
                                modifier = Modifier
                                    .padding(0.dp, 4.dp)
                            )
                            TextField(
                                modifier = Modifier
                                    .padding(0.dp, 4.dp),
                                value = pinInput,
                                onValueChange = { pinInput = it },
                                label = { Text("new Pin") },
                                visualTransformation = if (textFieldInputVisibility) VisualTransformation.None else PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                trailingIcon = {
                                    val image = if (textFieldInputVisibility) {
                                        Icons.Filled.Visibility
                                    } else {
                                        Icons.Filled.VisibilityOff
                                    }

                                    IconButton(onClick = {
                                        textFieldInputVisibility = !textFieldInputVisibility
                                    }) {
                                        Icon(imageVector = image, "")
                                    }
                                }
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                setPinWithBiometryAction(pinInput)
                            }
                        ) {
                            Text("Set pin")
                        }
                    },
                    dismissButton = {
                        Button(
                            onClick = {
                                openSetPinWithBiometryDialog.value = false
                            }
                        ) {
                            Text("Cancel")
                        }
                    }
                )
            }
            CustomButton(text = "Set pin with password") {
                openSetPinWithPasswordDialog.value = true
            }

            if (openSetPinWithPasswordDialog.value) {
                AlertDialog(
                    onDismissRequest = {
                        openSetPinWithPasswordDialog.value = false
                    },
                    title = {
                        Text(
                            text = "Setting a new pin by password!",
                            fontWeight = FontWeight.Bold
                        )
                    },
                    text = {
                        Column {
                            Text("Enter your desired pin:")
                            TextField(
                                value = pinInput,
                                onValueChange = { pinInput = it },
                                label = { Text("new Pin") },
                                visualTransformation = if (textFieldInputVisibility) VisualTransformation.None else PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                trailingIcon = {
                                    val image = if (textFieldInputVisibility) {
                                        Icons.Filled.Visibility
                                    } else {
                                        Icons.Filled.VisibilityOff
                                    }

                                    IconButton(onClick = {
                                        textFieldInputVisibility = !textFieldInputVisibility
                                    }) {
                                        Icon(imageVector = image, "")
                                    }
                                }
                            )
                            Text("You need to enter your password:")
                            TextField(
                                value = passwordInput,
                                onValueChange = { passwordInput = it },
                                label = { Text("Password") },
                                visualTransformation = if (textFieldInputVisibility) VisualTransformation.None else PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                trailingIcon = {
                                    val image = if (textFieldInputVisibility) {
                                        Icons.Filled.Visibility
                                    } else {
                                        Icons.Filled.VisibilityOff
                                    }

                                    IconButton(onClick = {
                                        textFieldInputVisibility = !textFieldInputVisibility
                                    }) {
                                        Icon(imageVector = image, "")
                                    }
                                }
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = { setPinWithPasswordAction(pinInput, passwordInput) }
                        ) {
                            Text("Enable")
                        }
                    },
                    dismissButton = {
                        Button(
                            onClick = {
                                openSetPinWithPasswordDialog.value = false
                            }
                        ) {
                            Text("Cancel")
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun CustomButton(text: String, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        shape = ButtonCornerShape,
        modifier = Modifier
            .padding(16.dp)
            .height(50.dp)
            .width(300.dp),
        border = BorderStroke(1.dp, Color.Black)
    ) {
        Text(
            text = text
        )
    }
}
