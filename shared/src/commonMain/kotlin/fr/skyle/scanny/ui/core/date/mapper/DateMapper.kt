package fr.skyle.scanny.ui.core.date.mapper

import fr.skyle.scanny.ui.core.date.model.CalendarDay
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.format.char
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

// On 24 hours, in every language: 09:05, 21:48
private val TIME_FORMAT = LocalTime.Format {
    hour()
    char(':')
    minute()
}

internal fun Instant.toCalendarDay(
    today: LocalDate,
    timeZone: TimeZone,
): CalendarDay =
    when (val date = toLocalDateTime(timeZone).date) {
        today -> CalendarDay.Today
        today.minus(1, DateTimeUnit.DAY) -> CalendarDay.Yesterday
        else -> CalendarDay.Date(date = date)
    }

internal fun Instant.toTimeText(timeZone: TimeZone): String =
    toLocalDateTime(timeZone).time.toTimeText()

internal fun LocalTime.toTimeText(): String =
    format(TIME_FORMAT)