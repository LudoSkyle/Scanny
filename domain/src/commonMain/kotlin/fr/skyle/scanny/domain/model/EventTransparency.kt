package fr.skyle.scanny.domain.model

/** Whether an event takes the time it lasts in a calendar (TRANSP of iCalendar): busy, or free. */
enum class EventTransparency {
    OPAQUE,
    TRANSPARENT,
}