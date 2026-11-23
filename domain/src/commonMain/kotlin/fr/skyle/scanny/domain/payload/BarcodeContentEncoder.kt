package fr.skyle.scanny.domain.payload

import fr.skyle.scanny.domain.model.BarcodeContent
import fr.skyle.scanny.domain.model.ContactAddress
import fr.skyle.scanny.domain.model.ContactEntryType
import fr.skyle.scanny.domain.model.ContactFormat
import fr.skyle.scanny.domain.model.ContactPhoneType
import fr.skyle.scanny.domain.model.EventDateTime
import fr.skyle.scanny.domain.model.EventRecurrence
import fr.skyle.scanny.domain.model.WifiEncryption

/**
 * Writes the text to put in a code for a given content, in the format the camera apps of Android and
 * iOS read. The exact inverse of [BarcodeContentParser]: what it writes is read back unchanged.
 */
internal object BarcodeContentEncoder {

    // Characters kept as is in a mailto link, the others are percent-encoded
    private const val URI_UNRESERVED = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-._~"
    private const val MAILTO_ADDRESS_ALLOWED = "$URI_UNRESERVED@+"
    private const val PHONE_NUMBER_ALLOWED = "$URI_UNRESERVED+"
    private const val APPLE_MAPS = "https://maps.apple.com/"

    fun encode(content: BarcodeContent): String =
        when (content) {
            is BarcodeContent.Text -> content.text
            is BarcodeContent.Url -> content.url
            is BarcodeContent.Email -> encodeEmail(email = content)
            is BarcodeContent.Sms -> encodeSms(sms = content)
            is BarcodeContent.Wifi -> encodeWifi(wifi = content)
            is BarcodeContent.Contact -> when (content.format) {
                ContactFormat.VCARD -> encodeContact(contact = content)
                ContactFormat.MECARD -> encodeMecard(contact = content)
            }
            is BarcodeContent.Mms -> encodeMms(mms = content)
            is BarcodeContent.Event -> encodeEvent(event = content)
            is BarcodeContent.Phone -> encodePhone(phone = content)
            is BarcodeContent.Location -> encodeLocation(location = content)
            is BarcodeContent.Bookmark -> encodeBookmark(bookmark = content)
            is BarcodeContent.Otp -> encodeOtp(otp = content)
            is BarcodeContent.Bitcoin -> encodeBitcoin(bitcoin = content)
        }

    /** The mms: link that opens the messages app on this message, its subject on its first line. */
    fun mmsLink(mms: BarcodeContent.Mms): String =
        encodeMms(
            mms = BarcodeContent.Mms(
                phoneNumber = mms.phoneNumber,
                message = listOfNotNull(mms.subject, mms.message)
                    .joinToString(separator = "\n")
                    .ifEmpty { null },
            ),
        )

    /** The geo: link that opens the maps of Android on this place, pinned under its name if any. */
    fun geoLink(location: BarcodeContent.Location): String =
        "${PayloadSyntax.GEO}${location.coordinates()}?q=${location.place()}"

    /** The link of Apple Maps on this place, pinned under its name, or else under its coordinates. */
    fun appleMapsLink(location: BarcodeContent.Location): String {
        val name = location.label?.let { label ->
            percentEncode(
                value = label,
                allowed = URI_UNRESERVED,
            )
        }

        return "$APPLE_MAPS?ll=${location.coordinates()}&q=${name ?: location.coordinates()}"
    }

    /** The sms: link of RFC 5724, which opens the messages app on this message, written. */
    fun smsLink(sms: BarcodeContent.Sms): String =
        buildString {
            append(PayloadSyntax.SMS)
            append(
                percentEncode(
                    value = sms.phoneNumber.orEmpty(),
                    allowed = PHONE_NUMBER_ALLOWED,
                ),
            )
            if (sms.message != null) {
                append("?body=")
                append(
                    percentEncode(
                        value = sms.message,
                        allowed = URI_UNRESERVED,
                    ),
                )
            }
        }

