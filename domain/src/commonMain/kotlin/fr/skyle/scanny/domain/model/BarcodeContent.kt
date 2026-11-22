package fr.skyle.scanny.domain.model

/** What a code contains, interpreted from its raw text the same way on Android and iOS. */
sealed interface BarcodeContent {

    data class Text(
        val text: String,
    ) : BarcodeContent

    data class Url(
        val url: String,
    ) : BarcodeContent

    data class Email(
        val address: String? = null,
        val subject: String? = null,
        val body: String? = null,
    ) : BarcodeContent

    data class Sms(
        val phoneNumber: String? = null,
        val message: String? = null,
    ) : BarcodeContent

    data class Phone(
        val phoneNumber: String,
    ) : BarcodeContent

    /** A place on the map, in decimal degrees, with the name the code gives it if any. */
    data class Location(
        val latitude: Double,
        val longitude: Double,
        val label: String? = null,
    ) : BarcodeContent

    /** A link saved with its title, as a bookmark of a browser. */
    data class Bookmark(
        val url: String,
        val title: String? = null,
    ) : BarcodeContent

    data class Wifi(
        val ssid: String,
        val encryption: WifiEncryption,
        val password: String? = null,
        val isHidden: Boolean = false,
    ) : BarcodeContent

    data class Contact(
        val formattedName: String? = null,
        val firstName: String? = null,
        val lastName: String? = null,
        val organization: String? = null,
        val title: String? = null,
        val phones: List<String> = emptyList(),
        val emails: List<String> = emptyList(),
        val addresses: List<String> = emptyList(),
        val urls: List<String> = emptyList(),
    ) : BarcodeContent {

        /** The name to show: the formatted one, or else the first and last names. */
        val displayName: String?
            get() = formattedName ?: listOfNotNull(firstName, lastName)
                .joinToString(separator = " ")
                .ifEmpty { null }
    }
}