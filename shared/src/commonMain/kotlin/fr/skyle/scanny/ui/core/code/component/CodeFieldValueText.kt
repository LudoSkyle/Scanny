package fr.skyle.scanny.ui.core.code.component

import androidx.compose.runtime.Composable
import fr.skyle.scanny.domain.model.EventClassification
import fr.skyle.scanny.domain.model.EventFrequency
import fr.skyle.scanny.domain.model.EventRecurrence
import fr.skyle.scanny.domain.model.EventStatus
import fr.skyle.scanny.domain.model.EventTransparency
import fr.skyle.scanny.resources.Res
import fr.skyle.scanny.resources.at_start
import fr.skyle.scanny.resources.busy
import fr.skyle.scanny.resources.class_confidential
import fr.skyle.scanny.resources.class_private
import fr.skyle.scanny.resources.class_public
import fr.skyle.scanny.resources.date_at_time
import fr.skyle.scanny.resources.days_before
import fr.skyle.scanny.resources.every_days
import fr.skyle.scanny.resources.every_months
import fr.skyle.scanny.resources.every_weeks
import fr.skyle.scanny.resources.every_years
import fr.skyle.scanny.resources.free
import fr.skyle.scanny.resources.hours_before
import fr.skyle.scanny.resources.minutes_before
import fr.skyle.scanny.resources.recurrence_with
import fr.skyle.scanny.resources.status_cancelled
import fr.skyle.scanny.resources.status_confirmed
import fr.skyle.scanny.resources.status_tentative
import fr.skyle.scanny.resources.time_in_zone
import fr.skyle.scanny.resources.times_count
import fr.skyle.scanny.resources.until_day
import fr.skyle.scanny.resources.week_days
import fr.skyle.scanny.ui.core.code.model.CodeField
import fr.skyle.scanny.ui.core.code.model.CodeFieldDisplay
import fr.skyle.scanny.ui.core.date.component.calendarDayText
import fr.skyle.scanny.ui.core.date.mapper.toTimeText
import fr.skyle.scanny.ui.core.date.model.CalendarDay
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringArrayResource
import org.jetbrains.compose.resources.stringResource

private const val MINUTES_PER_HOUR = 60
private const val MINUTES_PER_DAY = 1_440

/** The value of a field as the screen shows it, and copies it: in the language of the device. */
@Composable
internal fun codeFieldValueText(field: CodeField): String =
    when (val display = field.display) {
        null -> field.value
        is CodeFieldDisplay.Day -> calendarDayText(day = CalendarDay.Date(date = display.date))
        is CodeFieldDisplay.DateTime -> {
            val dateTime = stringResource(Res.string.date_at_time, calendarDayText(day = CalendarDay.Date(date = display.date)), display.time.toTimeText())
            display.timeZone?.let { timeZone -> stringResource(Res.string.time_in_zone, dateTime, timeZone) } ?: dateTime
        }
        is CodeFieldDisplay.Recurrence -> recurrenceText(recurrence = display.recurrence)
        is CodeFieldDisplay.Reminder -> reminderText(minutes = display.minutes)
        is CodeFieldDisplay.Status -> eventStatusText(status = display.status)
        is CodeFieldDisplay.Availability -> availabilityText(transparency = display.transparency)
        is CodeFieldDisplay.Classification -> classificationText(classification = display.classification)
    }

// "Every 2 weeks: Monday, Wednesday, 10 times"
@Composable
private fun recurrenceText(recurrence: EventRecurrence): String {
    val interval = recurrence.interval ?: 1
    val period = pluralStringResource(
        resource = when (recurrence.frequency) {
            EventFrequency.DAILY -> Res.plurals.every_days
            EventFrequency.WEEKLY -> Res.plurals.every_weeks
            EventFrequency.MONTHLY -> Res.plurals.every_months
            EventFrequency.YEARLY -> Res.plurals.every_years
        },
        quantity = interval,
        interval,
    )
    val weekDays = stringArrayResource(Res.array.week_days)
    val repeats = recurrence.days.takeIf { days -> days.isNotEmpty() }?.let { days ->
        stringResource(Res.string.recurrence_with, period, days.sortedBy { day -> day.ordinal }.joinToString(separator = ", ") { day -> weekDays[day.ordinal] })
    } ?: period

    return listOfNotNull(
        repeats,
        recurrence.count?.let { count -> pluralStringResource(Res.plurals.times_count, count, count) },
        recurrence.until?.let { until -> stringResource(Res.string.until_day, calendarDayText(day = CalendarDay.Date(date = until))) },
    ).joinToString(separator = ", ")
}

@Composable
internal fun eventStatusText(status: EventStatus): String =
    stringResource(
        when (status) {
            EventStatus.TENTATIVE -> Res.string.status_tentative
            EventStatus.CONFIRMED -> Res.string.status_confirmed
            EventStatus.CANCELLED -> Res.string.status_cancelled
        },
    )

@Composable
internal fun availabilityText(transparency: EventTransparency): String =
    stringResource(
        when (transparency) {
            EventTransparency.OPAQUE -> Res.string.busy
            EventTransparency.TRANSPARENT -> Res.string.free
        },
    )

@Composable
internal fun classificationText(classification: EventClassification): String =
    stringResource(
        when (classification) {
            EventClassification.PUBLIC -> Res.string.class_public
            EventClassification.PRIVATE -> Res.string.class_private
            EventClassification.CONFIDENTIAL -> Res.string.class_confidential
        },
    )

// In the largest unit it is a whole number of: "1 day before", "2 hours before", "45 minutes before"
@Composable
internal fun reminderText(minutes: Int): String =
    when {
        minutes == 0 -> stringResource(Res.string.at_start)
        minutes % MINUTES_PER_DAY == 0 -> (minutes / MINUTES_PER_DAY).let { days -> pluralStringResource(Res.plurals.days_before, days, days) }
        minutes % MINUTES_PER_HOUR == 0 -> (minutes / MINUTES_PER_HOUR).let { hours -> pluralStringResource(Res.plurals.hours_before, hours, hours) }
        else -> pluralStringResource(Res.plurals.minutes_before, minutes, minutes)
    }
