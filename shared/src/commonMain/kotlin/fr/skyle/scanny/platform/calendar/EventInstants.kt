package fr.skyle.scanny.platform.calendar

import fr.skyle.scanny.domain.model.BarcodeContent
import fr.skyle.scanny.domain.model.EventDateTime
import kotlinx.datetime.IllegalTimeZoneException
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toInstant
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Instant

/**
 * The instant a day and a time of an event are: a whole day at its midnight in [wholeDayZone], a time in
 * its zone, or in [deviceZone] when it is floating or its zone is not known.
 */
internal fun EventDateTime.toInstant(
    deviceZone: TimeZone,
    wholeDayZone: TimeZone,
): Instant {
    val time = time ?: return date.atStartOfDayIn(wholeDayZone)
    return LocalDateTime(date, time).toInstant(timeZone?.let { zone -> timeZoneOrNull(id = zone) } ?: deviceZone)
}

/** When an event ends: its end, or an hour after its start, or the day after a whole day. */
internal fun BarcodeContent.Event.endInstant(
    deviceZone: TimeZone,
    wholeDayZone: TimeZone,
): Instant =
    end?.toInstant(deviceZone = deviceZone, wholeDayZone = wholeDayZone)
        ?: start.toInstant(deviceZone = deviceZone, wholeDayZone = wholeDayZone).plus(if (start.time == null) 1.days else 1.hours)

// A TZID that is not a zone of the IANA, as some calendars of Windows write them, is not known
internal fun timeZoneOrNull(id: String): TimeZone? =
    try {
        TimeZone.of(zoneId = id)
    } catch (exception: IllegalTimeZoneException) {
        null
    }