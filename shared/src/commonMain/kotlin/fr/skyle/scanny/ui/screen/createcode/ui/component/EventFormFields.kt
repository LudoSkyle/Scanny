package fr.skyle.scanny.ui.screen.createcode.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import fr.skyle.scanny.designsystem.core.datefield.SCDateField
import fr.skyle.scanny.designsystem.core.dropdownfield.SCDropdownField
import fr.skyle.scanny.designsystem.core.textfield.SCTextFieldDefaults
import fr.skyle.scanny.designsystem.core.timefield.SCTimeField
import fr.skyle.scanny.designsystem.theme.SCTheme
import fr.skyle.scanny.domain.model.EventClassification
import fr.skyle.scanny.domain.model.EventDateTime
import fr.skyle.scanny.domain.model.EventFrequency
import fr.skyle.scanny.domain.model.EventStatus
import fr.skyle.scanny.domain.model.EventTransparency
import fr.skyle.scanny.platform.keyboard.coordinateKeyboardType
import fr.skyle.scanny.resources.Res
import fr.skyle.scanny.resources.all_day
import fr.skyle.scanny.resources.attendees
import fr.skyle.scanny.resources.availability
import fr.skyle.scanny.resources.categories
import fr.skyle.scanny.resources.classification
import fr.skyle.scanny.resources.description
import fr.skyle.scanny.resources.details
import fr.skyle.scanny.resources.end_date
import fr.skyle.scanny.resources.end_time
import fr.skyle.scanny.resources.event
import fr.skyle.scanny.resources.floating_time
import fr.skyle.scanny.resources.frequency
import fr.skyle.scanny.resources.frequency_daily
import fr.skyle.scanny.resources.frequency_monthly
import fr.skyle.scanny.resources.frequency_weekly
import fr.skyle.scanny.resources.frequency_yearly
import fr.skyle.scanny.resources.latitude
import fr.skyle.scanny.resources.link
import fr.skyle.scanny.resources.longitude
import fr.skyle.scanny.resources.not_set
import fr.skyle.scanny.resources.occurrences
import fr.skyle.scanny.resources.organizer
import fr.skyle.scanny.resources.place
import fr.skyle.scanny.resources.priority
import fr.skyle.scanny.resources.recurrence
import fr.skyle.scanny.resources.recurrence_after
import fr.skyle.scanny.resources.recurrence_end
import fr.skyle.scanny.resources.recurrence_never
import fr.skyle.scanny.resources.recurrence_on
import fr.skyle.scanny.resources.reminder
import fr.skyle.scanny.resources.repeat_interval
import fr.skyle.scanny.resources.repeats_on
import fr.skyle.scanny.resources.required_field
import fr.skyle.scanny.resources.start_date
import fr.skyle.scanny.resources.start_time
import fr.skyle.scanny.resources.status
import fr.skyle.scanny.resources.time_zone
import fr.skyle.scanny.resources.title
import fr.skyle.scanny.resources.until_date
import fr.skyle.scanny.ui.core.code.component.availabilityText
import fr.skyle.scanny.ui.core.code.component.classificationText
import fr.skyle.scanny.ui.core.code.component.eventStatusText
import fr.skyle.scanny.ui.core.code.component.reminderText
import fr.skyle.scanny.ui.core.date.component.calendarDayText
import fr.skyle.scanny.ui.core.date.mapper.toTimeText
import fr.skyle.scanny.ui.core.date.model.CalendarDay
import fr.skyle.scanny.ui.core.preview.AppPreviewWithBackground
import fr.skyle.scanny.ui.screen.createcode.ui.model.CreationForm
import fr.skyle.scanny.ui.screen.createcode.ui.model.CreationKind
import fr.skyle.scanny.ui.screen.createcode.ui.model.RecurrenceEnd
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Instant

// The reminders a calendar offers, in minutes before the start
private val REMINDERS = listOf(0, 5, 10, 15, 30, 60, 120, 1_440, 10_080)
private val PRIORITIES = (1..9).toList()
private const val MINUTES_PER_HOUR = 60

/**
 * The fields of an event, those of a VEVENT of iCalendar: its title, its start and end, its zone, where
 * and what for, who organizes and attends it, how it repeats, when it is reminded, and how a calendar files
 * it. An event of whole days has days, no times and no zone.
 */
