package fr.skyle.scanny.fake

import fr.skyle.scanny.domain.model.BarcodeContent
import fr.skyle.scanny.platform.contact.ContactAdder

/** Records the contacts it is given, or fails if [error] is set. */
internal class FakeContactAdder(
    private val error: Exception? = null,
) : ContactAdder {

    val addedContacts = mutableListOf<BarcodeContent.Contact>()

    override fun addContact(contact: BarcodeContent.Contact) {
        error?.let { throw it }

        addedContacts += contact
    }
}