    private fun encodeEmail(email: BarcodeContent.Email): String {
        val parameters = listOfNotNull(
            email.cc.takeIf { cc -> cc.isNotEmpty() }?.let { cc -> "cc=${encodeAddresses(addresses = cc)}" },
            email.bcc.takeIf { bcc -> bcc.isNotEmpty() }?.let { bcc -> "bcc=${encodeAddresses(addresses = bcc)}" },
            email.subject?.let { subject ->
                "subject=${percentEncode(value = subject, allowed = URI_UNRESERVED)}"
            },
            email.body?.let { body ->
                "body=${percentEncode(value = body, allowed = URI_UNRESERVED)}"
            },
        )

        return buildString {
            append(PayloadSyntax.MAILTO)
            append(encodeAddresses(addresses = email.to))
            if (parameters.isNotEmpty()) {
                append('?')
                append(parameters.joinToString(separator = "&"))
            }
        }
    }

    // Each address percent-encoded, the commas between them as is
    private fun encodeAddresses(addresses: List<String>): String =
        addresses.joinToString(separator = ",") { address ->
            percentEncode(
                value = address,
                allowed = MAILTO_ADDRESS_ALLOWED,
            )
        }

    private fun encodeSms(sms: BarcodeContent.Sms): String =
        buildString {
            append(PayloadSyntax.SMSTO)
            append(sms.phoneNumber.orEmpty())
            if (sms.message != null) {
                append(':')
                append(sms.message)
            }
        }

    // The mms: link, as the sms: one of RFC 5724 with a subject: the MMSTO payload has none
    private fun encodeMms(mms: BarcodeContent.Mms): String {
        val parameters = listOfNotNull(
            mms.subject?.let { subject -> "subject=${percentEncode(value = subject, allowed = URI_UNRESERVED)}" },
            mms.message?.let { message -> "body=${percentEncode(value = message, allowed = URI_UNRESERVED)}" },
        )

        return buildString {
            append(PayloadSyntax.MMS)
            append(
                percentEncode(
                    value = mms.phoneNumber.orEmpty(),
                    allowed = PHONE_NUMBER_ALLOWED,
                ),
            )
            if (parameters.isNotEmpty()) {
                append('?')
                append(parameters.joinToString(separator = "&"))
            }
        }
    }

    // In the order of the WPA3 specification: type, modes not to fall back from, SSID, hidden, identifier,
    // password, public key; then the fields of an enterprise network of ZXing. The fields are escaped with
    // a backslash, as ZXing and the cameras of the phones read them, not percent-encoded
    private fun encodeWifi(wifi: BarcodeContent.Wifi): String {
        val type = when (wifi.encryption) {
            WifiEncryption.NONE -> "nopass"
            WifiEncryption.WEP -> "WEP"
            WifiEncryption.WPA -> "WPA"
            WifiEncryption.WPA_ENTERPRISE -> "WPA2-EAP"
        }
        val bitmap = wifi.transitionDisable.sumOf { mode -> 1 shl mode.bit }
        val isEnterprise = wifi.encryption == WifiEncryption.WPA_ENTERPRISE

        return buildString {
            append(PayloadSyntax.WIFI)
            append("T:$type;")
            if (bitmap != 0) {
                append("R:${bitmap.toString(radix = 16).uppercase()};")
            }
            append("S:${escapeField(value = wifi.ssid)};")
            if (wifi.isHidden) {
                append("H:true;")
            }
            val identifier = if (isEnterprise) wifi.identity else wifi.passwordIdentifier
            identifier?.let { append("I:${escapeField(value = identifier)};") }
            if (wifi.encryption != WifiEncryption.NONE && wifi.password != null) {
                append("P:${escapeField(value = wifi.password)};")
            }
            wifi.publicKey?.let { publicKey -> append("K:${escapeField(value = publicKey)};") }
            if (isEnterprise) {
                wifi.eapMethod?.let { eapMethod -> append("E:${escapeField(value = eapMethod)};") }
                wifi.anonymousIdentity?.let { anonymousIdentity -> append("A:${escapeField(value = anonymousIdentity)};") }
                wifi.phase2Method?.let { phase2Method -> append("PH2:${escapeField(value = phase2Method)};") }
            }
            append(';')
        }
    }

