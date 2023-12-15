package car.pace.cofu.util.extension

import android.icu.number.NumberFormatter
import android.icu.number.Precision
import android.icu.text.DateFormat
import android.icu.text.MeasureFormat
import android.icu.text.NumberFormat
import android.icu.util.Measure
import android.icu.util.MeasureUnit
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import car.pace.cofu.R
import car.pace.cofu.ui.wallet.fueltype.FuelType
import car.pace.cofu.ui.wallet.fueltype.FuelTypeGroup
import car.pace.cofu.util.Constants.COFU_DISTANCE_METERS
import car.pace.cofu.util.price.PriceFormatter
import car.pace.cofu.util.price.toUnicodeString
import cloud.pace.sdk.poikit.poi.Address
import cloud.pace.sdk.poikit.poi.GasStation
import cloud.pace.sdk.poikit.poi.Price
import cloud.pace.sdk.poikit.utils.distanceTo
import com.google.android.gms.maps.model.LatLng
import java.util.Date
import java.util.Locale

@Composable
fun Address.twoLineAddress() = remember {
    if (street == null) {
        ""
    } else {
        val firstLineAddress = firstLineAddress()
        val secondLineAddress = secondLineAddress()
        if (secondLineAddress.isNullOrEmpty()) firstLineAddress else "$firstLineAddress\n$secondLineAddress"
    }
}.orEmpty()

@Composable
fun Address.oneLineAddress() = remember {
    if (street == null) {
        ""
    } else {
        val firstLineAddress = firstLineAddress()
        val secondLineAddress = secondLineAddress()
        if (secondLineAddress.isNullOrEmpty()) firstLineAddress else "$firstLineAddress, $secondLineAddress"
    }
}.orEmpty()

private fun Address.firstLineAddress(): String? {
    return if (street == null) {
        ""
    } else {
        if (houseNumber == null) street else "$street $houseNumber"
    }
}

private fun Address.secondLineAddress(): String? {
    return if (street == null) {
        ""
    } else {
        if (city == null) "" else if (postalCode == null) city else "$postalCode $city"
    }
}

@Composable
fun LatLng.distanceText(destination: LatLng?): String? {
    val context = LocalContext.current
    val distance = remember(destination) { destination?.let { distanceTo(it) } } ?: return null
    return if (distance < COFU_DISTANCE_METERS) {
        context.getString(R.string.gas_station_location_here)
    } else {
        val showInMeters = distance < 1000
        val measureUnit = if (showInMeters) MeasureUnit.METER else MeasureUnit.KILOMETER
        val digits = if (showInMeters) 0 else 1
        val number = if (showInMeters) distance else distance / 1000
        val measure = Measure(number, measureUnit)
        val formattedDistance = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val formatter = remember(digits) {
                NumberFormatter.withLocale(Locale.getDefault())
                    .unitWidth(NumberFormatter.UnitWidth.SHORT)
                    .precision(Precision.fixedFraction(digits))
            }
            formatter.format(measure).toString()
        } else {
            val formatter = remember(digits) {
                val numberFormat = NumberFormat.getInstance().apply {
                    minimumFractionDigits = digits
                    maximumFractionDigits = digits
                }
                MeasureFormat.getInstance(Locale.getDefault(), MeasureFormat.FormatWidth.SHORT, numberFormat)
            }
            formatter.formatMeasures(measure)
        }

        context.getString(R.string.gas_station_location_away, formattedDistance)
    }
}

@Composable
fun GasStation.canStartFueling(userLocation: LatLng?) = remember(userLocation) {
    userLocation ?: return@remember false
    center?.toLatLn()?.let { it.distanceTo(userLocation) < COFU_DISTANCE_METERS } ?: false
}

@Composable
fun GasStation.formatPrice(fuelTypeGroup: FuelTypeGroup): String? {
    val price = remember(fuelTypeGroup) {
        val identifiers = fuelTypeGroup.fuelTypes.map(FuelType::identifier)
        prices
            .filter { it.price != null && it.type in identifiers }
            .minByOrNull { it.price ?: 0.0 }
    }

    return price?.formatPrice(priceFormat = priceFormat, currency = currency)
}

@Composable
fun Price.formatPrice(
    priceFormat: String?,
    currency: String?
): String? {
    val price = price ?: return null
    val context = LocalContext.current
    return remember(priceFormat, currency) {
        val format = if (PriceFormatter.isValidFormat(priceFormat)) priceFormat else "d.dds"
        PriceFormatter.formatPrice(price, currency, context.getString(R.string.currency_format), format ?: "d.dds", Locale.getDefault()).toUnicodeString()
    }
}

@Composable
fun Date.lastUpdatedText(): String {
    val formattedDateTime = remember {
        val formatter = DateFormat.getDateTimeInstance(DateFormat.RELATIVE, DateFormat.SHORT)
        formatter.format(this)
    }

    return stringResource(id = R.string.gas_station_last_updated, formattedDateTime)
}