@Composable
fun EventFormFields(
    form: CreationForm,
    onFormChanged: (CreationForm) -> Unit,
    modifier: Modifier = Modifier,
) {
    // The zone of the device, the one a new event is in
    val deviceZone = TimeZone.currentSystemDefault().id

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(space = 24.dp),
    ) {
        // An event in a whole day has no time, and so no zone
        CreationSection(
            modifier = Modifier.fillMaxWidth(),
            title = stringResource(Res.string.event),
            moreOptions = if (form.isEventAllDay) {
                null
            } else {
                {
                    // The zone of the device, UTC, a time wherever one is, and the zone of a scanned event
                    val zones = listOf(deviceZone, EventDateTime.UTC, null, form.eventTimeZone).distinct()
                    SCDropdownField(
                        modifier = Modifier.fillMaxWidth(),
                        label = stringResource(Res.string.time_zone),
                        placeholder = stringResource(Res.string.time_zone),
                        options = zones,
                        selectedOption = form.eventTimeZone,
                        optionText = { zone -> zone ?: stringResource(Res.string.floating_time) },
                        onOptionSelected = { zone -> onFormChanged(form.copy(eventTimeZone = zone)) },
                        style = SCTextFieldDefaults.Style.LIGHT,
                    )
                }
            },
            hasMoreOptionsSet = !form.isEventAllDay && form.eventTimeZone != deviceZone,
        ) {
            CreationTextField(
                modifier = Modifier.fillMaxWidth(),
                value = form.eventSummary,
                onValueChange = { summary -> onFormChanged(form.copy(eventSummary = summary)) },
                label = stringResource(Res.string.title),
                isRequired = true,
            )
            CreationSwitchRow(
                modifier = Modifier.fillMaxWidth(),
                label = stringResource(Res.string.all_day),
                isChecked = form.isEventAllDay,
                onCheckedChange = { isAllDay -> onFormChanged(form.copy(isEventAllDay = isAllDay)) },
            )
            DayAndTime(
                dateLabel = stringResource(Res.string.start_date),
                date = form.eventStartDate,
                onDateChange = { date -> onFormChanged(form.copy(eventStartDate = date)) },
                timeLabel = stringResource(Res.string.start_time),
                time = form.eventStartTime,
                onTimeChange = { time -> onFormChanged(form.copy(eventStartTime = time)) },
                isTimeShown = !form.isEventAllDay,
                isRequired = true,
            )
            DayAndTime(
                dateLabel = stringResource(Res.string.end_date),
                date = form.eventEndDate,
                onDateChange = { date -> onFormChanged(form.copy(eventEndDate = date)) },
                timeLabel = stringResource(Res.string.end_time),
                time = form.eventEndTime,
                onTimeChange = { time -> onFormChanged(form.copy(eventEndTime = time)) },
                isTimeShown = !form.isEventAllDay,
                isRequired = false,
            )
            SCDropdownField(
                modifier = Modifier.fillMaxWidth(),
                label = stringResource(Res.string.reminder),
                placeholder = stringResource(Res.string.reminder),
                // The reminder of a scanned event among the usual ones
                options = (listOf(null) + (REMINDERS + listOfNotNull(form.eventReminderMinutes)).distinct().sorted()),
                selectedOption = form.eventReminderMinutes,
                optionText = { minutes -> minutes?.let { reminderText(minutes = minutes) } ?: stringResource(Res.string.not_set) },
                onOptionSelected = { minutes -> onFormChanged(form.copy(eventReminderMinutes = minutes)) },
                style = SCTextFieldDefaults.Style.LIGHT,
            )
        }

        CreationSection(
            modifier = Modifier.fillMaxWidth(),
            title = stringResource(Res.string.place),
            moreOptions = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(space = 16.dp),
                ) {
                    CreationTextField(
                        modifier = Modifier.weight(1f),
                        value = form.eventLatitude,
                        onValueChange = { latitude -> onFormChanged(form.copy(eventLatitude = latitude)) },
                        label = stringResource(Res.string.latitude),
                        keyboardType = coordinateKeyboardType,
                    )
                    CreationTextField(
                        modifier = Modifier.weight(1f),
                        value = form.eventLongitude,
                        onValueChange = { longitude -> onFormChanged(form.copy(eventLongitude = longitude)) },
                        label = stringResource(Res.string.longitude),
                        keyboardType = coordinateKeyboardType,
                    )
                }
            },
            hasMoreOptionsSet = form.eventLatitude.isNotBlank() || form.eventLongitude.isNotBlank(),
        ) {
            CreationTextField(
                modifier = Modifier.fillMaxWidth(),
                value = form.eventLocation,
                onValueChange = { location -> onFormChanged(form.copy(eventLocation = location)) },
                label = stringResource(Res.string.place),
            )
        }

        CreationSection(
            modifier = Modifier.fillMaxWidth(),
            title = stringResource(Res.string.details),
            moreOptions = {
                CreationTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = form.eventOrganizer,
                    onValueChange = { organizer -> onFormChanged(form.copy(eventOrganizer = organizer)) },
                    label = stringResource(Res.string.organizer),
                    keyboardType = KeyboardType.Email,
                )
                // Several addresses, separated by commas
                CreationTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = form.eventAttendees,
                    onValueChange = { attendees -> onFormChanged(form.copy(eventAttendees = attendees)) },
                    label = stringResource(Res.string.attendees),
                    keyboardType = KeyboardType.Email,
                )
                SCDropdownField(
                    modifier = Modifier.fillMaxWidth(),
                    label = stringResource(Res.string.status),
                    placeholder = stringResource(Res.string.status),
                    options = listOf(null) + EventStatus.entries,
                    selectedOption = form.eventStatus,
                    optionText = { status -> status?.let { eventStatusText(status = status) } ?: stringResource(Res.string.not_set) },
                    onOptionSelected = { status -> onFormChanged(form.copy(eventStatus = status)) },
                    style = SCTextFieldDefaults.Style.LIGHT,
                )
                SCDropdownField(
                    modifier = Modifier.fillMaxWidth(),
                    label = stringResource(Res.string.availability),
                    placeholder = stringResource(Res.string.availability),
                    options = listOf(null) + EventTransparency.entries,
                    selectedOption = form.eventTransparency,
                    optionText = { transparency -> transparency?.let { availabilityText(transparency = transparency) } ?: stringResource(Res.string.not_set) },
                    onOptionSelected = { transparency -> onFormChanged(form.copy(eventTransparency = transparency)) },
                    style = SCTextFieldDefaults.Style.LIGHT,
                )
                SCDropdownField(
                    modifier = Modifier.fillMaxWidth(),
                    label = stringResource(Res.string.classification),
                    placeholder = stringResource(Res.string.classification),
                    options = listOf(null) + EventClassification.entries,
                    selectedOption = form.eventClassification,
                    optionText = { classification -> classification?.let { classificationText(classification = classification) } ?: stringResource(Res.string.not_set) },
                    onOptionSelected = { classification -> onFormChanged(form.copy(eventClassification = classification)) },
                    style = SCTextFieldDefaults.Style.LIGHT,
                )
                // From 1, the highest, to 9
                SCDropdownField(
                    modifier = Modifier.fillMaxWidth(),
                    label = stringResource(Res.string.priority),
                    placeholder = stringResource(Res.string.priority),
                    options = listOf(null) + PRIORITIES,
                    selectedOption = form.eventPriority,
                    optionText = { priority -> priority?.toString() ?: stringResource(Res.string.not_set) },
                    onOptionSelected = { priority -> onFormChanged(form.copy(eventPriority = priority)) },
                    style = SCTextFieldDefaults.Style.LIGHT,
                )
                // Several categories, separated by commas
                CreationTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = form.eventCategories,
                    onValueChange = { categories -> onFormChanged(form.copy(eventCategories = categories)) },
                    label = stringResource(Res.string.categories),
                )
            },
            hasMoreOptionsSet = listOf(form.eventOrganizer, form.eventAttendees, form.eventCategories).any { field -> field.isNotBlank() } ||
                listOf(form.eventStatus, form.eventTransparency, form.eventClassification, form.eventPriority).any { field -> field != null },
        ) {
            CreationTextField(
                modifier = Modifier.fillMaxWidth(),
                value = form.eventDescription,
                onValueChange = { description -> onFormChanged(form.copy(eventDescription = description)) },
                label = stringResource(Res.string.description),
                minLines = 3,
            )
            CreationTextField(
                modifier = Modifier.fillMaxWidth(),
                value = form.eventUrl,
                onValueChange = { url -> onFormChanged(form.copy(eventUrl = url)) },
                label = stringResource(Res.string.link),
                keyboardType = KeyboardType.Uri,
            )
        }

        CreationSection(
            modifier = Modifier.fillMaxWidth(),
            title = stringResource(Res.string.recurrence),
        ) {
            SCDropdownField(
                modifier = Modifier.fillMaxWidth(),
                label = stringResource(Res.string.recurrence),
                placeholder = stringResource(Res.string.recurrence),
                options = listOf(null) + EventFrequency.entries,
                selectedOption = form.eventFrequency,
                optionText = { frequency -> frequencyText(frequency = frequency) },
                onOptionSelected = { frequency -> onFormChanged(form.copy(eventFrequency = frequency)) },
                style = SCTextFieldDefaults.Style.LIGHT,
            )
            if (form.eventFrequency != null) {
                CreationTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = form.eventInterval,
                    onValueChange = { interval -> onFormChanged(form.copy(eventInterval = interval)) },
                    label = stringResource(Res.string.repeat_interval),
                    keyboardType = KeyboardType.Number,
                )
                if (form.eventFrequency == EventFrequency.WEEKLY) {
                    WeekDaysChoice(
                        modifier = Modifier.fillMaxWidth(),
                        label = stringResource(Res.string.repeats_on),
                        selectedDays = form.eventDays,
                        onDaysChange = { days -> onFormChanged(form.copy(eventDays = days)) },
                    )
                }
                SegmentedChoice(
                    modifier = Modifier.fillMaxWidth(),
                    label = stringResource(Res.string.recurrence_end),
                    options = RecurrenceEnd.entries,
                    selectedOption = form.eventRecurrenceEnd,
                    optionText = { end ->
                        stringResource(
                            when (end) {
                                RecurrenceEnd.NEVER -> Res.string.recurrence_never
                                RecurrenceEnd.COUNT -> Res.string.recurrence_after
                                RecurrenceEnd.UNTIL -> Res.string.recurrence_on
                            },
                        )
                    },
                    onOptionSelected = { end -> onFormChanged(form.copy(eventRecurrenceEnd = end)) },
                )
                when (form.eventRecurrenceEnd) {
                    RecurrenceEnd.NEVER -> Unit
                    RecurrenceEnd.COUNT -> CreationTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = form.eventCount,
                        onValueChange = { count -> onFormChanged(form.copy(eventCount = count)) },
                        label = stringResource(Res.string.occurrences),
                        isRequired = true,
                        keyboardType = KeyboardType.Number,
                    )
                    RecurrenceEnd.UNTIL -> DateField(
                        modifier = Modifier.fillMaxWidth(),
                        label = stringResource(Res.string.until_date),
                        date = form.eventUntil,
                        onDateChange = { until -> onFormChanged(form.copy(eventUntil = until)) },
                        isRequired = true,
                    )
                }
            }
        }
    }
}

