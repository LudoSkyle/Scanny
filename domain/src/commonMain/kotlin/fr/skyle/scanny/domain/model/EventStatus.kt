package fr.skyle.scanny.domain.model

/** Whether an event will take place (STATUS of iCalendar). */
enum class EventStatus {
    TENTATIVE,
    CONFIRMED,
    CANCELLED,
}