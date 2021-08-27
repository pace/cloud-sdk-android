package car.pace.cofu.repository

import car.pace.cofu.R

/**
 * Lists all possible fuel types.
 */
enum class FuelType(val identifier: String, val stringRes: Int) {
    DIESEL("diesel", R.string.fuel_type_diesel),
    SUPER("ron95e5", R.string.fuel_type_super),
    E10("ron95e10", R.string.fuel_type_e10),
    SUPER_PLUS("ron98e5", R.string.fuel_type_super_plus),
}