package fr.skyle.scanny.ui.screen.history.ui.model

import fr.skyle.scanny.ui.core.date.model.CalendarDay

/** What a section of the history gathers: the favorites, at the top, or the others of a day. */
sealed interface HistoryHeader {

    data object Favorites : HistoryHeader

    data class Day(val day: CalendarDay) : HistoryHeader
}
