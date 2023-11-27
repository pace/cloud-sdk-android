package car.pace.cofu.ui.onboarding.fueltype

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import car.pace.cofu.R
import car.pace.cofu.ui.component.RadioGroup
import car.pace.cofu.ui.onboarding.PageScaffold
import car.pace.cofu.ui.theme.AppTheme
import car.pace.cofu.ui.wallet.fueltype.FuelType

@Composable
fun FuelTypePage(
    onNext: (FuelType) -> Unit
) {
    val fuelTypes = remember { FuelType.values().associateWith { it.stringRes } }
    var selectedFuelType: FuelType? by remember { mutableStateOf(null) }
    val nextButtonEnabled = selectedFuelType != null

    PageScaffold(
        imageRes = R.drawable.ic_fuel,
        titleRes = R.string.ONBOARDING_FUEL_TYPE_TITLE,
        descriptionRes = R.string.ONBOARDING_FUEL_TYPE_DESCRIPTION,
        nextButtonTextRes = R.string.ONBOARDING_ACTIONS_NEXT,
        onNextButtonClick = { onNext(selectedFuelType ?: FuelType.DIESEL) },
        nextButtonEnabled = nextButtonEnabled
    ) {
        RadioGroup(
            items = fuelTypes,
            selectedItem = selectedFuelType,
            modifier = Modifier.padding(top = 40.dp)
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
