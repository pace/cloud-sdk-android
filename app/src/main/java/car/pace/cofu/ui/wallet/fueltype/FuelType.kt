package car.pace.cofu.ui.wallet.fueltype

import androidx.annotation.StringRes
import car.pace.cofu.R

/**
 * Lists all possible fuel types.
 *
 * NOTE: Please do not changes the order of this enum because the [Enum.ordinal] was saved as selected fuel type in the old app :/
 */
enum class FuelType(val identifier: String, @StringRes val stringRes: Int) {
    DIESEL("diesel", R.string.fuel_group_diesel),
    SUPER("ron95e5", R.string.fuel_group_petrol),
    E10("ron95e10", R.string.fuel_group_petrol),
    SUPER_PLUS("ron98e5", R.string.fuel_group_petrol),
    E10_RON98("ron98e10", R.string.fuel_group_petrol),
    RON98("ron98", R.string.fuel_group_petrol),
    RON100("ron100", R.string.fuel_group_petrol),
    DIESEL_B7("dieselB7", R.string.fuel_group_diesel),
    DIESEL_PREMIUM("dieselPremium", R.string.fuel_group_diesel),
    DIESEL_GTL("dieselGtl", R.string.fuel_group_diesel),
    DIESEL_B0("dieselB0", R.string.fuel_group_diesel);

    fun toFuelTypeGroup(): FuelTypeGroup {
        return FuelTypeGroup.values().find { this in it.fuelTypes } ?: FuelTypeGroup.PETROL
    }
}

enum class FuelTypeGroup(val prefFuelType: FuelType, val fuelTypes: List<FuelType>, @StringRes val stringRes: Int) {
    PETROL(FuelType.SUPER, listOf(FuelType.SUPER, FuelType.E10, FuelType.SUPER_PLUS, FuelType.E10_RON98, FuelType.RON98, FuelType.RON100), R.string.fuel_group_petrol),
    DIESEL(FuelType.DIESEL, listOf(FuelType.DIESEL, FuelType.DIESEL_B7, FuelType.DIESEL_PREMIUM, FuelType.DIESEL_GTL, FuelType.DIESEL_B0), R.string.fuel_group_diesel)
}

fun Int.toFuelTypeGroup(): FuelTypeGroup {
    return FuelType.values().getOrNull(this)?.toFuelTypeGroup() ?: FuelTypeGroup.PETROL
}
