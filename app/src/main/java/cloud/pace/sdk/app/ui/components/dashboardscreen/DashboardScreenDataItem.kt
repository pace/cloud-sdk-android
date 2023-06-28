package cloud.pace.sdk.app.ui.components.dashboardscreen

import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import cloud.pace.sdk.api.pay.generated.model.ReadOnlyLocation
import cloud.pace.sdk.api.pay.generated.model.Transaction
import java.util.Date

@Composable
fun DashboardDataItemTemplate(transaction: Transaction) {
    ConstraintLayout {
        val (gasStationName, gasStationLocation, transactionDate, purchasedFuelData) = createRefs()
        Text(
            text = transaction.location?.brand.orEmpty(),
            modifier = Modifier.constrainAs(gasStationName) {
                top.linkTo(parent.top)
                start.linkTo(parent.start, margin = 20.dp)
            },
            style = MaterialTheme.typography.body2
        )

        Text(
            text = transaction.location?.address?.toReadableText().orEmpty(),
            modifier = Modifier.constrainAs(gasStationLocation) {
                top.linkTo(gasStationName.bottom, margin = 14.dp)
                start.linkTo(parent.start, margin = 20.dp)
            },
            style = MaterialTheme.typography.body2
        )

        Text(
            text = transaction.createdAt?.toString()!!,
            modifier = Modifier.constrainAs(transactionDate) {
                absoluteRight.linkTo(parent.end, margin = 20.dp)
                top.linkTo(parent.top)
                absoluteLeft.linkTo(gasStationLocation.end, margin = 50.dp)
            },
            style = MaterialTheme.typography.body2
        )

        Text(
            text = transaction.priceIncludingVAT?.toString()!! + " " + transaction.currency,
            modifier = Modifier.constrainAs(purchasedFuelData) {
                absoluteRight.linkTo(parent.end, margin = 20.dp)
                top.linkTo(transactionDate.bottom, margin = 14.dp)
            },
            style = MaterialTheme.typography.body2
        )
    }
}

fun ReadOnlyLocation.Address.toReadableText(): String? {
    return if (street == null) {
        ""
    } else {
        val addressFirstSubstring = if (houseNo == null) street else "$street $houseNo"
        val addressSecondSubstring = if (city == null) "" else if (postalCode == null) city else "$postalCode $city"
        if (addressSecondSubstring.isNullOrEmpty()) addressFirstSubstring else "$addressFirstSubstring\n$addressSecondSubstring"
    }
}

@Preview
@Composable
fun DashboardDataItemPreview() {
    DashboardDataItemTemplate(
        Transaction().apply {
            location = ReadOnlyLocation().apply { brand = "PACE" }
            createdAt = Date()
            priceIncludingVAT = 13.37
            currency = "EUR"
        }
    )
}
