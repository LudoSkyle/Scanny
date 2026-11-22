package fr.skyle.scanny.platform.calendar

import fr.skyle.scanny.domain.model.BarcodeContent
import fr.skyle.scanny.domain.model.EventFrequency
import fr.skyle.scanny.domain.model.EventTransparency
import fr.skyle.scanny.platform.ui.topViewController
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import platform.CoreLocation.CLLocation
import platform.EventKit.EKAlarm
import platform.EventKit.EKEvent
import platform.EventKit.EKEventAvailabilityBusy
import platform.EventKit.EKEventAvailabilityFree
import platform.EventKit.EKEventStore
import platform.EventKit.EKRecurrenceDayOfWeek
import platform.EventKit.EKRecurrenceEnd
import platform.EventKit.EKRecurrenceFrequency
import platform.EventKit.EKRecurrenceRule
import platform.EventKit.EKStructuredLocation
import platform.EventKit.EKWeekdayFriday
import platform.EventKit.EKWeekdayMonday
import platform.EventKit.EKWeekdaySaturday
import platform.EventKit.EKWeekdaySunday
import platform.EventKit.EKWeekdayThursday
import platform.EventKit.EKWeekdayTuesday
import platform.EventKit.EKWeekdayWednesday
import platform.EventKitUI.EKEventEditViewAction
import platform.EventKitUI.EKEventEditViewController
import platform.EventKitUI.EKEventEditViewDelegateProtocol
import platform.Foundation.NSDate
import platform.Foundation.NSTimeZone
import platform.Foundation.NSURL
import platform.Foundation.dateWithTimeIntervalSince1970
import platform.Foundation.timeZoneWithName
import platform.darwin.NSObject
import kotlin.time.Instant

private const val SECONDS_PER_MINUTE = 60

internal class IosEventAdder(
    private val timeZone: TimeZone,
) : EventAdder {

    // Kept here: the view controller only holds its delegate weakly
    private val delegate = EventEditDelegate()
    private val eventStore = EKEventStore()

    // Since iOS 17, the screen that edits an event adds it without the access to the calendars
    override fun addEvent(event: BarcodeContent.Event) {
        val controller = EKEventEditViewController()
        controller.eventStore = eventStore
        controller.event = event.toEKEvent(store = eventStore, deviceZone = timeZone)
        controller.editViewDelegate = delegate

        topViewController()?.presentViewController(
            viewControllerToPresent = controller,
            animated = true,
            completion = null,
        )
    }
}

private class EventEditDelegate :
    NSObject(),
    EKEventEditViewDelegateProtocol {

    // Called whether the event was added or not: the screen closes either way
    override fun eventEditViewController(
        controller: EKEventEditViewController,
        didCompleteWithAction: EKEventEditViewAction,
    ) {
        controller.dismissViewControllerAnimated(
            flag = true,
            completion = null,
        )
    }
}

/**
 * The event as Calendar holds it: a whole day at its local midnight, a floating time with no zone. Calendar
 * cannot be given the attendees, the organizer, the status, the visibility, the priority nor the categories.
 */
private fun BarcodeContent.Event.toEKEvent(
    store: EKEventStore,
    deviceZone: TimeZone,
): EKEvent {
    // Named: in the block, location and description would be those of the event of EventKit
    val content = this
    return EKEvent.eventWithEventStore(store).apply {
        setTitle(content.summary)
        setLocation(content.location)
        setNotes(content.description)
        content.url?.let { url -> setURL(NSURL.URLWithString(url)) }
        setAllDay(content.start.time == null)
        setStartDate(content.start.toInstant(deviceZone = deviceZone, wholeDayZone = deviceZone).toNSDate())
        setEndDate(content.endInstant(deviceZone = deviceZone, wholeDayZone = deviceZone).toNSDate())
        setTimeZone(content.start.timeZone?.let { timeZone -> NSTimeZone.timeZoneWithName(timeZone) })
        content.transparency?.let { transparency ->
            setAvailability(
                when (transparency) {
                    EventTransparency.OPAQUE -> EKEventAvailabilityBusy
                    EventTransparency.TRANSPARENT -> EKEventAvailabilityFree
                },
            )
        }
        val latitude = content.latitude
        val longitude = content.longitude
        if (latitude != null && longitude != null) {
            setStructuredLocation(
                EKStructuredLocation.locationWithTitle(content.location ?: content.summary.orEmpty()).apply {
                    setGeoLocation(CLLocation(latitude = latitude, longitude = longitude))
                },
            )
        }
        content.recurrence?.let { recurrence ->
            val end = recurrence.count?.let { count -> EKRecurrenceEnd.recurrenceEndWithOccurrenceCount(count.toULong()) }
                ?: recurrence.until?.let { until -> EKRecurrenceEnd.recurrenceEndWithEndDate(until.atStartOfDayIn(deviceZone).toNSDate()) }
            setRecurrenceRules(
                listOf(
                    EKRecurrenceRule(
                        recurrenceWithFrequency = when (recurrence.frequency) {
                            EventFrequency.DAILY -> EKRecurrenceFrequency.EKRecurrenceFrequencyDaily
                            EventFrequency.WEEKLY -> EKRecurrenceFrequency.EKRecurrenceFrequencyWeekly
                            EventFrequency.MONTHLY -> EKRecurrenceFrequency.EKRecurrenceFrequencyMonthly
                            EventFrequency.YEARLY -> EKRecurrenceFrequency.EKRecurrenceFrequencyYearly
                        },
                        interval = (recurrence.interval ?: 1).toLong(),
                        daysOfTheWeek = recurrence.days.takeIf { days -> days.isNotEmpty() }?.map { day ->
                            EKRecurrenceDayOfWeek.dayOfWeek(day.toEKWeekday())
                        },
                        daysOfTheMonth = null,
                        monthsOfTheYear = null,
                        weeksOfTheYear = null,
                        daysOfTheYear = null,
                        setPositions = null,
                        end = end,
                    ),
                ),
            )
        }
        content.reminderMinutes?.let { minutes ->
            setAlarms(listOf(EKAlarm.alarmWithRelativeOffset(-(minutes * SECONDS_PER_MINUTE).toDouble())))
        }
    }
}

private fun DayOfWeek.toEKWeekday() =
    when (this) {
        DayOfWeek.MONDAY -> EKWeekdayMonday
        DayOfWeek.TUESDAY -> EKWeekdayTuesday
        DayOfWeek.WEDNESDAY -> EKWeekdayWednesday
        DayOfWeek.THURSDAY -> EKWeekdayThursday
        DayOfWeek.FRIDAY -> EKWeekdayFriday
        DayOfWeek.SATURDAY -> EKWeekdaySaturday
        DayOfWeek.SUNDAY -> EKWeekdaySunday
    }

private fun Instant.toNSDate(): NSDate =
    NSDate.dateWithTimeIntervalSince1970(toEpochMilliseconds() / 1_000.0)
