package fr.skyle.scanny.domain.model

import kotlin.time.Duration
import kotlin.time.Duration.Companion.days

/** How long a capture stays in the history. */
enum class HistoryRetention(
    // As stored by the settings: 0 for forever
    internal val days: Int,
) {
    ONE_WEEK(days = 7),
    ONE_MONTH(days = 30),
    THREE_MONTHS(days = 90),
    ONE_YEAR(days = 365),
    FOREVER(days = 0);

    // Null when the capture stays forever
    val duration: Duration?
        get() = days.takeIf { days -> days > 0 }?.days

    internal companion object {

        // A number of days no retention has falls back to the default of the settings
        fun fromDays(days: Int): HistoryRetention =
            entries.firstOrNull { retention -> retention.days == days } ?: ONE_MONTH
    }
}