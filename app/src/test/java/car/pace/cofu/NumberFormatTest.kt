package car.pace.cofu

import car.pace.cofu.core.util.formattedAsMeter
import org.junit.Test

import org.junit.Assert.*


class NumberFormatTest {
    @Test
    fun numberToStringConversion() {
        assertEquals("130m", 130.0.formattedAsMeter)
        assertEquals("1km", 1000.0.formattedAsMeter)
        assertEquals("1.4km", 1400.0.formattedAsMeter)
        assertEquals("2km", 1999.9999999.formattedAsMeter)
        assertEquals("2km", 2000.0000001.formattedAsMeter)
        assertEquals("15km", 15000.0.formattedAsMeter)
        assertEquals("16km", 15900.0.formattedAsMeter)
    }
}