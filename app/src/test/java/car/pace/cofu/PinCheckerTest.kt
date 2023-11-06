package car.pace.cofu

import car.pace.cofu.util.PinChecker
import org.junit.Assert.assertEquals
import org.junit.Test


class PinCheckerTest {
    @Test
    fun pinCheck() {
        assertEquals(PinChecker.Result.OK, PinChecker.checkPin("1834"))
        assertEquals(PinChecker.Result.OK, PinChecker.checkPin("8274"))
        assertEquals(PinChecker.Result.INVALID_LENGTH, PinChecker.checkPin("123"))
        assertEquals(PinChecker.Result.INVALID_LENGTH, PinChecker.checkPin("284"))
        assertEquals(PinChecker.Result.INVALID_LENGTH, PinChecker.checkPin("92381"))
        assertEquals(PinChecker.Result.TOO_FEW_DIGITS, PinChecker.checkPin("0000"))
        assertEquals(PinChecker.Result.TOO_FEW_DIGITS, PinChecker.checkPin("1111"))
        assertEquals(PinChecker.Result.TOO_FEW_DIGITS, PinChecker.checkPin("3131"))
        assertEquals(PinChecker.Result.TOO_FEW_DIGITS, PinChecker.checkPin("3993"))
        assertEquals(PinChecker.Result.SERIES, PinChecker.checkPin("0123"))
        assertEquals(PinChecker.Result.SERIES, PinChecker.checkPin("1234"))
        assertEquals(PinChecker.Result.SERIES, PinChecker.checkPin("3456"))
        assertEquals(PinChecker.Result.SERIES, PinChecker.checkPin("7654"))
        assertEquals(PinChecker.Result.OK, PinChecker.checkPin("3454"))
    }
}
