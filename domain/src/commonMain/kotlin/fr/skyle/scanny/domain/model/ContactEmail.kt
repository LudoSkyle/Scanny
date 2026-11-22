package fr.skyle.scanny.domain.model

/** An e-mail address of a contact, and what it is for. */
data class ContactEmail(
    val address: String,
    val type: ContactEntryType = ContactEntryType.OTHER,
)