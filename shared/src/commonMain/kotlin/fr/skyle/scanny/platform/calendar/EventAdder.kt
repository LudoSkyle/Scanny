package fr.skyle.scanny.platform.calendar

import fr.skyle.scanny.domain.model.BarcodeContent

/** Opens the screen of the system that adds an event to a calendar, filled with the one read in a code. */
interface EventAdder {

    fun addEvent(event: BarcodeContent.Event)
}