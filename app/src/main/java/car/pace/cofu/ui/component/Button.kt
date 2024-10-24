package car.pace.cofu.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import car.pace.cofu.R
import car.pace.cofu.ui.theme.AppTheme
import car.pace.cofu.ui.theme.PrimaryButtonText
import car.pace.cofu.ui.theme.appRippleConfiguration

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrimaryButton(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    onClick: () -> Unit
) {
    CompositionLocalProvider(LocalRippleConfiguration provides appRippleConfiguration(PrimaryButtonText)) {
        Button(
            onClick = onClick,
            modifier = modifier
                .fillMaxWidth()
                .heightIn(min = 50.dp),
            enabled = enabled,
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(
                contentColor = PrimaryButtonText,
                disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                disabledContentColor = PrimaryButtonText.copy(alpha = 0.2f)
            )
        ) {
            ButtonContent(
                text = text,
                loading = loading,
                loadingColor = PrimaryButtonText
            )
        }
    }
}

@Composable
fun SecondaryButton(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 50.dp),
        enabled = enabled,
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = MaterialTheme.colorScheme.secondary,
            disabledContentColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
        ),
        border = BorderStroke(
            width = 1.dp,
            color = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
        )
    ) {
        ButtonContent(
            text = text,
            loading = loading
        )
    }
}

@Composable
fun ButtonContent(
    text: String,
    loading: Boolean,
    loadingColor: Color = MaterialTheme.colorScheme.onPrimary
) {
    if (loading) {
        DefaultCircularProgressIndicator(
            modifier = Modifier.size(30.dp),
            color = loadingColor
        )
    } else {
        Text(
            text = text,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.labelLarge
        )
    }
}

@Composable
fun DefaultTextButton(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    TextButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        colors = ButtonDefaults.textButtonColors(
            contentColor = MaterialTheme.colorScheme.primary,
            disabledContentColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
        ),
        contentPadding = PaddingValues(8.dp)
    ) {
        Text(
            text = text,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.labelLarge
        )
    }
}

@Preview
@Composable
fun PrimaryButtonPreview() {
    AppTheme {
        PrimaryButton(
            text = "Start fueling",
            onClick = {}
        )
    }
}

@Preview
@Composable
fun DisabledPrimaryButtonPreview() {
    AppTheme {
        PrimaryButton(
            text = "Start fueling",
            enabled = false,
            onClick = {}
        )
    }
}

@Preview
@Composable
fun LoadingPrimaryButtonPreview() {
    AppTheme {
        PrimaryButton(
            text = "Start fueling",
            loading = true,
            onClick = {}
        )
    }
}

@Preview
@Composable
fun SecondaryButtonPreview() {
    AppTheme {
        SecondaryButton(
            text = "Start navigation",
            onClick = {}
        )
    }
}

@Preview
@Composable
fun DisabledSecondaryButtonPreview() {
    AppTheme {
        SecondaryButton(
            text = "Start navigation",
            enabled = false,
            onClick = {}
        )
    }
}

@Preview
@Composable
fun LoadingecondaryButtonPreview() {
    AppTheme {
        SecondaryButton(
            text = "Start navigation",
            loading = true,
            onClick = {}
        )
    }
}

@Preview
@Composable
fun DefaultTextButtonPreview() {
    AppTheme {
        DefaultTextButton(
            text = stringResource(id = R.string.onboarding_create_pin_title),
            onClick = {}
        )
    }
}

@Preview
@Composable
fun DisabledDefaultTextButtonPreview() {
    AppTheme {
        DefaultTextButton(
            text = stringResource(id = R.string.onboarding_create_pin_title),
            enabled = false,
            onClick = {}
        )
    }
}
