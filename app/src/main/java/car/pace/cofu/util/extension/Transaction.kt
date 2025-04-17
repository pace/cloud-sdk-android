package car.pace.cofu.util.extension

import android.icu.math.BigDecimal
import android.icu.number.NumberFormatter
import android.icu.number.Precision
import android.icu.text.DateFormat
import android.icu.text.MeasureFormat
import android.icu.text.NumberFormat
import android.icu.util.Currency
import android.icu.util.Measure
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import car.pace.cofu.util.FuelUnit
import car.pace.cofu.util.price.PriceFormatter
import cloud.pace.sdk.api.pay.generated.model.Transaction
import java.math.RoundingMode
import java.util.Locale

@Composable
fun Transaction.formatCreationDate(): String? {
    val creationDate = createdAt ?: return null
    return remember {
        val date = DateFormat.getInstanceForSkeleton(DateFormat.WEEKDAY + DateFormat.YEAR_NUM_MONTH_DAY).format(creationDate)
        val time = DateFormat.getTimeInstance(DateFormat.SHORT).format(creationDate)
        "$date · $time"
    }
}

@Composable
fun Transaction.formatPrice(minFractionPlaces: Int = 2, maxFractionPlaces: Int = 2): String? {
    val price = priceIncludingVAT ?: return null
    val currency = currency
    return remember(price, currency, minFractionPlaces, maxFractionPlaces) {
        formatPrice(price, currency, minFractionPlaces, maxFractionPlaces)
    }
}

@Composable
fun Transaction.formatPricePerUnit(minFractionPlaces: Int = 2, maxFractionPlaces: Int = 3): String? {
    val price = fuel?.pricePerUnit ?: return null
    val currency = currency
    val fuelUnit = fuel?.unit?.let { FuelUnit.fromValue(it) } ?: FuelUnit.LITER
    return remember(price, currency, fuelUnit, minFractionPlaces, maxFractionPlaces) {
        val priceWithoutCurrency = formatPrice(price, null, minFractionPlaces, maxFractionPlaces).trim()
        val currencySymbol = PriceFormatter.getCurrencySymbol(currency, Locale.getDefault())
        val fuelUnitSymbol = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            MeasureFormat.getInstance(Locale.getDefault(), MeasureFormat.FormatWidth.NARROW).getUnitDisplayName(fuelUnit.toMeasureUnit())
        } else {
            fuelUnit.symbol
        }
        "$priceWithoutCurrency $currencySymbol/$fuelUnitSymbol"
    }
}

@Composable
fun Transaction.formatFuelAmount(): String? {
    val fuelAmount = fuel?.amount ?: return null
    val fuelUnit = fuel?.unit?.let { FuelUnit.fromValue(it) } ?: FuelUnit.LITER
    return remember(fuelAmount, fuelUnit) {
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
                val measure = Measure(fuelAmount, fuelUnit.toMeasureUnit())
                NumberFormatter.withLocale(Locale.getDefault())
                    .unitWidth(NumberFormatter.UnitWidth.FULL_NAME)
                    .precision(Precision.fixedFraction(2))
                    .format(measure)
                    .toString()
            }

            Build.VERSION.SDK_INT >= Build.VERSION_CODES.P -> {
                val measure = Measure(fuelAmount, fuelUnit.toMeasureUnit())
                val numberFormat = NumberFormat.getInstance().apply {
                    minimumFractionDigits = 2
                    maximumFractionDigits = 2
                }
                MeasureFormat.getInstance(Locale.getDefault(), MeasureFormat.FormatWidth.WIDE, numberFormat).formatMeasures(measure)
            }

            else -> {
                val numberFormat = java.text.NumberFormat.getInstance().apply {
                    minimumFractionDigits = 2
                    maximumFractionDigits = 2
                }
                "${numberFormat.format(fuelAmount)} ${fuelUnit.symbol}"
            }
        }
    }
}

fun formatPrice(price: Double, currency: String?, minFractionPlaces: Int = 2, maxFractionPlaces: Int = 2): String {
    return when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
            NumberFormatter.withLocale(Locale.getDefault())
                .unit(Currency.getInstance(currency ?: "EUR"))
                .unitWidth(if (currency != null) NumberFormatter.UnitWidth.NARROW else NumberFormatter.UnitWidth.HIDDEN)
                .roundingMode(RoundingMode.DOWN)
                .precision(Precision.minMaxFraction(minFractionPlaces, maxFractionPlaces))
                .format(price)
                .toString()
        }

        else -> {
            val numberFormat = if (currency != null) NumberFormat.getCurrencyInstance() else NumberFormat.getInstance()
            numberFormat.apply {
                minimumFractionDigits = minFractionPlaces
                maximumFractionDigits = maxFractionPlaces
                roundingMode = BigDecimal.ROUND_DOWN
                if (currency != null) {
                    this.currency = Currency.getInstance(currency)
                }
            }
            numberFormat.format(price)
        }
    }
}
