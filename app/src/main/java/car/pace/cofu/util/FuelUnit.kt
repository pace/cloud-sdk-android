package car.pace.cofu.util

import android.icu.util.MeasureUnit
import android.os.Build
import androidx.annotation.RequiresApi

enum class FuelUnit(val value: String, val symbol: String) {
    LITER("liter", "l"),
    US_GALLON("us-gallon", "gal"),
    UK_GALLON("uk-gallon", "Imp. gal"),
    KILOGRAM("kilogram", "kg");

    @RequiresApi(Build.VERSION_CODES.P)
    fun toMeasureUnit(): MeasureUnit {
        return when (this) {
            LITER -> MeasureUnit.LITER
            US_GALLON -> MeasureUnit.GALLON
            UK_GALLON -> MeasureUnit.GALLON_IMPERIAL
            KILOGRAM -> MeasureUnit.KILOGRAM
        }
    }

    companion object {
        private val map = entries.associateBy(FuelUnit::value)

        fun fromValue(value: String) = map[value] ?: LITER
    }
}
