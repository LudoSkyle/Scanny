package fr.skyle.scanny.ui.core.date.mapper

import fr.skyle.scanny.ui.core.date.model.CalendarDay
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Instant

class DateMapperTest {

    @Test
    fun `a moment is named today yesterday or else by its date`() {
        // Given
        val moments = listOf(
            "2026-11-20T08:00:00Z",
            "2026-11-19T23:59:00Z",
            "2026-11-18T12:00:00Z",
        ).map(Instant::parse)

        // When
        val days = moments.map { moment ->
            moment.toCalendarDay(
                today = TODAY,
                timeZone = TimeZone.UTC,
            )
        }

        // Then
        assertEquals(
            expected = listOf(
                CalendarDay.Today,
                CalendarDay.Yesterday,
                CalendarDay.Date(
                    date = LocalDate(
                        year = 2026,
                        month = 11,
                        day = 18,
                    ),
                ),
            ),
            actual = days,
        )
    }

    @Test
    fun `the day of a moment is the one of the time zone of the device`() {
        // Given
        val lateEveningInUtc = Instant.parse("2026-11-19T23:30:00Z")

        // When
        val day = lateEveningInUtc.toCalendarDay(
            today = TODAY,
            timeZone = TimeZone.of("Europe/Paris"),
        )

        // Then
        assertEquals(
            expected = CalendarDay.Today,
            actual = day,
        )
    }

    @Test
    fun `the time is written on 24 hours with two digits each`() {
        // Given
        val moments = listOf(
            "2026-11-20T09:05:00Z",
            "2026-11-20T21:48:59Z",
        ).map(Instant::parse)

        // When
        val times = moments.map { moment ->
            moment.toTimeText(timeZone = TimeZone.UTC)
        }

        // Then
        assertEquals(
            expected = listOf(
                "09:05",
                "21:48",
            ),
            actual = times,
        )
    }

    private companion object {
        val TODAY = LocalDate(
            year = 2026,
            month = 11,
            day = 20,
        )
    }
}