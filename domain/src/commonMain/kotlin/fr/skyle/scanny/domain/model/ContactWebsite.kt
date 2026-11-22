package fr.skyle.scanny.domain.model

/** A website of a contact, and what it is for. */
data class ContactWebsite(
    val url: String,
    val type: ContactEntryType = ContactEntryType.OTHER,
)