    // vCard 3.0. Only the text properties are escaped: a telephone, an e-mail or an URL is written as is.
    // A telephone, an e-mail, an address or a website of no particular kind has no type
    private fun encodeContact(contact: BarcodeContent.Contact): String =
        buildList {
            add("BEGIN:VCARD")
            add("VERSION:3.0")
            val nameParts = listOf(contact.lastName, contact.firstName, contact.middleName, contact.namePrefix, contact.nameSuffix)
            if (nameParts.any { part -> part != null }) {
                add("N:${nameParts.joinToString(separator = ";") { part -> escapeVcard(value = part.orEmpty()) }}")
            }
            contact.formattedName?.let { formattedName ->
                add("FN:${escapeVcard(value = formattedName)}")
            }
            contact.nickname?.let { nickname ->
                add("NICKNAME:${escapeVcard(value = nickname)}")
            }
            if (contact.organization != null || contact.department != null) {
                add("ORG:${escapeVcard(value = contact.organization.orEmpty())}${contact.department?.let { department -> ";${escapeVcard(value = department)}" }.orEmpty()}")
            }
            contact.title?.let { title ->
                add("TITLE:${escapeVcard(value = title)}")
            }
            contact.phones.forEach { phone ->
                add("TEL${vcardType(type = phone.type.vcardType())}:${phone.number}")
            }
            contact.emails.forEach { email ->
                add("EMAIL${vcardType(type = email.type.vcardType())}:${email.address}")
            }
            contact.addresses.forEach { address ->
                val components = listOf(
                    address.poBox,
                    address.extended,
                    address.street,
                    address.city,
                    address.region,
                    address.postalCode,
                    address.country,
                ).joinToString(separator = ";") { component -> escapeVcard(value = component.orEmpty()) }
                add("ADR${vcardType(type = address.type.vcardType())}:$components")
            }
            contact.websites.forEach { website ->
                add("URL${vcardType(type = website.type.vcardType())}:${website.url}")
            }
            contact.birthday?.let { birthday ->
                add("BDAY:$birthday")
            }
            contact.note?.let { note ->
                add("NOTE:${escapeVcard(value = note)}")
            }
            add("END:VCARD")
        }.joinToString(separator = "\n")

    private fun vcardType(type: String?): String =
        type?.let { ";TYPE=$type" }.orEmpty()

    private fun ContactPhoneType.vcardType(): String? =
        when (this) {
            ContactPhoneType.MOBILE -> "CELL"
            ContactPhoneType.HOME -> "HOME"
            ContactPhoneType.WORK -> "WORK"
            ContactPhoneType.FAX -> "FAX"
            ContactPhoneType.OTHER -> null
        }

    private fun ContactEntryType.vcardType(): String? =
        when (this) {
            ContactEntryType.HOME -> "HOME"
            ContactEntryType.WORK -> "WORK"
            ContactEntryType.OTHER -> null
        }

    // The fields of a MeCard: N is "last name,first name", or the whole name. A MeCard has no other part
    // of a name, no department and no kind of telephone, address or website: they are left out. Its
    // organization and its title are not in the format, but the readers read them
    private fun encodeMecard(contact: BarcodeContent.Contact): String =
        buildString {
            append(PayloadSyntax.MECARD)
            if (contact.lastName != null || contact.firstName != null) {
                append("N:${escapeField(value = contact.lastName.orEmpty())},${escapeField(value = contact.firstName.orEmpty())};")
            } else if (contact.formattedName != null) {
                append("N:${escapeField(value = contact.formattedName)};")
            }
            contact.nickname?.let { nickname ->
                append("NICKNAME:${escapeField(value = nickname)};")
            }
            contact.organization?.let { organization ->
                append("ORG:${escapeField(value = organization)};")
            }
            contact.title?.let { title ->
                append("TITLE:${escapeField(value = title)};")
            }
            contact.phones.forEach { phone ->
                append("TEL:${escapeField(value = phone.number)};")
            }
            contact.emails.forEach { email ->
                append("EMAIL:${escapeField(value = email.address)};")
            }
            contact.addresses.forEach { address ->
                append("ADR:${mecardAddress(address = address)};")
            }
            contact.websites.forEach { website ->
                append("URL:${escapeField(value = website.url)};")
            }
            contact.birthday?.let { birthday ->
                append("BDAY:${birthday.filter { char -> char != '-' }};")
            }
            contact.note?.let { note ->
                append("NOTE:${escapeField(value = note)};")
            }
            append(';')
        }

    // "PO box,room,street,city,region,postal code,country"; a street alone is written alone
    private fun mecardAddress(address: ContactAddress): String {
        val components = listOf(
            address.poBox,
            address.extended,
            address.street,
            address.city,
            address.region,
            address.postalCode,
            address.country,
        )
        val isStreetAlone = components.count { component -> component != null } == 1 && address.street != null
        return if (isStreetAlone) {
            escapeField(value = address.street.orEmpty())
        } else {
            components.joinToString(separator = ",") { component -> escapeField(value = component.orEmpty()) }
        }
    }

