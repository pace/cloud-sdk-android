package cloud.pace.sdk.app.view.mainscreen.listscreen

import android.location.Location
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cloud.pace.sdk.poikit.poi.*
import java.util.*

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
                .weight(1f),
        ) {
            Text(
                text = gasStation.name.orEmpty(),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                overflow = TextOverflow.Visible,
                maxLines = 1,
            )

            Text(
                text = getAddressText(gasStation.address).orEmpty(),
                overflow = TextOverflow.Visible,
                maxLines = 2,
            )
            if (location != null) {
                Text(
                    text = gasStation.center?.let { getDistanceText(it.getDistanceInMetersTo(location.toLocationPoint())) }.orEmpty(),
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.End,
                    maxLines = 1,
                    modifier = Modifier.padding(vertical = 6.dp)
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
    ListScreenListItem(GasStation("poiId", arrayListOf()).apply {
        id = "poiId"
        name = "HEM Tankstelle am Südend"
        address = Address("c=DE;l=Karlsruhe;pc=76131;s=Haid-und-Neu-Straße;hn=18")
        openingHours = listOf(
            OpeningHours(listOf(Day.FRIDAY, Day.MONDAY, Day.THURSDAY, Day.TUESDAY, Day.WEDNESDAY), listOf(OpeningHour("8", "22")), OpeningRule.OPEN),
            OpeningHours(listOf(Day.SUNDAY), listOf(OpeningHour("0", "0")), OpeningRule.CLOSED),
            OpeningHours(listOf(Day.SATURDAY), listOf(OpeningHour("10", "20")), OpeningRule.OPEN)
        )
        prices = mutableListOf(
            Price(FuelType.E5, "Super", 1.389),
            Price(FuelType.E10, "Super E10", 1.349),
            Price(FuelType.DIESEL, "Diesel", 1.289)
        )
        currency = "EUR"
        isConnectedFuelingAvailable = true
        cofuPaymentMethods = mutableListOf("paypal")
        paymentMethods = mutableListOf(
            PaymentMethod.AMERICAN_EXPRESS,
            PaymentMethod.CASH,
            PaymentMethod.ESSO_CARD,
            PaymentMethod.GIROCARD,
            PaymentMethod.MAESTRO,
            PaymentMethod.MASTER_CARD,
            PaymentMethod.UTA,
            PaymentMethod.VISA
        )
        amenities = mutableListOf(
            Amenity.ATM,
            Amenity.DISABILITY_FRIENDLY,
            Amenity.TOILET
        )
        foods = mutableListOf(
            Food.BAKERY
        )
        services = mutableListOf(
            Service.CAR_WASH,
            Service.FREE_WIFI,
            Service.TYRE_AIR
        )
        updatedAt = Date(1575651009687)
        latitude = 82.0
        longitude = 73.0
        priceSuggestion = PriceSuggestion.HIGH
    },
        Location("").apply {
            latitude = 43.0
            longitude = 8.0
        }
    )
}







