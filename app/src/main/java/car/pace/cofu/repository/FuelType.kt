package car.pace.cofu.repository

import car.pace.cofu.R

/**
 * Lists all possible fuel types.
 */
enum class FuelType(val identifier: String, val stringRes: Int) {
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