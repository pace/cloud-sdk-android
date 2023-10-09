package car.pace.cofu.util

import androidx.annotation.StringRes
import car.pace.cofu.R

/**
 * A simple checker for pin complexity and validity.
 */
object PinChecker {

    /**
     * Checks a given PIN for compliance with PACE's rules:
     * - must be 4 digits long
     * - must use 3 different digits
     * - must not be a numerical series (e.g. 1234, 4321, ...)
     */
    fun checkPin(pin: String): Result {
        val digits = pin.toCharArray()
        if (digits.size != 4) return Result.INVALID_LENGTH
        if (digits.toSet().size < 3) return Result.TOO_FEW_DIGITS
        if (digits[0] == digits[1] + 1 && digits[0] == digits[2] + 2 && digits[0] == digits[3] + 3) return Result.SERIES
        if (digits[0] == digits[1] - 1 && digits[0] == digits[2] - 2 && digits[0] == digits[3] - 3) return Result.SERIES
        return Result.OK
    }

    /**
     * Lists all pin check results.
     */
    enum class Result(@StringRes val errorStringRes: Int) {
        OK(0),
        INVALID_LENGTH(R.string.ONBOARDING_PIN_ERROR_INVALID_LENGTH),
        TOO_FEW_DIGITS(R.string.ONBOARDING_PIN_ERROR_TOO_FEW_DIGITS),
        SERIES(R.string.ONBOARDING_PIN_ERROR_SERIES)
    }
}
