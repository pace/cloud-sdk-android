package car.pace.cofu.ui.onboarding.fueltype

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocalGasStation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import car.pace.cofu.R
import car.pace.cofu.ui.component.Description
import car.pace.cofu.ui.component.RadioGroup
import car.pace.cofu.ui.onboarding.PageScaffold
import car.pace.cofu.ui.theme.AppTheme
import car.pace.cofu.ui.wallet.fueltype.FuelTypeGroup

@Composable
fun FuelTypePage(
    onNext: (FuelTypeGroup) -> Unit
) {
    val fuelTypeGroups = remember { FuelTypeGroup.entries.associateWith { it.stringRes } }
    var selectedFuelType: FuelTypeGroup? by remember { mutableStateOf(null) }
    val nextButtonEnabled = selectedFuelType != null

    PageScaffold(
        imageVector = Icons.Outlined.LocalGasStation,
        titleRes = R.string.onboarding_fuel_type_title,
        nextButtonTextRes = R.string.common_use_next,
        onNextButtonClick = { onNext(selectedFuelType ?: FuelTypeGroup.DIESEL) },
        nextButtonEnabled = nextButtonEnabled,
        descriptionContent = {
            Description(
                text = stringResource(id = R.string.onboarding_fuel_type_description)
            )
        }
    ) {
        RadioGroup(
            items = fuelTypeGroups,
            selectedItem = selectedFuelType,
            modifier = Modifier
                .padding(top = 28.dp)
                .fillMaxWidth(0.5f)
        ) {
            selectedFuelType = it
        }
    }
}

@Preview
@Composable
fun FuelTypePagePreview() {
    AppTheme {
        FuelTypePage {}
    }
}
