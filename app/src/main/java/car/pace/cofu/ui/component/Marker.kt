package car.pace.cofu.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import car.pace.cofu.R
import car.pace.cofu.ui.theme.AppTheme
import car.pace.cofu.ui.theme.PrimaryButtonText
import car.pace.cofu.ui.theme.Shadow
import car.pace.cofu.util.extension.formatPrice
import cloud.pace.sdk.poikit.poi.Price

@Composable
fun GasStationMarker(
    name: String?,
    formattedPrice: String?,
    isClosed: Boolean
) {
    Box(
        modifier = Modifier.padding(10.dp)
    ) {
        val backgroundColor = if (isClosed) MaterialTheme.colorScheme.background.copy(alpha = 0.5f) else MaterialTheme.colorScheme.background
        val width = 100.dp
        val arrowWidth = 16.dp

        MarkerAnchor(
            modifier = Modifier.align(Alignment.BottomCenter),
            color = if (isClosed) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.primary
        )

        Column(
            modifier = Modifier
                .padding(bottom = 5.dp)
                .bubbleShape(
                    backgroundColor = backgroundColor,
                    shadowColor = Shadow,
                    shadowRadius = 8.dp,
                    cornerRadius = 8.dp,
                    arrowWidth = arrowWidth,
                    arrowHeight = 8.dp,
                    arrowOffset = width / 2 - arrowWidth / 2
                )
                .requiredWidth(width)
                .heightIn(max = 65.dp)
                .padding(horizontal = 6.5.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val showSecondRow = isClosed || formattedPrice != null

            Text(
                text = name ?: stringResource(id = R.string.gas_station_default_name),
                modifier = Modifier.padding(top = 7.dp, bottom = if (showSecondRow) 3.dp else 15.dp),
                color = if (isClosed) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onPrimary,
                textAlign = TextAlign.Center,
                overflow = TextOverflow.Ellipsis,
                maxLines = if (showSecondRow) 1 else 2,
                style = MaterialTheme.typography.bodySmall
            )

            if (isClosed) {
                ClosedLabel(
                    modifier = Modifier.padding(top = 4.dp, bottom = 18.dp)
                )
            } else if (formattedPrice != null) {
                Text(
                    text = formattedPrice,
                    modifier = Modifier.padding(bottom = 12.dp),
                    textAlign = TextAlign.Center,
                    lineHeight = 24.75.sp,
                    style = MaterialTheme.typography.headlineSmall
                )
            }
        }
    }
}

@Composable
fun ClosedLabel(
    modifier: Modifier = Modifier
) {
    Text(
        text = stringResource(id = R.string.closed_label),
        modifier = modifier
            .border(width = 1.dp, color = MaterialTheme.colorScheme.onSurface, shape = RoundedCornerShape(size = 8.dp))
            .background(color = MaterialTheme.colorScheme.background, shape = RoundedCornerShape(size = 8.dp))
            .padding(horizontal = 5.dp, vertical = 2.dp),
        color = MaterialTheme.colorScheme.onSurface,
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        lineHeight = 15.sp
    )
}

@Composable
fun MarkerAnchor(
    modifier: Modifier = Modifier,
    isClosed: Boolean = false,
    blurRadius: Dp = 9.dp,
    color: Color = MaterialTheme.colorScheme.primary
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (isClosed) {
            ClosedLabel(
                modifier = Modifier
                    .padding(bottom = 4.dp)
                    .dropShadow(color = Shadow)
            )
        }

        Box(
            modifier = Modifier
                .dropShadow(color = Shadow, radius = blurRadius, isRound = true)
                .size(15.dp)
                .background(color = MaterialTheme.colorScheme.primaryContainer, shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(11.dp)
                    .background(color = color, shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(3.dp)
                        .background(color = MaterialTheme.colorScheme.primaryContainer, shape = CircleShape)
                )
            }
        }
    }
}

@Composable
fun ClusterMarker(
    count: Int,
    modifier: Modifier = Modifier,
    blurRadius: Dp = 18.dp,
    color: Color = MaterialTheme.colorScheme.primary
) {
    Box(modifier = modifier) {
        Box(
            modifier = Modifier
                .dropShadow(color = Shadow, radius = blurRadius, isRound = true)
                .size(40.dp)
                .background(color = MaterialTheme.colorScheme.primaryContainer, shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .background(color = color, shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (count > 99) "99+" else count.toString(),
                    color = PrimaryButtonText,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFAED581)
@Composable
fun OpenGasStationMarkerPreview() {
    AppTheme {
        GasStationMarkerPreview()
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFAED581)
@Composable
fun ClosedGasStationMarkerPreview() {
    AppTheme {
        GasStationMarkerPreview(isClosed = true)
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFAED581)
@Composable
fun NoPriceGasStationMarkerPreview() {
    AppTheme {
        GasStationMarkerPreview(price = null)
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFAED581)
@Composable
fun TwoLinesGasStationMarkerPreview() {
    AppTheme {
        GasStationMarkerPreview(name = "Tankstelle extralang")
    }
}

@Composable
private fun GasStationMarkerPreview(
    name: String = "Gas what",
    price: Double? = 1.337,
    isClosed: Boolean = false
) {
    GasStationMarker(
        name = name,
        formattedPrice = price?.let { Price("diesel", "Diesel", it).formatPrice(priceFormat = "d.dds", currency = "EUR") },
        isClosed = isClosed
    )
}

@Preview
@Composable
fun ClosedLabelPreview() {
    AppTheme {
        ClosedLabel()
    }
}

@Preview
@Composable
fun MarkerAnchorPreview() {
    AppTheme {
        MarkerAnchor(modifier = Modifier.padding(10.dp))
    }
}

@Preview
@Composable
fun ClosedMarkerAnchorPreview() {
    AppTheme {
        MarkerAnchor(
            modifier = Modifier.padding(10.dp),
            isClosed = true
        )
    }
}

@Preview
@Composable
fun ClusterMarkerPreview() {
    AppTheme {
        ClusterMarker(
            count = 100,
            modifier = Modifier.padding(15.dp)
        )
    }
}
