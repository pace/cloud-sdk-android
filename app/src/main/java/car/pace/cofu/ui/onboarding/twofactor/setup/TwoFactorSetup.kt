package car.pace.cofu.ui.onboarding.twofactor.setup

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import car.pace.cofu.ui.component.Description
import car.pace.cofu.ui.component.OtpInput
import car.pace.cofu.ui.component.PrimaryButton
import car.pace.cofu.ui.component.TextTopBar
import car.pace.cofu.ui.component.Title
import car.pace.cofu.ui.theme.AppTheme
import car.pace.cofu.util.UiState
import car.pace.cofu.util.extension.errorTextRes
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TwoFactorSetup(
    viewModel: TwoFactorSetupViewModel = hiltViewModel(),
    type: TwoFactorSetupType,
    onResult: (successful: Boolean) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    ModalBottomSheet(
        onDismissRequest = { onResult(false) },
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        tonalElevation = 0.dp,
        dragHandle = null
    ) {
        var index by remember(type) { mutableIntStateOf(0) }
        val page = type.pages[index]
        var uiState: UiState<Unit> by remember { mutableStateOf(UiState.Success(Unit)) }
        var pinInput by remember { mutableStateOf("") }
        var pinConfirmation by remember { mutableStateOf("") }
        var otpInput by remember { mutableStateOf("") }
        val value = when (page) {
            TwoFactorSetupPage.PIN_INPUT -> pinInput
            TwoFactorSetupPage.PIN_CONFIRMATION -> pinConfirmation
            TwoFactorSetupPage.OTP_INPUT -> otpInput
        }

        LaunchedEffect(page) {
            if (page == TwoFactorSetupPage.OTP_INPUT) {
                viewModel.sendMailOtp().onFailure {
                    uiState = UiState.Error(it)
                }
            }
        }

        fun changeStep(increase: Boolean) {
            val newIndex = index.let { if (increase) it + 1 else it - 1 }
            if (newIndex in type.pages.indices) {
                index = newIndex
                uiState = UiState.Success(Unit)
            } else {
                coroutineScope.launch {
                    sheetState.hide()
                }.invokeOnCompletion {
                    onResult(increase)
                }
            }
        }

        fun Result<Unit>.handleResult() {
            onSuccess {
                changeStep(true)
            }.onFailure {
                uiState = UiState.Error(it)
            }
        }

        TwoFactorSetupContent(
            page = page,
            value = value,
            uiState = uiState,
            onNavigateUp = {
                changeStep(false)
            },
            onValueChange = { newValue, isValid ->
                when (page) {
                    TwoFactorSetupPage.PIN_INPUT -> pinInput = newValue
                    TwoFactorSetupPage.PIN_CONFIRMATION -> pinConfirmation = newValue
                    TwoFactorSetupPage.OTP_INPUT -> otpInput = newValue
                }

                if (!isValid) {
                    // Reset the state when user enters (to remove the error state)
                    uiState = UiState.Success(Unit)
                }
            },
            onButtonClick = {
                when (page) {
                    TwoFactorSetupPage.PIN_INPUT -> {
                        viewModel.checkPin(pinInput).handleResult()
                    }

                    TwoFactorSetupPage.PIN_CONFIRMATION -> {
                        viewModel.confirmPin(pinInput, pinConfirmation).handleResult()
                    }

                    TwoFactorSetupPage.OTP_INPUT -> {
                        coroutineScope.launch {
                            uiState = UiState.Loading

                            if (type == TwoFactorSetupType.BIOMETRY) {
                                viewModel.setBiometryWithOtp(otpInput).handleResult()
                            } else {
                                viewModel.setPinWithOtp(pinInput, otpInput).handleResult()
                            }
                        }
                    }
                }
            }
        )
    }
}

