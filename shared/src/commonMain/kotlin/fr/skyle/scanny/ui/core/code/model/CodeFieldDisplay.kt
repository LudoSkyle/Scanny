package fr.skyle.scanny.ui.core.code.model

import fr.skyle.scanny.domain.model.EventClassification
import fr.skyle.scanny.domain.model.EventRecurrence
import fr.skyle.scanny.domain.model.EventStatus
import fr.skyle.scanny.domain.model.EventTransparency
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

/** A value of a field that the screen writes in the language of the device: kotlinx-datetime does not. */
sealed interface CodeFieldDisplay {

    data class Day(val date: LocalDate) : CodeFieldDisplay

    // In [timeZone] unless null, the time wherever one is
    data class DateTime(
        val date: LocalDate,
        val time: LocalTime,
        val timeZone: String?,
    ) : CodeFieldDisplay

    data class Recurrence(val recurrence: EventRecurrence) : CodeFieldDisplay

    // Minutes before the start
    data class Reminder(val minutes: Int) : CodeFieldDisplay

    data class Status(val status: EventStatus) : CodeFieldDisplay

    data class Availability(val transparency: EventTransparency) : CodeFieldDisplay

    data class Classification(val classification: EventClassification) : CodeFieldDisplay
}