package car.pace.cofu.ui.component

import androidx.annotation.StringRes
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import car.pace.cofu.ui.theme.AppTheme
import car.pace.cofu.ui.theme.Gray

const val DEFAULT_LIST_ITEM_CONTENT_TYPE = "DefaultListItem"

data class ListItem(
    val id: String,
    val icon: ImageVector,
    @StringRes val textRes: Int
)

@Composable
fun DefaultListItem(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    text: String
) {
    Column {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 15.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = Gray
            )
            Text(
                text = text,
                modifier = Modifier
                    .padding(start = 12.dp)
                    .weight(1f),
                textAlign = TextAlign.Start,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight(510)
                )
            )
            Icon(
                imageVector = Icons.Outlined.ArrowForwardIos,
                contentDescription = null,
                modifier = Modifier
                    .padding(start = 12.dp)
                    .size(16.dp),
                tint = Gray
            )
        }
        Divider()
    }
}

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
