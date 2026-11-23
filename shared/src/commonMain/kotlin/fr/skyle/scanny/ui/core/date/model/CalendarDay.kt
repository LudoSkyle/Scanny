package fr.skyle.scanny.ui.core.date.model

import kotlinx.datetime.LocalDate

/** A day as the screens name it: today, yesterday, or else its date. */
sealed interface CalendarDay {

    data object Today : CalendarDay

    data object Yesterday : CalendarDay

    data class Date(val date: LocalDate) : CalendarDay
}