@Composable
fun TwoFactorSetupContent(
    page: TwoFactorSetupPage,
    value: String,
    uiState: UiState<Unit>,
    onNavigateUp: () -> Unit,
    onValueChange: (newValue: String, isValid: Boolean) -> Unit,
    onButtonClick: () -> Unit
) {
    Column {
        val inputValid by remember(page, value) {
            mutableStateOf(value.filterNot { it.isWhitespace() }.length == page.cellsCount)
        }

        TextTopBar(onNavigateUp = onNavigateUp)

        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Title(
                text = stringResource(id = page.titleRes)
            )
            Description(
                text = stringResource(id = page.descriptionRes),
                modifier = Modifier.padding(top = 20.dp)
            )

            val modifier = Modifier.padding(top = 28.dp)
            when (page) {
                TwoFactorSetupPage.PIN_INPUT -> {
                    PinInputField(
                        value = value,
                        modifier = modifier,
                        isValueInvalid = uiState is UiState.Error,
                        enabled = uiState !is UiState.Loading,
                        onValueChange = onValueChange
                    )
                }

                TwoFactorSetupPage.PIN_CONFIRMATION -> {
                    PinConfirmationInputField(
                        value = value,
                        modifier = modifier,
                        isValueInvalid = uiState is UiState.Error,
                        enabled = uiState !is UiState.Loading,
                        onValueChange = onValueChange
                    )
                }

                TwoFactorSetupPage.OTP_INPUT -> {
                    OtpInputField(
                        value = value,
                        modifier = modifier,
                        isValueInvalid = uiState is UiState.Error,
                        enabled = uiState !is UiState.Loading,
                        onValueChange = onValueChange
                    )
                }
            }

            if (uiState is UiState.Error) {
                Text(
                    text = stringResource(id = uiState.throwable.errorTextRes()),
                    modifier = Modifier.padding(top = 12.dp),
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Normal,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            PrimaryButton(
                text = stringResource(id = page.buttonRes),
                modifier = Modifier.padding(top = 12.dp),
                enabled = inputValid && uiState !is UiState.Loading,
                loading = uiState is UiState.Loading,
                onClick = onButtonClick
            )
        }
    }
}

@Composable
fun PinInputField(
    value: String,
    modifier: Modifier = Modifier,
    isValueInvalid: Boolean = false,
    enabled: Boolean = true,
    onValueChange: (newValue: String, isValid: Boolean) -> Unit
) {
    OtpInput(
        value = value,
        cellsCount = TwoFactorSetupPage.PIN_INPUT.cellsCount,
        modifier = modifier,
        isValueInvalid = isValueInvalid,
        enabled = enabled,
        onValueChange = onValueChange
    )
}

@Composable
fun PinConfirmationInputField(
    value: String,
    modifier: Modifier = Modifier,
    isValueInvalid: Boolean = false,
    enabled: Boolean = true,
    onValueChange: (newValue: String, isValid: Boolean) -> Unit
) {
    OtpInput(
        value = value,
        cellsCount = TwoFactorSetupPage.PIN_CONFIRMATION.cellsCount,
        modifier = modifier,
        isValueInvalid = isValueInvalid,
        enabled = enabled,
        onValueChange = onValueChange
    )
}

@Composable
fun OtpInputField(
    value: String,
    modifier: Modifier = Modifier,
    isValueInvalid: Boolean = false,
    enabled: Boolean = true,
    onValueChange: (newValue: String, isValid: Boolean) -> Unit
) {
    OtpInput(
        value = value,
        cellsCount = TwoFactorSetupPage.OTP_INPUT.cellsCount,
        modifier = modifier,
        isValueInvalid = isValueInvalid,
        enabled = enabled,
        onValueChange = onValueChange
    )
}

@Preview
@Composable
fun TwoFactorSetupContentPreview() {
    AppTheme {
        TwoFactorSetupContent(
            page = TwoFactorSetupPage.PIN_INPUT,
            value = "123",
            uiState = UiState.Success(Unit),
            onNavigateUp = {},
            onValueChange = { _, _ -> },
            onButtonClick = {}
        )
    }
}
