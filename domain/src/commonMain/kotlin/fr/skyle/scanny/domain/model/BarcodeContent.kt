package fr.skyle.scanny.domain.model

/** What a code contains, interpreted from its raw text the same way on Android and iOS. */
sealed interface BarcodeContent {

    data class Text(
        val text: String,
    ) : BarcodeContent

    data class Url(
        val url: String,
    ) : BarcodeContent

    /** An e-mail of RFC 6068: its recipients, in copy and in blind copy, its subject and its message. */
    data class Email(
        val to: List<String> = emptyList(),
        val cc: List<String> = emptyList(),
        val bcc: List<String> = emptyList(),
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

    /**
     * A place on the map of RFC 5870, in decimal degrees: its altitude and its uncertainty in meters, and
     * the name the code gives it, if any.
     */
    data class Location(
        val latitude: Double,
        val longitude: Double,
        val altitude: Double? = null,
        val uncertainty: Double? = null,
        val label: String? = null,
    ) : BarcodeContent

    /**
     * An event of iCalendar (VEVENT of RFC 5545): its title, when it starts and ends, where, what for, who
     * organizes and attends it, how it repeats, when it is reminded, and how a calendar files it. The
     * priority goes from 1, the highest, to 9.
     */
    data class Event(
        val summary: String? = null,
        val start: EventDateTime,
        val end: EventDateTime? = null,
        val location: String? = null,
        val latitude: Double? = null,
        val longitude: Double? = null,
        val description: String? = null,
        val url: String? = null,
        val organizer: String? = null,
        val attendees: List<String> = emptyList(),
        val recurrence: EventRecurrence? = null,
        // Minutes before the start
        val reminderMinutes: Int? = null,
        val status: EventStatus? = null,
        val transparency: EventTransparency? = null,
        val classification: EventClassification? = null,
        val priority: Int? = null,
        val categories: List<String> = emptyList(),
    ) : BarcodeContent

    /** A link saved with its title, as a bookmark of a browser. */
    data class Bookmark(
        val url: String,
        val title: String? = null,
    ) : BarcodeContent

    /**
     * The key of a one-time password, as the authenticator apps read it (`otpauth://`, the key URI format).
     * The parameters stay as read, null when the code leaves them to their default.
     */
    data class Otp(
        val secret: String,
        val type: OtpType = OtpType.TOTP,
        val issuer: String? = null,
        val account: String? = null,
        val algorithm: OtpAlgorithm? = null,
        val digits: Int? = null,
        // Of a TOTP key only, in seconds
        val period: Int? = null,
        // Of a HOTP key only, which needs it
        val counter: Long? = null,
    ) : BarcodeContent {

        companion object {
            val DEFAULT_ALGORITHM = OtpAlgorithm.SHA1
            const val DEFAULT_DIGITS = 6
            const val DEFAULT_PERIOD = 30
        }
    }

    /** A Bitcoin payment request of BIP 21, its amount in bitcoins as written. */
    data class Bitcoin(
        val address: String,
        val amount: String? = null,
        val label: String? = null,
        val message: String? = null,
    ) : BarcodeContent

    /** A multimedia message: an SMS that has a subject. */
    data class Mms(
        val phoneNumber: String? = null,
        val subject: String? = null,
        val message: String? = null,
    ) : BarcodeContent

    /**
     * A Wi-Fi network, with the fields of the "WIFI:" format of the WPA3 specification (the modes it does
     * not fall back from, the identifier of an SAE password, the public key of SAE-PK) and, for an
     * enterprise network, those of ZXing (the method of EAP, the identities, the phase 2).
     */
    data class Wifi(
        val ssid: String,
        val encryption: WifiEncryption,
        val password: String? = null,
        val isHidden: Boolean = false,
        val transitionDisable: Set<WifiTransitionMode> = emptySet(),
        val passwordIdentifier: String? = null,
        val publicKey: String? = null,
        val eapMethod: String? = null,
        val identity: String? = null,
        val anonymousIdentity: String? = null,
        val phase2Method: String? = null,
    ) : BarcodeContent

    /**
     * A contact, with the fields of a vCard: its name in parts and as shown, its organization, its
     * telephones, e-mail addresses, postal addresses and websites, each with what it is for, its
     * birthday and a note.
     */
    data class Contact(
        val formattedName: String? = null,
        val namePrefix: String? = null,
        val firstName: String? = null,
        val middleName: String? = null,
        val lastName: String? = null,
        val nameSuffix: String? = null,
        val nickname: String? = null,
        val organization: String? = null,
        val department: String? = null,
        val title: String? = null,
        val phones: List<ContactPhone> = emptyList(),
        val emails: List<ContactEmail> = emptyList(),
        val addresses: List<ContactAddress> = emptyList(),
        val websites: List<ContactWebsite> = emptyList(),
        // In ISO 8601: 1990-05-12
        val birthday: String? = null,
        val note: String? = null,
        // The format the contact is written in: the one it was read in, for an edited contact to keep it
        val format: ContactFormat = ContactFormat.VCARD,
    ) : BarcodeContent {

        /** The name to show: the formatted one, or else the parts of the name. */
        val displayName: String?
            get() = formattedName ?: listOfNotNull(namePrefix, firstName, middleName, lastName, nameSuffix)
                .joinToString(separator = " ")
                .ifEmpty { null }
    }
}