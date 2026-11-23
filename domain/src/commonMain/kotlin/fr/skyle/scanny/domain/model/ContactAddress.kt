package fr.skyle.scanny.domain.model

/**
 * A postal address of a contact, in the parts of a vCard (and in their order): post office box,
 * extended address (an apartment, a building), street, city, region, postal code, country.
 */
data class ContactAddress(
    val poBox: String? = null,
    val extended: String? = null,
    val street: String? = null,
    val city: String? = null,
    val region: String? = null,
    val postalCode: String? = null,
    val country: String? = null,
    val type: ContactEntryType = ContactEntryType.OTHER,
)