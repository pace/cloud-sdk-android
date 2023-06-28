package cloud.pace.sdk.app.ui.components.listscreen

import android.location.Location
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cloud.pace.sdk.poikit.poi.Address
import cloud.pace.sdk.poikit.poi.Day
import cloud.pace.sdk.poikit.poi.GasStation
import cloud.pace.sdk.poikit.poi.OpeningHour
import cloud.pace.sdk.poikit.poi.OpeningHours
import cloud.pace.sdk.poikit.poi.OpeningRule
import cloud.pace.sdk.poikit.poi.Price
import cloud.pace.sdk.poikit.poi.toLocationPoint
import java.util.Date

/**
 * Template of items inside the gasStation list, to be shown on the listScreen
 */
@Composable
fun ListScreenListItem(gasStation: GasStation, location: Location?) {
    Row(
        modifier = Modifier
            .padding(top = 2.dp)
            .height(IntrinsicSize.Min)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        Column(
            modifier = Modifier
                .padding(vertical = 4.dp, horizontal = 14.dp)
                .weight(1f)
        ) {
            Text(
                text = gasStation.name.orEmpty(),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                overflow = TextOverflow.Visible,
                maxLines = 1,
                style = MaterialTheme.typography.body2
            )

            Text(
                text = getAddressText(gasStation.address).orEmpty(),
                overflow = TextOverflow.Visible,
                maxLines = 2,
                style = MaterialTheme.typography.body2
            )
            if (location != null) {
                Text(
                    text = gasStation.center?.let { getDistanceText(it.getDistanceInMetersTo(location.toLocationPoint())) }.orEmpty(),
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.End,
                    maxLines = 1,
                    modifier = Modifier.padding(vertical = 6.dp),
                    style = MaterialTheme.typography.body2
                )
            }
        }
        Column(
            modifier = Modifier
                .padding(vertical = 4.dp, horizontal = 14.dp)
                .weight(1f),
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = gasStation.prices.toString().replace(",", "\n").replace("[", "").replace("]", "").replace(" ", ""),
                textAlign = TextAlign.End,
                overflow = TextOverflow.Ellipsis,
                maxLines = 7,
                style = MaterialTheme.typography.body2
            )
        }
    }
}

fun getAddressText(address: Address?): String? {
    return if (address?.street == null) {
        ""
    } else {
        val addressFirstSubstring = if (address.houseNumber == null) address.street else "${address.street} ${address.houseNumber}"
        val addressSecondSubstring = if (address.city == null) "" else if (address.postalCode == null) address.city else "${address.postalCode} ${address.city}"
        if (addressSecondSubstring.isNullOrEmpty()) addressFirstSubstring else "$addressFirstSubstring\n$addressSecondSubstring"
    }
}

fun getDistanceText(distance: Double?): String {
    return distance?.let {
        if (it < 1000) {
            // Show in meter
            "${String.format("%.0f", it)} ${"m"}"
        } else {
            // Show in kilometer
            "${String.format("%.1f", it / 1000.0)} ${"km"}"
        }
    } ?: "--"
}

@Preview
@Composable
fun ListItemPreview() {
    ListScreenListItem(
        GasStation("poiId", arrayListOf()).apply {
            id = "poiId"
            name = "HEM Tankstelle am Südend"
            address = Address("c=DE;l=Karlsruhe;pc=76131;s=Haid-und-Neu-Straße;hn=18")
            openingHours = listOf(
                OpeningHours(listOf(Day.FRIDAY, Day.MONDAY, Day.THURSDAY, Day.TUESDAY, Day.WEDNESDAY), listOf(OpeningHour("8", "22")), OpeningRule.OPEN),
                OpeningHours(listOf(Day.SUNDAY), listOf(OpeningHour("0", "0")), OpeningRule.CLOSED),
                OpeningHours(listOf(Day.SATURDAY), listOf(OpeningHour("10", "20")), OpeningRule.OPEN)
            )
            prices = mutableListOf(
                Price("ron95e5", "Super", 1.389),
                Price("ron95e10", "Super E10", 1.349),
                Price("diesel", "Diesel", 1.289)
            )
            currency = "EUR"
            isConnectedFuelingAvailable = true
            cofuPaymentMethods = mutableListOf("paypal")
            paymentMethods = mutableListOf(
                "americanExpress",
                "cash",
                "essoCard",
                "girocard",
                "maestro",
                "masterCard",
                "uta",
                "visa"
            )
            amenities = mutableListOf(
                "atm",
                "disabilityFriendly",
                "toilet"
            )
            foods = mutableListOf(
                "bakery"
            )
            services = mutableListOf(
                "carWash",
                "freeWifi",
                "tyreAir"
            )
            updatedAt = Date(1575651009687)
            latitude = 82.0
            longitude = 73.0
        },
        Location("").apply {
            latitude = 43.0
            longitude = 8.0
        }
    )
}
