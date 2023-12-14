package car.pace.cofu.ui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowForwardIos
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material3.Divider
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
    text: String,
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
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = text,
                modifier = Modifier
                    .padding(vertical = 16.dp)
                    .weight(1f),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Start,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
                style = MaterialTheme.typography.titleSmall
            )

            if (switchInfo == null) {
                Icon(
                    imageVector = Icons.Outlined.ArrowForwardIos,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(start = 12.dp)
                        .size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Switch(
                    checked = switchInfo.checked,
                    onCheckedChange = switchInfo.onCheckChanged,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.surface,
                        checkedTrackColor = MaterialTheme.colorScheme.primary,
                        uncheckedThumbColor = MaterialTheme.colorScheme.secondary,
                        uncheckedTrackColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        }
        Divider()
    }
}

data class SwitchInfo(val checked: Boolean, val onCheckChanged: ((Boolean) -> Unit)?)

@Preview
@Composable
fun DefaultListItemPreview() {
    AppTheme {
        DefaultListItem(
            icon = Icons.Outlined.ReceiptLong,
            text = "List item label"
        )
    }
}

@Preview
@Composable
fun DefaultSwitchListItemPreview() {
    AppTheme {
        DefaultListItem(
            icon = Icons.Outlined.ReceiptLong,
            text = "List item label",
            switchInfo = SwitchInfo(true) {}
        )
    }
}
