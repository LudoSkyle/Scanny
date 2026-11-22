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

    /**
     * The key of a one-time password, as the authenticator apps read it (`otpauth://`). The parameters
     * stay as read, null when the code leaves them to their default: 6 digits, SHA1, every 30 s.
     */
    data class Otp(
        val secret: String,
        val type: OtpType = OtpType.TOTP,
        val issuer: String? = null,
        val account: String? = null,
        val algorithm: String? = null,
        val digits: Int? = null,
        val period: Int? = null,
        val counter: Long? = null,
    ) : BarcodeContent

    /** A Bitcoin payment request of BIP 21, its amount in bitcoins as written. */
    data class Bitcoin(
        val address: String,
        val amount: String? = null,
        val label: String? = null,
        val message: String? = null,
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