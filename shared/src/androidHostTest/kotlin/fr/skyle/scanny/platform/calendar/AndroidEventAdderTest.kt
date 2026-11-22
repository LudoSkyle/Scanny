package fr.skyle.scanny.platform.calendar

import android.content.Intent
import android.provider.CalendarContract
import android.provider.CalendarContract.Events
import fr.skyle.scanny.domain.model.BarcodeContent
import fr.skyle.scanny.domain.model.EventClassification
import fr.skyle.scanny.domain.model.EventDateTime
import fr.skyle.scanny.domain.model.EventFrequency
import fr.skyle.scanny.domain.model.EventRecurrence
import fr.skyle.scanny.domain.model.EventStatus
import fr.skyle.scanny.domain.model.EventTransparency
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.Test
import kotlin.test.assertEquals

@RunWith(RobolectricTestRunner::class)
class AndroidEventAdderTest {

    @Test
    fun `the calendar opens on the event with what its extras hold`() {
        // Given
        val event = BarcodeContent.Event(
            summary = "Launch",
            start = EventDateTime(date = LocalDate(2026, 11, 22), time = LocalTime(15, 0), timeZone = "Europe/Paris"),
            end = EventDateTime(date = LocalDate(2026, 11, 22), time = LocalTime(16, 30), timeZone = "Europe/Paris"),
            location = "Opéra",
            description = "Come along",
            url = "https://scanny.app",
            attendees = listOf("jane@scanny.app", "jim@scanny.app"),
            recurrence = EventRecurrence(frequency = EventFrequency.WEEKLY, count = 10),
            status = EventStatus.CANCELLED,
            transparency = EventTransparency.TRANSPARENT,
            classification = EventClassification.PRIVATE,
        )

        // When
        val intent = event.toInsertIntent(deviceZone = TimeZone.UTC)

        // Then
        assertEquals(
            expected = listOf<Any?>(
                Intent.ACTION_INSERT,
                Events.CONTENT_URI,
                "Launch",
                "Come along\n\nhttps://scanny.app",
                "Opéra",
                // 15:00 and 16:30 in Paris, an hour ahead of UTC in November
                1_795_356_000_000L,
                1_795_361_400_000L,
                false,
                "Europe/Paris",
                "FREQ=WEEKLY;COUNT=10",
                Events.STATUS_CANCELED,
                Events.AVAILABILITY_FREE,
                Events.ACCESS_PRIVATE,
                "jane@scanny.app,jim@scanny.app",
            ),
            actual = listOf<Any?>(
                intent.action,
                intent.data,
                intent.getStringExtra(Events.TITLE),
                intent.getStringExtra(Events.DESCRIPTION),
                intent.getStringExtra(Events.EVENT_LOCATION),
                intent.getLongExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, 0),
                intent.getLongExtra(CalendarContract.EXTRA_EVENT_END_TIME, 0),
                intent.getBooleanExtra(CalendarContract.EXTRA_EVENT_ALL_DAY, true),
                intent.getStringExtra(Events.EVENT_TIMEZONE),
                intent.getStringExtra(Events.RRULE),
                intent.getIntExtra(Events.STATUS, -1),
                intent.getIntExtra(Events.AVAILABILITY, -1),
                intent.getIntExtra(Events.ACCESS_LEVEL, -1),
                intent.getStringExtra(Intent.EXTRA_EMAIL),
            ),
        )
    }

    @Test
    fun `an event of whole days without end lasts its day from midnight utc and a floating one is in the device zone`() {
        // Given
        val wholeDay = BarcodeContent.Event(start = EventDateTime(date = LocalDate(2026, 12, 24)))
        val floating = BarcodeContent.Event(start = EventDateTime(date = LocalDate(2026, 12, 24), time = LocalTime(9, 0)))

        // When
        val wholeDayIntent = wholeDay.toInsertIntent(deviceZone = TimeZone.of("Europe/Paris"))
        val floatingIntent = floating.toInsertIntent(deviceZone = TimeZone.of("Europe/Paris"))

        // Then
        assertEquals(
            expected = listOf<Any?>(true, 1_798_070_400_000L, 1_798_156_800_000L, 1_798_099_200_000L, 1_798_102_800_000L),
            actual = listOf<Any?>(
                wholeDayIntent.getBooleanExtra(CalendarContract.EXTRA_EVENT_ALL_DAY, false),
                wholeDayIntent.getLongExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, 0),
                wholeDayIntent.getLongExtra(CalendarContract.EXTRA_EVENT_END_TIME, 0),
                floatingIntent.getLongExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, 0),
                floatingIntent.getLongExtra(CalendarContract.EXTRA_EVENT_END_TIME, 0),
            ),
        )
    }
}