    private fun encodePhone(phone: BarcodeContent.Phone): String =
        PayloadSyntax.TEL + percentEncode(
            value = phone.phoneNumber,
            allowed = PHONE_NUMBER_ALLOWED,
        )

    // The place goes in the query only to carry the name: a reader takes the coordinates first
    private fun encodeLocation(location: BarcodeContent.Location): String =
        buildString {
            append(PayloadSyntax.GEO)
            append(location.coordinates())
            location.altitude?.let { altitude ->
                append(',')
                append(altitude.toCoordinateText())
            }
            location.uncertainty?.let { uncertainty ->
                append(";u=")
                append(uncertainty.toCoordinateText())
            }
            if (location.label != null) {
                append("?q=")
                append(location.place())
            }
        }

    private fun encodeBookmark(bookmark: BarcodeContent.Bookmark): String =
        buildString {
            append(PayloadSyntax.MEBKM)
            if (bookmark.title != null) {
                append("TITLE:${escapeField(value = bookmark.title)};")
            }
            append("URL:${escapeField(value = bookmark.url)};")
            append(';')
        }

    // The issuer goes in the label and in the parameters: the authenticator apps read one or the other.
    // An issuer without account keeps its colon, or it would be read back as the account
    private fun encodeOtp(otp: BarcodeContent.Otp): String {
        val label = buildString {
            if (otp.issuer != null) {
                append(percentEncode(value = otp.issuer, allowed = URI_UNRESERVED))
                append(':')
            }
            append(percentEncode(value = otp.account.orEmpty(), allowed = URI_UNRESERVED))
        }
        val parameters = listOfNotNull(
            "secret=${percentEncode(value = otp.secret, allowed = URI_UNRESERVED)}",
            otp.issuer?.let { issuer -> "issuer=${percentEncode(value = issuer, allowed = URI_UNRESERVED)}" },
            otp.algorithm?.let { algorithm -> "algorithm=${algorithm.name}" },
            otp.digits?.let { digits -> "digits=$digits" },
            otp.period?.let { period -> "period=$period" },
            otp.counter?.let { counter -> "counter=$counter" },
        )

        return "${PayloadSyntax.OTPAUTH}${otp.type.name.lowercase()}/$label?${parameters.joinToString(separator = "&")}"
    }

    private fun encodeBitcoin(bitcoin: BarcodeContent.Bitcoin): String {
        val parameters = listOfNotNull(
            bitcoin.amount?.let { amount -> "amount=$amount" },
            bitcoin.label?.let { label -> "label=${percentEncode(value = label, allowed = URI_UNRESERVED)}" },
            bitcoin.message?.let { message -> "message=${percentEncode(value = message, allowed = URI_UNRESERVED)}" },
        )

        return buildString {
            append(PayloadSyntax.BITCOIN)
            append(bitcoin.address)
            if (parameters.isNotEmpty()) {
                append('?')
                append(parameters.joinToString(separator = "&"))
            }
        }
    }

    private fun BarcodeContent.Location.coordinates(): String =
        "${latitude.toCoordinateText()},${longitude.toCoordinateText()}"

    // "lat,lon(name)", the name percent-encoded: the place in the query of a geo: link
    private fun BarcodeContent.Location.place(): String =
        buildString {
            append(coordinates())
            if (label != null) {
                append('(')
                append(
                    percentEncode(
                        value = label,
                        allowed = URI_UNRESERVED,
                    ),
                )
                append(')')
            }
        }

