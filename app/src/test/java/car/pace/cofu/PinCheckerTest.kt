package car.pace.cofu

import car.pace.cofu.util.PinChecker
import car.pace.cofu.util.extension.InvalidLengthException
import car.pace.cofu.util.extension.SeriesNotAllowedException
import car.pace.cofu.util.extension.TooFewDigitsException
import org.junit.Assert.assertTrue
import org.junit.Test

class PinCheckerTest {
    @Test
    fun pinCheck() {
        assertTrue(PinChecker.checkPin("1834").getOrNull() is Unit)
        assertTrue(PinChecker.checkPin("8274").getOrNull() is Unit)
        assertTrue(PinChecker.checkPin("123").exceptionOrNull() is InvalidLengthException)
        assertTrue(PinChecker.checkPin("284").exceptionOrNull() is InvalidLengthException)
        assertTrue(PinChecker.checkPin("92381").exceptionOrNull() is InvalidLengthException)
        assertTrue(PinChecker.checkPin("0000").exceptionOrNull() is TooFewDigitsException)
        assertTrue(PinChecker.checkPin("1111").exceptionOrNull() is TooFewDigitsException)
        assertTrue(PinChecker.checkPin("3131").exceptionOrNull() is TooFewDigitsException)
        assertTrue(PinChecker.checkPin("3993").exceptionOrNull() is TooFewDigitsException)
        assertTrue(PinChecker.checkPin("0123").exceptionOrNull() is SeriesNotAllowedException)
        assertTrue(PinChecker.checkPin("1234").exceptionOrNull() is SeriesNotAllowedException)
        assertTrue(PinChecker.checkPin("3456").exceptionOrNull() is SeriesNotAllowedException)
        assertTrue(PinChecker.checkPin("7654").exceptionOrNull() is SeriesNotAllowedException)
        assertTrue(PinChecker.checkPin("3454").getOrNull() is Unit)
    }
}
