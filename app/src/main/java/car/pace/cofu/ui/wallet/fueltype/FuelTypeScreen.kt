package car.pace.cofu.ui.wallet.fueltype

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import car.pace.cofu.R
import car.pace.cofu.ui.component.RadioGroup
import car.pace.cofu.ui.component.TextTopBar
import car.pace.cofu.ui.theme.AppTheme

@Composable
fun FuelTypeScreen(
    onNavigateUp: () -> Unit,
    viewModel: FuelTypeViewModel = hiltViewModel()
) {
    val selectedFuelTypeGroup by viewModel.fuelTypeGroup.collectAsStateWithLifecycle()

    FuelTypeScreenContent(
        selectedFuelTypeGroup = selectedFuelTypeGroup,
        onNavigateUp = onNavigateUp,
        onFuelTypeGroupClick = viewModel::setFuelTypeGroup
    )
}

@Composable
fun FuelTypeScreenContent(
    selectedFuelTypeGroup: FuelTypeGroup,
    onNavigateUp: () -> Unit,
    onFuelTypeGroupClick: (FuelTypeGroup) -> Unit
) {
    Column {
        val fuelTypeGroups = remember { FuelTypeGroup.entries.associateWith { it.stringRes } }

        TextTopBar(
            text = stringResource(id = R.string.wallet_fuel_type_selection_title),
            onNavigateUp = onNavigateUp
        )

        RadioGroup(
            items = fuelTypeGroups,
            selectedItem = selectedFuelTypeGroup,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            onClick = onFuelTypeGroupClick
        )
    }
}

@Preview
@Composable
fun FuelTypeScreenContentPreview() {
    AppTheme {
        FuelTypeScreenContent(
            selectedFuelTypeGroup = FuelTypeGroup.DIESEL,
            onNavigateUp = {},
            onFuelTypeGroupClick = {}
        )
    }
}
