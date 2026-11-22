package fr.skyle.scanny.platform.calendar

import android.content.Context
import android.content.Intent
import android.provider.CalendarContract
import android.provider.CalendarContract.Events
import fr.skyle.scanny.domain.model.BarcodeContent
import fr.skyle.scanny.domain.model.EventClassification
import fr.skyle.scanny.domain.model.EventStatus
import fr.skyle.scanny.domain.model.EventTransparency
import fr.skyle.scanny.domain.payload.toRecurrenceRule
import kotlinx.datetime.TimeZone

internal class AndroidEventAdder(
    private val context: Context,
    private val timeZone: TimeZone,
) : EventAdder {

    override fun addEvent(event: BarcodeContent.Event) {
        context.startActivity(event.toInsertIntent(deviceZone = timeZone).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }
}

/**
 * The screen of the calendar that adds the event, filled with what its extras hold. It has no extra for
 * the organizer, the reminder, the place on the map, the priority nor the categories, and no field for a
 * link, which joins the description. A whole day is at midnight UTC, as the calendar wants it.
 */
internal fun BarcodeContent.Event.toInsertIntent(deviceZone: TimeZone): Intent =
    Intent(Intent.ACTION_INSERT, Events.CONTENT_URI).apply {
        putExtra(Events.TITLE, summary)
        putExtra(Events.DESCRIPTION, listOfNotNull(description, url).joinToString(separator = "\n\n").ifEmpty { null })
        putExtra(Events.EVENT_LOCATION, location)
        putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, start.toInstant(deviceZone = deviceZone, wholeDayZone = TimeZone.UTC).toEpochMilliseconds())
        putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endInstant(deviceZone = deviceZone, wholeDayZone = TimeZone.UTC).toEpochMilliseconds())
        putExtra(CalendarContract.EXTRA_EVENT_ALL_DAY, start.time == null)
        start.timeZone?.let { timeZone -> putExtra(Events.EVENT_TIMEZONE, timeZone) }
        toRecurrenceRule()?.let { rule -> putExtra(Events.RRULE, rule) }
        transparency?.let { transparency ->
            putExtra(
                Events.AVAILABILITY,
                when (transparency) {
                    EventTransparency.OPAQUE -> Events.AVAILABILITY_BUSY
                    EventTransparency.TRANSPARENT -> Events.AVAILABILITY_FREE
                },
            )
        }
        classification?.let { classification ->
            putExtra(
                Events.ACCESS_LEVEL,
                when (classification) {
                    EventClassification.PUBLIC -> Events.ACCESS_PUBLIC
                    EventClassification.PRIVATE -> Events.ACCESS_PRIVATE
                    EventClassification.CONFIDENTIAL -> Events.ACCESS_CONFIDENTIAL
                },
            )
        }
        status?.let { status ->
            putExtra(
                Events.STATUS,
                when (status) {
                    EventStatus.TENTATIVE -> Events.STATUS_TENTATIVE
                    EventStatus.CONFIRMED -> Events.STATUS_CONFIRMED
                    EventStatus.CANCELLED -> Events.STATUS_CANCELED
                },
            )
        }
        if (attendees.isNotEmpty()) {
            putExtra(Intent.EXTRA_EMAIL, attendees.joinToString(separator = ","))
        }
    }
