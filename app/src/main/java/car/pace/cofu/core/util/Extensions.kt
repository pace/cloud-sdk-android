package car.pace.cofu.core.util

import androidx.databinding.ObservableInt
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Increases the value [by].
 */
fun ObservableInt.increase(by: Int = 1) {
    set(get() + by)
}

/**
 * Decreases the value [by].
 */
fun ObservableInt.decrease(by: Int = 1) {
    set(get() - by)
}

/**
 * Formats a double in m. If below 1000, it will show the distance in meters. Between 1 and 10 km
 * it will show the distance in kilometers with one decimal place (if necessary), for greater distances
 * it will show the distance in kilometers without decimal places.
 */
val Double.formattedAsMeter: String
    get() = when {
        this < 1000 -> "${this.toInt()}m"
        this < 10000 -> {
            if (abs((this / 1000).roundToInt() - (this / 1000)) < 0.00001) {
                "${(this / 1000).roundToInt()}km"
            } else {
                "${"%.1f".format(this / 1000)}km"
            }
        }
        else -> "${(this / 1000).roundToInt()}km"
    }