@Composable
private fun frequencyText(frequency: EventFrequency?): String =
    stringResource(
        when (frequency) {
            null -> Res.string.not_set
            EventFrequency.DAILY -> Res.string.frequency_daily
            EventFrequency.WEEKLY -> Res.string.frequency_weekly
            EventFrequency.MONTHLY -> Res.string.frequency_monthly
            EventFrequency.YEARLY -> Res.string.frequency_yearly
        },
    )

// A day, and its time beside it unless the event lasts whole days
@Composable
private fun DayAndTime(
    dateLabel: String,
    date: LocalDate?,
    onDateChange: (LocalDate?) -> Unit,
    timeLabel: String,
    time: LocalTime?,
    onTimeChange: (LocalTime?) -> Unit,
    isTimeShown: Boolean,
    isRequired: Boolean,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(space = 16.dp),
    ) {
        DateField(
            modifier = Modifier.weight(3f),
            label = dateLabel,
            date = date,
            onDateChange = onDateChange,
            isRequired = isRequired,
        )
        if (isTimeShown) {
            SCTimeField(
                modifier = Modifier.weight(2f),
                label = if (isRequired) stringResource(Res.string.required_field, timeLabel) else timeLabel,
                timeText = time?.toTimeText().orEmpty(),
                selectedMinutes = time?.let { time -> time.hour * MINUTES_PER_HOUR + time.minute },
                onTimeSelected = { minutes -> onTimeChange(minutes?.let { LocalTime(hour = minutes / MINUTES_PER_HOUR, minute = minutes % MINUTES_PER_HOUR) }) },
                style = SCTextFieldDefaults.Style.LIGHT,
            )
        }
    }
}