    // A VEVENT in its VCALENDAR, lines ending with CRLF as RFC 5545 asks. Neither UID nor DTSTAMP: a code is
    // not an object of a calendar to sync, and they would make two codes of the same event differ
    private fun encodeEvent(event: BarcodeContent.Event): String =
        buildList {
            add(PayloadSyntax.VCALENDAR)
            add("VERSION:2.0")
            add("PRODID:-//Scanny//Scanny//EN")
            add(PayloadSyntax.VEVENT)
            event.summary?.let { summary -> add("SUMMARY:${escapeVcard(value = summary)}") }
            add("DTSTART${eventDateTime(dateTime = event.start)}")
            event.end?.let { end -> add("DTEND${eventDateTime(dateTime = end)}") }
            event.location?.let { location -> add("LOCATION:${escapeVcard(value = location)}") }
            if (event.latitude != null && event.longitude != null) {
                add("GEO:${event.latitude.toCoordinateText()};${event.longitude.toCoordinateText()}")
            }
            event.description?.let { description -> add("DESCRIPTION:${escapeVcard(value = description)}") }
            event.url?.let { url -> add("URL:$url") }
            event.organizer?.let { organizer -> add("ORGANIZER:mailto:$organizer") }
            event.attendees.forEach { attendee -> add("ATTENDEE:mailto:$attendee") }
            event.recurrence?.let { recurrence -> add("RRULE:${recurrenceRule(recurrence = recurrence, start = event.start)}") }
            event.status?.let { status -> add("STATUS:${status.name}") }
            event.transparency?.let { transparency -> add("TRANSP:${transparency.name}") }
            event.classification?.let { classification -> add("CLASS:${classification.name}") }
            event.priority?.let { priority -> add("PRIORITY:$priority") }
            if (event.categories.isNotEmpty()) {
                add("CATEGORIES:${event.categories.joinToString(separator = ",") { category -> escapeVcard(value = category) }}")
            }
            event.reminderMinutes?.let { minutes ->
                // A reminder that shows needs a text: the title of the event
                add("BEGIN:VALARM")
                add("ACTION:DISPLAY")
                add("DESCRIPTION:${escapeVcard(value = event.summary ?: "Reminder")}")
                add("TRIGGER:-PT${minutes}M")
                add("END:VALARM")
            }
            add("END:VEVENT")
            add("END:VCALENDAR")
        }.joinToString(separator = "\r\n")

    // ";VALUE=DATE:20261122" for a whole day, ":20261122T150000Z" in UTC, ";TZID=…:20261122T150000" in a zone,
    // ":20261122T150000" floating
    private fun eventDateTime(dateTime: EventDateTime): String {
        val date = dateTime.date.toString().filter { char -> char != '-' }
        val time = dateTime.time ?: return ";VALUE=DATE:$date"
        val clock = "T${time.hour.twoDigits()}${time.minute.twoDigits()}${time.second.twoDigits()}"
        return when (dateTime.timeZone) {
            null -> ":$date$clock"
            EventDateTime.UTC -> ":$date${clock}Z"
            else -> ";TZID=${dateTime.timeZone}:$date$clock"
        }
    }

    /** The RRULE of an event that repeats: UNTIL is of the type of its start, in UTC at the end of its day for a time. */
    fun recurrenceRule(
        recurrence: EventRecurrence,
        start: EventDateTime,
    ): String =
        listOfNotNull(
            "FREQ=${recurrence.frequency.name}",
            recurrence.interval?.let { interval -> "INTERVAL=$interval" },
            recurrence.count?.let { count -> "COUNT=$count" },
            recurrence.until?.let { until ->
                val date = until.toString().filter { char -> char != '-' }
                if (start.time == null) "UNTIL=$date" else "UNTIL=${date}T235959Z"
            },
            recurrence.days.takeIf { days -> days.isNotEmpty() }?.let { days ->
                "BYDAY=${days.sortedBy { day -> day.ordinal }.joinToString(separator = ",") { day -> day.name.take(2) }}"
            },
        ).joinToString(separator = ";")

    private fun Int.twoDigits(): String =
        toString().padStart(length = 2, padChar = '0')

    private fun escapeField(value: String): String =
        buildString {
            value.forEach { char ->
                if (char in PayloadSyntax.FIELD_SPECIAL_CHARACTERS) {
                    append('\\')
                }
                append(char)
            }
        }

    private fun escapeVcard(value: String): String =
        buildString {
            value.forEach { char ->
                when (char) {
                    '\\', ';', ',' -> {
                        append('\\')
                        append(char)
                    }

                    '\n' -> append("\\n")

                    else -> append(char)
                }
            }
        }

    private fun percentEncode(
        value: String,
        allowed: String,
    ): String =
        buildString {
            value.encodeToByteArray().forEach { byte ->
                val code = byte.toInt() and 0xFF
                val char = code.toChar()
                if (code < 0x80 && char in allowed) {
                    append(char)
                } else {
                    append('%')
                    append(
                        code.toString(radix = 16).uppercase().padStart(
                            length = 2,
                            padChar = '0',
                        ),
                    )
                }
            }
        }
}