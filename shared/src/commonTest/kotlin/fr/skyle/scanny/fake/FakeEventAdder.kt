package fr.skyle.scanny.fake

import fr.skyle.scanny.domain.model.BarcodeContent
import fr.skyle.scanny.platform.calendar.EventAdder

internal class FakeEventAdder(
    private val error: Exception? = null,
) : EventAdder {

    val addedEvents = mutableListOf<BarcodeContent.Event>()

    override fun addEvent(event: BarcodeContent.Event) {
        error?.let { throw it }

        addedEvents += event
    }
}