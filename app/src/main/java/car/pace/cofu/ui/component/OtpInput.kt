package car.pace.cofu.ui.component

import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import car.pace.cofu.ui.onboarding.twofactor.setup.TwoFactorSetupPage
import car.pace.cofu.ui.theme.AppTheme

@Composable
fun OtpInput(
    value: String,
    cellsCount: Int,
    modifier: Modifier = Modifier,
    isValueInvalid: Boolean = false,
    enabled: Boolean = true,
    onValueChange: (newValue: String, isValid: Boolean) -> Unit,
    onConfirm: () -> Unit
) {
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    LaunchedEffect(isValueInvalid, value) {
        if (isValueInvalid && value.isEmpty()) {
            focusRequester.requestFocus()
        }
    }

    BasicTextField(
        modifier = modifier.focusRequester(focusRequester),
        value = TextFieldValue(value, selection = TextRange(cellsCount)),
        enabled = enabled,
        onValueChange = {
            if (it.text.length <= cellsCount) {
                onValueChange.invoke(it.text, it.text.length == cellsCount)
            }
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.NumberPassword,
            imeAction = ImeAction.Done,
            showKeyboardOnFocus = true
        ),
        keyboardActions = KeyboardActions(
            onDone = {
                if (value.length == cellsCount) {
                    onConfirm()
                }
            }
        ),
        decorationBox = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
            ) {
                val useFixedSize = cellsCount == TwoFactorSetupPage.PIN_INPUT.cellsCount
                val modifier = if (useFixedSize) Modifier.size(50.dp) else Modifier.weight(1f).aspectRatio(1f)

                for (i in 0..cellsCount - 1) {
                    NumberInputField(
                        index = i,
                        text = value,
                        cellsCount = cellsCount,
                        isValueInvalid = isValueInvalid,
                        modifier = modifier
                    )
                }
            }
        }
    )
}

@Composable
private fun NumberInputField(
    index: Int,
    text: String,
    cellsCount: Int,
    modifier: Modifier = Modifier,
    isValueInvalid: Boolean = false
) {
    val isFocused = text.length == index || (text.length == cellsCount && index == cellsCount - 1)
    val char = text.getOrNull(index)

    val borderColor = when {
        isValueInvalid && text.isEmpty() -> MaterialTheme.colorScheme.error
        isFocused -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.onSurface
    }

    Box(
        modifier = modifier
            .border(BorderStroke(1.dp, borderColor), RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (char != null) {
                Text(
                    text = char.toString(),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Normal),
                    textAlign = TextAlign.Center
                )
            }

            if (isFocused) {
                Cursor(color = if (isValueInvalid) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
private fun Cursor(color: Color, modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "cursorTransition")
    val cursorAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000)
        )
    )
    Box(
        modifier = modifier
            .width(2.dp)
            .height(22.dp)
            .alpha(cursorAlpha)
            .background(color)
    )
}

@Preview
@Composable
fun OtpInputPreview() {
    AppTheme {
        OtpInput(
            value = "123",
            cellsCount = 4,
            onValueChange = { _, _ -> },
            onConfirm = {}
        )
    }
}
