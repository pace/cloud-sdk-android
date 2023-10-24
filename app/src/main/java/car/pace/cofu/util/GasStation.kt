package car.pace.cofu.util

import android.icu.number.NumberFormatter
import android.icu.number.Precision
import android.icu.text.MeasureFormat
import android.icu.text.NumberFormat
import android.icu.util.Measure
import android.icu.util.MeasureUnit
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import car.pace.cofu.R
import car.pace.cofu.ui.fueltype.FuelType
import car.pace.cofu.util.price.PriceFormatter
import car.pace.cofu.util.price.toUnicodeString
import cloud.pace.sdk.poikit.poi.GasStation
import cloud.pace.sdk.poikit.utils.distanceTo
import com.google.android.gms.maps.model.LatLng
import java.util.Locale

private const val DISTANCE_THRESHOLD = 500

@Composable
fun GasStation.twoLineAddress() = remember {
    if (address?.street == null) {
        ""
    } else {
        val firstLineAddress = firstLineAddress()
        val secondLineAddress = secondLineAddress()
        if (secondLineAddress.isNullOrEmpty()) firstLineAddress else "$firstLineAddress\n$secondLineAddress"
    }
}.orEmpty()

private fun GasStation.firstLineAddress(): String? {
    val address = address
    return if (address?.street == null) {
        ""
    } else {
        if (address.houseNumber == null) address.street else "${address.street} ${address.houseNumber}"
    }
}

private fun GasStation.secondLineAddress(): String? {
    val address = address
    return if (address?.street == null) {
        ""
    } else {
        if (address.city == null) "" else if (address.postalCode == null) address.city else "${address.postalCode} ${address.city}"
    }
}

@Composable
fun GasStation.distanceText(userLocation: LatLng?): String? {
    val distance = remember(userLocation) { userLocation?.let { center?.toLatLn()?.distanceTo(it) } } ?: return null
    val showInMeters = distance < 1000
    val measureUnit = if (showInMeters) MeasureUnit.METER else MeasureUnit.KILOMETER
    val digits = if (showInMeters) 0 else 1
    val number = if (showInMeters) distance else distance / 1000
    val measure = Measure(number, measureUnit)

    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
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
}

@Composable
fun GasStation.canStartFueling(userLocation: LatLng?) = remember(userLocation) {
    userLocation ?: return@remember false
    center?.toLatLn()?.let { it.distanceTo(userLocation) < DISTANCE_THRESHOLD } ?: false
}

@Composable
fun GasStation.formatPrice(fuelType: FuelType): String? {
    val context = LocalContext.current
    return remember(fuelType) {
        prices.find { it.type == fuelType.identifier }?.price?.let {
            val format = if (PriceFormatter.isValidFormat(priceFormat)) priceFormat else "d.dds"
            PriceFormatter.formatPrice(it, currency, context.getString(R.string.currency_format), format ?: "d.dds", Locale.getDefault()).toUnicodeString()
        }
    }
}
