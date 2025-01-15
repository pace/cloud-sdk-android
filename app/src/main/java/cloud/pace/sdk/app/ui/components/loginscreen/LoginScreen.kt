package cloud.pace.sdk.app.ui.components.loginscreen

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.primarySurface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import cloud.pace.sdk.app.R
import cloud.pace.sdk.app.ui.components.NoSupportedBrowserDialog

@Composable
fun ShowLoginScreen(showDialog: Boolean, onDialogDismiss: () -> Unit, openLogin: () -> Unit) {
    Scaffold(
        modifier = Modifier
            .background(MaterialTheme.colors.primarySurface)
            .safeDrawingPadding(),
        topBar = {
            TopAppBar(
                title = {
                    Text(text = stringResource(id = R.string.app_name))
                },
                navigationIcon = {
                    Icon(
                        painter = painterResource(id = R.drawable.pace_logo),
                        contentDescription = null,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            )
        },
        content = {
            ConstraintLayout(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(it)
            ) {
                val (infoText, loginButton, termsText, termsAndPrivacyDivider, privacyText) = createRefs()
                val context = LocalContext.current
                Text(
                    text = stringResource(id = R.string.short_login_screen_information),
                    modifier = Modifier
                        .padding(40.dp)
                        .constrainAs(infoText) {
                            top.linkTo(parent.top)
                        }
                )

                LoginButton(
                    modifier = Modifier.constrainAs(loginButton) {
                        top.linkTo(infoText.bottom)
                        bottom.linkTo(termsAndPrivacyDivider.top)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    },
                    openLogin = openLogin
                )

                Text(
                    text = "Terms",
                    color = Color(0, 102, 204),
                    modifier = Modifier
                        .padding(75.dp, 25.dp)
                        .constrainAs(termsText) {
                            bottom.linkTo(parent.bottom)
                            start.linkTo(parent.start)
                        }
                        .clickable(
                            enabled = true,
                            role = Role.Button
                        ) {
                            Toast
                                .makeText(context, "Clicked on terms", Toast.LENGTH_SHORT)
                                .show()
                        }
                )

                Canvas(
                    modifier = Modifier
                        .padding(0.dp, 32.dp)
                        .constrainAs(termsAndPrivacyDivider) {
                            start.linkTo(termsText.end)
                            end.linkTo(privacyText.start)
                            bottom.linkTo(parent.bottom)
                        }
                ) {
                    drawCircle(
                        color = Color.Black,
                        radius = 6f
                    )
                }

                Text(
                    text = "Privacy",
                    color = Color(0, 102, 204),
                    modifier = Modifier
                        .padding(75.dp, 25.dp)
                        .constrainAs(privacyText) {
                            bottom.linkTo(parent.bottom)
                            end.linkTo(parent.end)
                        }
                        .clickable(
                            enabled = true,
                            role = Role.Button
                        ) {}
                )

                if (showDialog) {
                    NoSupportedBrowserDialog(onDismiss = onDialogDismiss)
                }
            }
        }
    )
}

@Composable
fun LoginButton(modifier: Modifier, openLogin: () -> Unit) {
    OutlinedButton(
        modifier = modifier
            .padding(25.dp)
            .fillMaxWidth(),
        border = BorderStroke(1.dp, Color.Black),
        onClick = {
            openLogin()
        },
        shape = RoundedCornerShape(40)
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_baseline_login_24),
            contentDescription = "",
            modifier = Modifier
                .size(60.dp)
                .padding(10.dp)
        )
        Text(
            text = "Login",
            fontSize = 30.sp,
            modifier = Modifier.padding(10.dp)
        )
    }
}
