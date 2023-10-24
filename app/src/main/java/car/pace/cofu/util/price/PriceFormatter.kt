package car.pace.cofu.util.price

import android.text.Spannable
import android.text.SpannableStringBuilder
import androidx.core.text.getSpans
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Currency
import java.util.Locale

object PriceFormatter {

    private val digitFormatRegex = "[d,s]+\\.*[d,s]*".toRegex()

    // initializing DecimalFormats is expensive, save them
    // key contains format + locale, e.g. "d.ddsen"
    private val formatterCache = mutableMapOf<String, DecimalFormat>()

    // same for currency
    private val currencyCache = mutableMapOf<String, String>()

    /**
     * Formats the given double according to the given format and currency position.
     * Superscript digits are formatted using a Spannable. If your representation cannot handle Spannable, use toUnicodeString() afterwards
     *
     * @param price value of the price to format
     * @param currency currency as in ISO 4217, e.g. "EUR"; empty string is valid
     * @param currencyFormat a string with two (indexed) placeholders where the first is the price position and the second the currency position. No other characters are allowed. Defaults to "%1$s%2$s" if it is invalid.
     * @param digitFormat a string defining the digit format of the price, e.g. "d.dds" for typical german fuel price formatting; see vector tile spec
     * @param loc the locale to use for formatting
     *
     * @return a spannable string with the formatted price for usage in TextViews
     */
    fun formatPrice(price: Double, currency: String?, currencyFormat: String?, digitFormat: String, loc: Locale): SpannableStringBuilder {
        val validCurrencyFormat = if (currencyFormat != null && isCurrencyFormatValid(currencyFormat)) currencyFormat else "%1\$s%2\$s"
        val formatter = initOrGetFormatter(digitFormat, loc)
        val priceString = String.format(validCurrencyFormat, formatter.format(price), getCurrencySymbol(currency, loc))
        return applySuperscript(priceString, digitFormat, loc)
    }

    /**
     * Checks if [currencyFormat] contains exactly two indexed string placeholders.
     *
     * @return True if the [currencyFormat] contains two indexed string placeholders, false otherwise
     */
    fun isCurrencyFormatValid(currencyFormat: String): Boolean {
        return "(%[0-9]\\$[sd]){2}".toRegex().matches(currencyFormat)
    }

    /**
     * Returns the currency symbol for the given currency ISO code, or empty string if none was found
     */
    fun getCurrencySymbol(currency: String?, loc: Locale): String {
        val key = currency + loc.toString()
        var currencySymbol = currencyCache[key]
        if (currencySymbol != null) {
            return currencySymbol
        } else {
            try {
                currencySymbol = Currency.getInstance(currency ?: "").getSymbol(loc)
                currencyCache.put(key, currencySymbol)
                return currencySymbol
            } catch (e: IllegalArgumentException) {
                return ""
            }
        }
    }

    /**
     * Returns true if the given format string is a valid digit format according to the vector tile spec
     */
    fun isValidFormat(formatString: String?): Boolean {
        return formatString?.matches(digitFormatRegex) ?: false
    }

    private fun initOrGetFormatter(formatString: String, loc: Locale): DecimalFormat {
        val formatterKey = formatString + loc.toString()
        var formatter = formatterCache[formatterKey]
        if (formatter == null) {
            if (!isValidFormat(formatString)) {
                throw IllegalArgumentException("Not a valid digit format: $formatString")
            }

            val decimalFormat = formatString
                .replace('d', '0')
                .replace('s', '0') // just insert normal digit, superscript is applied later
            formatter = DecimalFormat(decimalFormat).apply {
                roundingMode = RoundingMode.FLOOR
                decimalFormatSymbols = DecimalFormatSymbols.getInstance(loc)
            }
            formatterCache.put(formatterKey, formatter)
        }
        return formatter
    }

    // put a superscript spannable onto every digit which is marked with an 's' in the digitFormat
    private fun applySuperscript(priceString: String, format: String, loc: Locale): SpannableStringBuilder {
        val span = SpannableStringBuilder(priceString)

        // do all indices relative to separator, because price string might have different length than format
        val sep = DecimalFormatSymbols.getInstance(loc).getDecimalSeparator()
        val formatSeparatorIndex = format.indexOf(".")
        val priceSeparatorIndex = priceString.indexOf(sep)
        format.forEachIndexed { i, c ->
            if (c == 's') {
                val superscriptIndex = i - formatSeparatorIndex
                span.setSpan(
                    TopAlignedSuperscriptSpan(0.6f),
                    priceSeparatorIndex + superscriptIndex,
                    priceSeparatorIndex + superscriptIndex + 1,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
        }
        return span
    }
}

// convert to string using unicode characters for superscript instead of spannable
fun SpannableStringBuilder.toUnicodeString(): String {
    for (i in 0..this.length - 1)
        if (getSpans<TopAlignedSuperscriptSpan>(i, i + 1).size > 0) {
            val replacement = when (this[i]) {
                '0' -> '\u2070'
                '1' -> '\u00b9'
                '2' -> '\u00b2'
                '3' -> '\u00b3'
                '4' -> '\u2074'
                '5' -> '\u2075'
                '6' -> '\u2076'
                '7' -> '\u2077'
                '8' -> '\u2078'
                '9' -> '\u2079'
                else -> this[i]
            }
            this.replace(i, i + 1, replacement.toString())
        }
    return this.toString()
}

fun SpannableStringBuilder.surroundWithSpaces(): SpannableStringBuilder {
    for (i in 0..this.length - 1)
        when {
            getSpans<TopAlignedSuperscriptSpan>(i, i + 1).size > 0 -> {
                val replacement = " ${this[i]} "
                this.replace(i, i + 1, replacement)
            }

            i == 0 -> {
                val replacement = " ${this[i]}"
                this.replace(i, i + 1, replacement)
            }

            i == this.length - 1 -> {
                val replacement = "${this[i]} "
                this.replace(i, i + 1, replacement)
            }
        }
    return this
}
