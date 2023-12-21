package car.pace.cofu.ui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowForwardIos
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import car.pace.cofu.ui.theme.AppTheme

@Composable
fun DefaultListItem(
    modifier: Modifier = Modifier,
    icon: ImageVector?,
    title: String,
    description: String? = null,
    switchInfo: SwitchInfo? = null
) {
    Column(
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(end = 12.dp)
                        .size(20.dp),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            Column(
                modifier = Modifier
                    .padding(vertical = 16.dp)
                    .weight(1f)
            ) {
                Text(
                    text = title,
                    color = MaterialTheme.colorScheme.onPrimary,
                    textAlign = TextAlign.Start,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                    style = MaterialTheme.typography.titleSmall
                )

                if (description != null) {
                    Text(
                        text = description,
                        modifier = Modifier.padding(top = 2.dp),
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Start,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            if (switchInfo == null) {
                Icon(
                    imageVector = Icons.Outlined.ArrowForwardIos,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(start = 12.dp)
                        .size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            } else {
                Switch(
                    checked = switchInfo.checked,
                    onCheckedChange = switchInfo.onCheckChanged,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.background,
                        uncheckedThumbColor = MaterialTheme.colorScheme.background,
                        uncheckedTrackColor = MaterialTheme.colorScheme.onSurface,
                        uncheckedBorderColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            }
        }
        HorizontalDivider()
    }
}

data class SwitchInfo(val checked: Boolean, val onCheckChanged: ((Boolean) -> Unit)?)

@Preview
@Composable
fun DefaultListItemPreview() {
    AppTheme {
        DefaultListItem(
            icon = Icons.Outlined.ReceiptLong,
            title = "List item label"
        )
    }
}

@Preview
@Composable
fun DefaultListItemWithDescriptionPreview() {
    AppTheme {
        DefaultListItem(
            icon = Icons.Outlined.ReceiptLong,
            title = "List item label",
            description = "Explains what the item does"
        )
    }
}

@Preview
@Composable
fun DefaultCheckedSwitchListItemPreview() {
    AppTheme {
        DefaultListItem(
            icon = Icons.Outlined.ReceiptLong,
            title = "List item label",
            switchInfo = SwitchInfo(true) {}
        )
    }
}

@Preview
@Composable
fun DefaultUncheckedSwitchListItemPreview() {
    AppTheme {
        DefaultListItem(
            icon = Icons.Outlined.ReceiptLong,
            title = "List item label",
            switchInfo = SwitchInfo(false) {}
        )
    }
}
