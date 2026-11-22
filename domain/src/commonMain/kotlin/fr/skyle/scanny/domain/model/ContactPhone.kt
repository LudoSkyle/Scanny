package fr.skyle.scanny.domain.model

/** A telephone of a contact, and what it is for. */
data class ContactPhone(
    val number: String,
    val type: ContactPhoneType = ContactPhoneType.OTHER,
)