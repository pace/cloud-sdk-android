package car.pace.cofu.util

import car.pace.cofu.util.extension.InvalidLengthException
import car.pace.cofu.util.extension.SeriesNotAllowedException
import car.pace.cofu.util.extension.TooFewDigitsException

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
    fun checkPin(pin: String): Result<Unit> {
        val digits = pin.toCharArray()
        if (digits.size != 4) return Result.failure(InvalidLengthException())
        if (digits.toSet().size < 3) return Result.failure(TooFewDigitsException())
        if (digits[0] == digits[1] + 1 && digits[0] == digits[2] + 2 && digits[0] == digits[3] + 3) return Result.failure(SeriesNotAllowedException())
        if (digits[0] == digits[1] - 1 && digits[0] == digits[2] - 2 && digits[0] == digits[3] - 3) return Result.failure(SeriesNotAllowedException())
        return Result.success(Unit)
    }
}
