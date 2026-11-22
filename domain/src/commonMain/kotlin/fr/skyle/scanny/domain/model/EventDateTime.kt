package fr.skyle.scanny.domain.model

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

/**
 * A day of an event, and its time unless the event lasts the whole day. The time is in [timeZone]: "UTC",
 * a zone of TZID ("Europe/Paris"), or null for the time wherever one is, which iCalendar calls floating.
 */
data class EventDateTime(
    val date: LocalDate,
    val time: LocalTime? = null,
    val timeZone: String? = null,
) {

    companion object {
        const val UTC = "UTC"
    }
}