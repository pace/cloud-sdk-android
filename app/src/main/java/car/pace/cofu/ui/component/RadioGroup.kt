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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import car.pace.cofu.ui.theme.AppTheme
import car.pace.cofu.ui.wallet.fueltype.FuelTypeGroup

@Composable
fun <T> RadioGroup(
    items: Map<T, Int>,
    selectedItem: T?,
    modifier: Modifier = Modifier,
    onClick: (T) -> Unit
) {
    Column(
        modifier = modifier.selectableGroup(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items.forEach { (id, textRes) ->
            RadioButton(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(id = textRes),
                selected = id == selectedItem,
                onClick = { onClick(id) }
            )
        }
    }
}

@Composable
fun RadioButton(
    modifier: Modifier = Modifier,
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier
            .selectable(
                selected = selected,
                role = Role.RadioButton,
                onClick = onClick
            )
            .background(color = MaterialTheme.colorScheme.secondary, shape = RoundedCornerShape(8.dp))
            .padding(horizontal = 20.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            color = MaterialTheme.colorScheme.onPrimary,
            style = MaterialTheme.typography.titleSmall
        )
        Box(
            modifier = Modifier
                .size(30.dp)
                .background(color = MaterialTheme.colorScheme.primaryContainer, shape = CircleShape)
                .border(width = 1.dp, color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant, shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (selected) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .background(color = MaterialTheme.colorScheme.primary, shape = CircleShape)
                )
            }
        }
    }
}

@Preview
@Composable
fun RadioGroupPreview() {
    AppTheme {
        val fuelTypeGroups = remember { FuelTypeGroup.values().associateWith { it.stringRes } }
        var selectedFuelTypeGroup: FuelTypeGroup? by remember { mutableStateOf(null) }

        RadioGroup(
            items = fuelTypeGroups,
            selectedItem = selectedFuelTypeGroup
        ) {
            selectedFuelTypeGroup = it
        }
    }
}

@Preview
@Composable
fun RadioButtonPreview() {
    AppTheme {
        RadioButton(
            modifier = Modifier.fillMaxWidth(),
            text = "Diesel",
            selected = true,
            onClick = {}
        )
    }
}
