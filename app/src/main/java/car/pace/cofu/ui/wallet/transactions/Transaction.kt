package car.pace.cofu.ui.wallet.transactions

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
import cloud.pace.sdk.api.pay.generated.model.ReadOnlyLocation.Address
import java.math.RoundingMode
import java.util.Date
import java.util.Locale
import timber.log.Timber

class Transaction(
    val id: String,
    val createdAt: Date,
    val productName: String,
    val price: Double,
    val currency: String,
    val stationName: String?,
    val address: Address?,
    val pumpNumber: Int?,
    val fuelAmount: Double,
    val pricePerFuelUnit: Double,
    val fuelUnit: String,
    val paymentMethodId: String
) {

    @Composable
    fun formatCreationDate(showWeekday: Boolean): String {
        return remember {
            val dateSkeleton = if (showWeekday) DateFormat.WEEKDAY + DateFormat.YEAR_NUM_MONTH_DAY else DateFormat.YEAR_NUM_MONTH_DAY
            val date = DateFormat.getInstanceForSkeleton(dateSkeleton).format(createdAt)
            val time = DateFormat.getTimeInstance(DateFormat.SHORT).format(createdAt)
            "$date · $time"
        }
    }

    @Composable
    fun formatPrice(minFractionPlaces: Int = 2, maxFractionPlaces: Int = 2): String {
        val currency = currency
        return remember(price, currency, minFractionPlaces, maxFractionPlaces) {
            formatPrice(price, currency, minFractionPlaces, maxFractionPlaces)
        }
    }

    @Composable
    fun formatPricePerUnit(minFractionPlaces: Int = 2, maxFractionPlaces: Int = 3): String {
        val fuelUnit = FuelUnit.fromValue(fuelUnit)
        return remember(pricePerFuelUnit, currency, fuelUnit, minFractionPlaces, maxFractionPlaces) {
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
    fun formatFuelAmount(): String {
        val fuelUnit = FuelUnit.fromValue(fuelUnit)
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

    private fun formatPrice(price: Double, currency: String?, minFractionPlaces: Int = 2, maxFractionPlaces: Int = 2): String {
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
}

fun cloud.pace.sdk.api.pay.generated.model.Transaction.toTransaction(): Transaction? {
    return try {
        Transaction(
            id = id,
            createdAt = createdAt ?: throw IllegalStateException("Transaction created date null"),
            productName = fuel?.productName ?: throw IllegalStateException("Transaction product name null"),
            price = priceIncludingVAT ?: throw IllegalStateException("Transaction price null"),
            currency = currency ?: throw IllegalStateException("Transaction currency null"),
            stationName = location?.brand,
            address = location?.address,
            pumpNumber = fuel?.pumpNumber,
            fuelAmount = fuel?.amount ?: throw IllegalStateException("Transaction fuel amount null"),
            pricePerFuelUnit = fuel?.pricePerUnit ?: throw IllegalStateException("Transaction price per unit null"),
            fuelUnit = fuel?.unit ?: throw IllegalStateException("Transaction fuel unit null"),
            paymentMethodId = paymentMethodId ?: throw IllegalStateException("Transaction payment method null")
        )
    } catch (e: Exception) {
        Timber.e(e, "Could not convert transaction")
        null
    }
}
