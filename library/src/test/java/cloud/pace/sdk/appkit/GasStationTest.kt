package cloud.pace.sdk.appkit

import cloud.pace.sdk.poikit.poi.Day
import cloud.pace.sdk.poikit.poi.GasStation
import cloud.pace.sdk.poikit.poi.OpeningHour
import cloud.pace.sdk.poikit.poi.OpeningHours
import cloud.pace.sdk.poikit.poi.OpeningRule
import junit.framework.Assert.assertFalse
import junit.framework.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.koin.test.KoinTest
import java.text.SimpleDateFormat
import java.util.Date

class GasStationTest : KoinTest {
    private lateinit var gasStation: GasStation

    private val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")

    private val monday08_30: Date = format.parse("2020-01-06T08:30:00Z")
    private val monday22_15: Date = format.parse("2020-01-06T22:15:00Z")
    private val wednesday00_01: Date = format.parse("2020-01-08T00:01:00Z")
    private val saturday10_20: Date = format.parse("2020-01-11T10:20:00Z")
    private val sunday10_20: Date = format.parse("2020-01-12T10:20:00Z")
    private val sunday23_59: Date = format.parse("2020-01-12T23:59:00Z")

    @Before
    fun setup() {
        gasStation = GasStation("", arrayListOf())
    }

    @Test
    fun `empty rules`() {
        gasStation.openingHours = listOf()

        assertFalse(gasStation.isOpen(monday08_30))
        assertFalse(gasStation.isOpen(monday22_15))
        assertFalse(gasStation.isOpen(wednesday00_01))
        assertFalse(gasStation.isOpen(saturday10_20))
        assertFalse(gasStation.isOpen(sunday10_20))
        assertFalse(gasStation.isOpen(sunday23_59))
    }

    @Test
    fun `always open`() {
        gasStation.openingHours = listOf(
            OpeningHours(
                listOf(Day.MONDAY, Day.TUESDAY, Day.WEDNESDAY, Day.THURSDAY, Day.FRIDAY, Day.SATURDAY, Day.SUNDAY),
                listOf(OpeningHour("0", "24")),
                OpeningRule.OPEN
            )
        )

        assertTrue(gasStation.isOpen(monday08_30))
        assertTrue(gasStation.isOpen(monday22_15))
        assertTrue(gasStation.isOpen(wednesday00_01))
        assertTrue(gasStation.isOpen(saturday10_20))
        assertTrue(gasStation.isOpen(sunday10_20))
        assertTrue(gasStation.isOpen(sunday23_59))
    }

    @Test
    fun `open only on monday`() {
        gasStation.openingHours = listOf(
            OpeningHours(
                listOf(Day.MONDAY),
                listOf(OpeningHour("0", "24")),
                OpeningRule.OPEN
            )
        )

        assertTrue(gasStation.isOpen(monday08_30))
        assertTrue(gasStation.isOpen(monday22_15))
        assertFalse(gasStation.isOpen(wednesday00_01))
        assertFalse(gasStation.isOpen(saturday10_20))
        assertFalse(gasStation.isOpen(sunday10_20))
        assertFalse(gasStation.isOpen(sunday23_59))
    }

    @Test
    fun `open only on weekends 0-22`() {
        gasStation.openingHours = listOf(
            OpeningHours(
                listOf(Day.SATURDAY, Day.SUNDAY),
                listOf(OpeningHour("0", "22")),
                OpeningRule.OPEN
            )
        )

        assertFalse(gasStation.isOpen(monday08_30))
        assertFalse(gasStation.isOpen(monday22_15))
        assertFalse(gasStation.isOpen(wednesday00_01))
        assertTrue(gasStation.isOpen(saturday10_20))
        assertTrue(gasStation.isOpen(sunday10_20))
        assertFalse(gasStation.isOpen(sunday23_59))
    }

    @Test
    fun `multiple rules for same day`() {
        gasStation.openingHours = listOf(
            OpeningHours(
                listOf(Day.MONDAY),
                listOf(OpeningHour("0", "2")),
                OpeningRule.OPEN
            ),
            OpeningHours(
                listOf(Day.MONDAY),
                listOf(OpeningHour("4", "5")),
                OpeningRule.OPEN
            ),
            OpeningHours(
                listOf(Day.MONDAY),
                listOf(OpeningHour("8", "9")),
                OpeningRule.OPEN
            ),
            OpeningHours(
                listOf(Day.MONDAY),
                listOf(OpeningHour("12", "15")),
                OpeningRule.OPEN
            )
        )

        assertTrue(gasStation.isOpen(monday08_30))
        assertFalse(gasStation.isOpen(monday22_15))
        assertFalse(gasStation.isOpen(wednesday00_01))
    }

