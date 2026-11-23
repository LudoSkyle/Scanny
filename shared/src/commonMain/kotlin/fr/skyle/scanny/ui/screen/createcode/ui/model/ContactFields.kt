package fr.skyle.scanny.ui.screen.createcode.ui.model

import fr.skyle.scanny.domain.model.ContactEntryType
import fr.skyle.scanny.domain.model.ContactPhoneType

/** A telephone of a contact in the editor, as typed. */
data class ContactPhoneField(
    val number: String = "",
    val type: ContactPhoneType = ContactPhoneType.MOBILE,
)

/** An e-mail address or a website of a contact in the editor, as typed. */
data class ContactEntryField(
    val value: String = "",
    val type: ContactEntryType = ContactEntryType.OTHER,
)

/** A postal address of a contact in the editor, as typed. */
data class ContactAddressField(
    val street: String = "",
    val extended: String = "",
    val poBox: String = "",
    val postalCode: String = "",
    val city: String = "",
    val region: String = "",
    val country: String = "",
    val type: ContactEntryType = ContactEntryType.HOME,
)