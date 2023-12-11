package car.pace.cofu.ui.wallet.fueltype

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import car.pace.cofu.ui.component.RadioGroup
import car.pace.cofu.ui.theme.AppTheme

@Composable
fun FuelTypeScreen(
    viewModel: FuelTypeViewModel = hiltViewModel()
) {
    val selectedFuelTypeGroup by viewModel.fuelTypeGroup.collectAsStateWithLifecycle()

    FuelTypeScreenContent(
        selectedFuelTypeGroup = selectedFuelTypeGroup,
        onFuelTypeGroupClick = viewModel::setFuelTypeGroup
    )
}

@Composable
fun FuelTypeScreenContent(
    selectedFuelTypeGroup: FuelTypeGroup,
    onFuelTypeGroupClick: (FuelTypeGroup) -> Unit
) {
    val fuelTypeGroups = remember { FuelTypeGroup.values().associateWith { it.stringRes } }

    RadioGroup(
        items = fuelTypeGroups,
        selectedItem = selectedFuelTypeGroup,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        onClick = onFuelTypeGroupClick
    )
}

@Preview
@Composable
fun FuelTypeScreenContentPreview() {
    AppTheme {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            FuelTypeScreenContent(
                selectedFuelTypeGroup = FuelTypeGroup.DIESEL,
                onFuelTypeGroupClick = {}
            )
        }
    }
}
