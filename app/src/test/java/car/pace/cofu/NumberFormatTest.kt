package car.pace.cofu

import car.pace.cofu.core.util.formattedAsMeter
import org.junit.Test

import org.junit.Assert.*


class NumberFormatTest {
    @Test
    fun numberToStringConversion() {
        assertEquals("130 m", 130.0.formattedAsMeter)
        assertEquals("1 km", 1000.0.formattedAsMeter)
        assertEquals("1,4 km", 1400.0.formattedAsMeter)
        assertEquals("2 km", 1999.9999999.formattedAsMeter)
        assertEquals("2 km", 2000.0000001.formattedAsMeter)
        assertEquals("15 km", 15000.0.formattedAsMeter)
        assertEquals("16 km", 15900.0.formattedAsMeter)
    }
}