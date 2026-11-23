package fr.skyle.scanny.ui.screen.createcode.ui.model

/** When an event the editor repeats stops: never, after a number of times, or on a last day. */
enum class RecurrenceEnd {
    NEVER,
    COUNT,
    UNTIL,
}