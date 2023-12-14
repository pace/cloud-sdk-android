package car.pace.cofu.ui.component

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import car.pace.cofu.ui.theme.AppTheme
import com.composeuisuite.ohteepee.OhTeePeeInput
import com.composeuisuite.ohteepee.configuration.OhTeePeeCellConfiguration
import com.composeuisuite.ohteepee.configuration.OhTeePeeConfigurations

@Composable
fun OtpInput(
    value: String,
    cellsCount: Int,
    modifier: Modifier = Modifier,
    isValueInvalid: Boolean = false,
    enabled: Boolean = true,
    onValueChange: (newValue: String, isValid: Boolean) -> Unit
) {
    val defaultCellConfig = OhTeePeeCellConfiguration.withDefaults(
        shape = RoundedCornerShape(8.dp),
        borderColor = MaterialTheme.colorScheme.primary,
        textStyle = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Normal)
    )

    OhTeePeeInput(
        value = value,
        onValueChange = onValueChange, // Only validate if user filled all cells. Otherwise reset state to remove error.
        configurations = OhTeePeeConfigurations.withDefaults(
            cellsCount = cellsCount,
            emptyCellConfig = defaultCellConfig.copy(
                borderColor = MaterialTheme.colorScheme.onSurface
            ),
            activeCellConfig = defaultCellConfig,
            errorCellConfig = defaultCellConfig.copy(
                borderColor = MaterialTheme.colorScheme.error
            ),
            cellModifier = Modifier
                .padding(horizontal = 4.dp)
                .size(50.dp),
            cursorColor = if (isValueInvalid) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
        ),
        modifier = modifier,
        isValueInvalid = isValueInvalid,
        enabled = enabled
    )
}

@Preview
@Composable
fun OtpInputPreview() {
    AppTheme {
        OtpInput(
            value = "123",
            cellsCount = 4,
            onValueChange = { _, _ -> }
        )
    }
}
