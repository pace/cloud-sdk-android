package car.pace.cofu.repository

import androidx.annotation.StringRes
import car.pace.cofu.R

/**
 * Lists all possible fuel types.
 *
 * NOTE: Please do not changes the order of this enum because we save the [Enum.ordinal] as selected fuel type :/
 */
enum class FuelType(val identifier: String, @StringRes val stringRes: Int) {
    DIESEL("diesel", R.string.FUEL_TYPE_DIESEL),
    SUPER("ron95e5", R.string.FUEL_TYPE_SUPER),
    E10("ron95e10", R.string.FUEL_TYPE_SUPER_E10),
    SUPER_PLUS("ron98e5", R.string.FUEL_TYPE_SUPER_PLUS);

    companion object {
        private val map = values().associateBy { it.identifier }
        fun byIdentifier(identifier: String): FuelType? {
            return map[identifier]
        }
    }
}