// A day picked in a calendar, shown in the language of the device
@Composable
private fun DateField(
    label: String,
    date: LocalDate?,
    onDateChange: (LocalDate?) -> Unit,
    isRequired: Boolean,
    modifier: Modifier = Modifier,
) {
    SCDateField(
        modifier = modifier,
        label = if (isRequired) stringResource(Res.string.required_field, label) else label,
        dateText = date?.let { day -> calendarDayText(day = CalendarDay.Date(date = day)) }.orEmpty(),
        selectedDateMillis = date?.atStartOfDayIn(TimeZone.UTC)?.toEpochMilliseconds(),
        // The calendar gives the day at midnight UTC
        onDateSelected = { millis -> onDateChange(millis?.let { Instant.fromEpochMilliseconds(millis).toLocalDateTime(TimeZone.UTC).date }) },
        style = SCTextFieldDefaults.Style.LIGHT,
    )
}

@AppPreviewWithBackground
@Composable
private fun PreviewEventFormFields() {
    SCTheme {
        EventFormFields(
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = 16.dp),
            form = CreationForm(
                kind = CreationKind.EVENT,
                eventSummary = "Scanny launch",
                eventStartDate = LocalDate(2026, 11, 22),
                eventStartTime = LocalTime(15, 0),
                eventTimeZone = "Europe/Paris",
                eventFrequency = EventFrequency.WEEKLY,
            ),
            onFormChanged = {},
        )
    }
}
