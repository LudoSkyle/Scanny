package fr.skyle.scanny.platform.contact

import fr.skyle.scanny.domain.model.BarcodeContent

/** Opens the screen of the system that adds a contact, filled with the one read in a code. */
interface ContactAdder {

    fun addContact(contact: BarcodeContent.Contact)
}