    @Test
    fun `overlapping rules for same day`() {
        gasStation.openingHours = listOf(
            OpeningHours(
                listOf(Day.MONDAY),
                listOf(OpeningHour("0", "8")),
                OpeningRule.OPEN
            ),
            OpeningHours(
                listOf(Day.MONDAY),
                listOf(OpeningHour("1", "9")),
                OpeningRule.OPEN
            ),
            OpeningHours(
                listOf(Day.MONDAY),
                listOf(OpeningHour("4", "9")),
                OpeningRule.OPEN
            ),
            OpeningHours(
                listOf(Day.MONDAY),
                listOf(OpeningHour("8", "12")),
                OpeningRule.OPEN
            )
        )

        assertTrue(gasStation.isOpen(monday08_30))
        assertFalse(gasStation.isOpen(monday22_15))
        assertFalse(gasStation.isOpen(wednesday00_01))
    }

    @Test
    fun `closed rule`() {
        gasStation.openingHours = listOf(
            OpeningHours(
                listOf(Day.MONDAY),
                listOf(OpeningHour("8", "9")),
                OpeningRule.OPEN
            ),
            OpeningHours(
                listOf(Day.MONDAY),
                listOf(OpeningHour("7", "8")),
                OpeningRule.CLOSED
            ),
            OpeningHours(
                listOf(Day.MONDAY),
                listOf(OpeningHour("9", "10")),
                OpeningRule.CLOSED
            )
        )

        assertTrue(gasStation.isOpen(monday08_30))
        assertFalse(gasStation.isOpen(monday22_15))
        assertFalse(gasStation.isOpen(wednesday00_01))
        assertFalse(gasStation.isOpen(saturday10_20))
        assertFalse(gasStation.isOpen(sunday10_20))
        assertFalse(gasStation.isOpen(sunday23_59))
    }

    @Test
    fun `off by one test`() {
        gasStation.openingHours = listOf(
            OpeningHours(
                listOf(Day.SUNDAY),
                listOf(OpeningHour("23", "24")),
                OpeningRule.OPEN
            ),
            OpeningHours(
                listOf(Day.MONDAY),
                listOf(OpeningHour("0", "24")),
                OpeningRule.CLOSED
            ),
            OpeningHours(
                listOf(Day.SATURDAY),
                listOf(OpeningHour("0", "24")),
                OpeningRule.CLOSED
            )
        )

        assertFalse(gasStation.isOpen(monday08_30))
        assertFalse(gasStation.isOpen(monday22_15))
        assertFalse(gasStation.isOpen(wednesday00_01))
        assertFalse(gasStation.isOpen(saturday10_20))
        assertFalse(gasStation.isOpen(sunday10_20))
        assertTrue(gasStation.isOpen(sunday23_59))
    }

    @Test
    fun `overlapping opening hours between two days`() {
        val friday23_59: Date = format.parse("2020-01-10T23:59:00Z")
        val friday02_20: Date = format.parse("2020-01-10T02:20:00Z")
        val saturday02_20: Date = format.parse("2020-01-11T02:20:00Z")
        val sunday01_00: Date = format.parse("2020-01-12T01:00:00Z")

        gasStation.openingHours = listOf(
            OpeningHours(
                listOf(Day.FRIDAY),
                listOf(OpeningHour("6", "3")),
                OpeningRule.OPEN
            ),
            OpeningHours(
                listOf(Day.SATURDAY),
                listOf(OpeningHour("7", "2")),
                OpeningRule.OPEN
            ),
            OpeningHours(
                listOf(Day.SUNDAY),
                listOf(OpeningHour("8", "22")),
                OpeningRule.OPEN
            ),
        )

        assertTrue(gasStation.isOpen(friday23_59))
        assertFalse(gasStation.isOpen(friday02_20))
        assertTrue(gasStation.isOpen(saturday02_20))
        assertTrue(gasStation.isOpen(sunday01_00))
    }
}
