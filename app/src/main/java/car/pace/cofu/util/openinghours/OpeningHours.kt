package car.pace.cofu.util.openinghours

import android.text.format.DateFormat
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import car.pace.cofu.R
import cloud.pace.sdk.poikit.poi.GasStation
import cloud.pace.sdk.poikit.poi.OpeningHours
import java.util.Date

@Composable
fun List<OpeningHours>.format(): List<OpeningHoursItem> {
    val context = LocalContext.current
    return remember {
        val timeTable = PoiTimeTableConverter().convertToTimetable(this)
        val days = arrayOf(
            context.getString(R.string.common_use_monday),
            context.getString(R.string.common_use_tuesday),
            context.getString(R.string.common_use_wednesday),
            context.getString(R.string.common_use_thursday),
            context.getString(R.string.common_use_friday),
            context.getString(R.string.common_use_saturday),
            context.getString(R.string.common_use_sunday)
        )
        val closed = context.getString(R.string.gas_station_opening_hours_closed)
        val alwaysOpen = context.getString(R.string.gas_station_opening_hours_always_open)
        val daily = context.getString(R.string.gas_station_opening_hours_daily)
        val is24HourFormat = DateFormat.is24HourFormat(context)

        timeTable.toHumanReadableString(days, closed, alwaysOpen, daily, is24HourFormat)
    }
}

@Composable
fun GasStation.openingHoursStatus(): OpeningHoursStatus {
    return if (isClosed()) {
        OpeningHoursStatus.Closed
    } else {
        val closesSoon = closesSoon()
        if (closesSoon != null) OpeningHoursStatus.ClosesSoon(closesSoon) else OpeningHoursStatus.Open
    }
}

@Composable
fun GasStation.isClosed(): Boolean {
    return !isOpenOrUnknown()
}

@Composable
fun GasStation.isOpenOrUnknown(): Boolean {
    val now = Date()
    return remember(now) {
        isOpen(now) || openingHours.isEmpty()
    }
}

@Composable
fun GasStation.closesSoon(): String? {
    val now = Date()
    return remember(now) {
        val timeTable = PoiTimeTableConverter().convertToTimetable(openingHours)
        timeTable.closesSoon(now)
    }
}

sealed class OpeningHoursStatus {
    object Open : OpeningHoursStatus()
    object Closed : OpeningHoursStatus()
    data class ClosesSoon(val closesAt: String) : OpeningHoursStatus()
}
