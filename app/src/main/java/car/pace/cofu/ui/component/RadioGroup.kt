package car.pace.cofu.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import car.pace.cofu.R
import car.pace.cofu.ui.fueltype.FuelType
import car.pace.cofu.ui.theme.AppTheme

@Composable
fun <T> RadioGroup(
    items: Map<T, Int>,
    selectedItem: T?,
    modifier: Modifier = Modifier,
    onItemSelected: (T) -> Unit
) {
    Column(
        modifier = modifier.selectableGroup(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items.forEach { (identifier, textRes) ->
            val selected = identifier == selectedItem
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = selected,
                        role = Role.RadioButton,
                        onClick = { onItemSelected(identifier) },
                    )
                    .border(
                        width = 2.dp,
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 20.dp, vertical = 17.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val size = 25.dp
                if (selected) {
                    Box(
                        modifier = Modifier
                            .size(size)
                            .background(color = MaterialTheme.colorScheme.primary, shape = CircleShape)
                            .padding(3.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_check),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .size(size)
                            .border(
                                width = 2.dp,
                                color = MaterialTheme.colorScheme.primary,
                                shape = CircleShape
                            )
                    )
                }

                Text(
                    text = stringResource(id = textRes).uppercase(),
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = size), // Center it over the entire row
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }
    }
}

@Preview
@Composable
fun RadioGroupPreview() {
    AppTheme {
        val fuelTypes = remember { FuelType.values().associateWith { it.stringRes } }
        var selectedFuelType: FuelType? by remember { mutableStateOf(null) }

        RadioGroup(
            items = fuelTypes,
            selectedItem = selectedFuelType
        ) {
            selectedFuelType = it
        }
    }
}
