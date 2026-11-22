package fr.skyle.scanny.domain.model

import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate

/**
 * How an event repeats (RRULE of iCalendar): every [interval] periods of [frequency], on [days] of the
 * week for a weekly one, [count] times or until the day [until], or for ever.
 */
data class EventRecurrence(
    val frequency: EventFrequency,
    val interval: Int? = null,
    val count: Int? = null,
    val until: LocalDate? = null,
    val days: Set<DayOfWeek> = emptySet(),
)