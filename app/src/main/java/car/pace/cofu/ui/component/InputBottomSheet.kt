package car.pace.cofu.ui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import car.pace.cofu.ui.theme.AppTheme

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun InputBottomSheet(
    title: String,
    description: String,
    buttonText: String,
    errorText: String? = null,
    loading: Boolean = false,
    onDismissRequest: (input: String?) -> Unit
) {
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = { onDismissRequest(null) },
        sheetState = sheetState,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 35.dp, top = 5.dp, end = 35.dp, bottom = 30.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            var input by rememberSaveable(title) { mutableStateOf("") }
            val keyboardController = LocalSoftwareKeyboardController.current

            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = description,
                modifier = Modifier.padding(top = 14.dp),
                style = MaterialTheme.typography.bodyLarge
            )
            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp),
                label = {
                    Text(text = errorText ?: title)
                },
                isError = errorText != null,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.NumberPassword,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        keyboardController?.hide()
                        onDismissRequest(input)
                    }
                )
            )
            DefaultButton(
                text = buttonText,
                modifier = Modifier.padding(top = 25.dp),
                enabled = !loading,
                onClick = { onDismissRequest(input) }
            )

            if (loading) {
                DefaultLinearProgressIndicator(
                    modifier = Modifier.padding(top = 20.dp)
                )
            }
        }
    }
}

@Preview
@Composable
fun InputBottomSheetPreview() {
    AppTheme {
        InputBottomSheet(
            title = "Bottom sheet title",
            description = "Bottom sheet description",
            buttonText = "Button text",
            onDismissRequest = {}
        )
    }
}
