package fr.skyle.scanny.domain.payload

import fr.skyle.scanny.domain.model.BarcodeContent
import fr.skyle.scanny.domain.model.ContactAddress
import fr.skyle.scanny.domain.model.ContactEmail
import fr.skyle.scanny.domain.model.ContactEntryType
import fr.skyle.scanny.domain.model.ContactFormat
import fr.skyle.scanny.domain.model.ContactPhone
import fr.skyle.scanny.domain.model.ContactPhoneType
import fr.skyle.scanny.domain.model.ContactWebsite
import fr.skyle.scanny.domain.model.EventClassification
import fr.skyle.scanny.domain.model.EventDateTime
import fr.skyle.scanny.domain.model.EventFrequency
import fr.skyle.scanny.domain.model.EventRecurrence
import fr.skyle.scanny.domain.model.EventStatus
import fr.skyle.scanny.domain.model.EventTransparency
import fr.skyle.scanny.domain.model.OtpAlgorithm
import fr.skyle.scanny.domain.model.OtpType
import fr.skyle.scanny.domain.model.WifiEncryption
import fr.skyle.scanny.domain.model.WifiTransitionMode
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.seconds

/**
 * Interprets the raw text of a code. Both platforms go through it, and never through the typed
 * results of ML Kit, which iOS has no equivalent of. It never throws: what it does not recognize is
 * a text.
 */
internal object BarcodeContentParser {

    // The place in the query of a geo: link, "lat,lon" and its name between parentheses if any
    private val GEO_PLACE = Regex("""\s*([-+]?[0-9.]+)\s*,\s*([-+]?[0-9.]+)\s*(?:\((.*)\))?\s*""")
    private val LATITUDES = -90.0..90.0
    private const val MAX_EVENT_DAYS = 36_500L
    private val ISO_DURATION = Regex("""P(?:([0-9]+)W)?(?:([0-9]+)D)?(?:T(?:([0-9]+)H)?(?:([0-9]+)M)?(?:([0-9]+)S)?)?""")
    private val WEEK_DAYS = mapOf(
        "MO" to DayOfWeek.MONDAY,
        "TU" to DayOfWeek.TUESDAY,
        "WE" to DayOfWeek.WEDNESDAY,
        "TH" to DayOfWeek.THURSDAY,
        "FR" to DayOfWeek.FRIDAY,
        "SA" to DayOfWeek.SATURDAY,
        "SU" to DayOfWeek.SUNDAY,
    )
    private val BITCOIN_AMOUNT = Regex("""[0-9]+(\.[0-9]+)?""")
    private val LONGITUDES = -180.0..180.0

    fun parse(rawValue: String): BarcodeContent {
        val value = rawValue.trim()

        val content = when {
            value.hasPrefix(PayloadSyntax.WIFI) -> parseWifi(fieldsText = value.drop(PayloadSyntax.WIFI.length))
            value.hasPrefix(PayloadSyntax.MATMSG) -> parseMatmsg(fieldsText = value.drop(PayloadSyntax.MATMSG.length))
            value.hasPrefix(PayloadSyntax.MAILTO) -> parseMailto(uri = value.drop(PayloadSyntax.MAILTO.length))
            value.hasPrefix(PayloadSyntax.SMSTO) -> parseSmsTo(text = value.drop(PayloadSyntax.SMSTO.length))
            value.hasPrefix(PayloadSyntax.SMS) -> parseSmsUri(uri = value.drop(PayloadSyntax.SMS.length))
            value.hasPrefix(PayloadSyntax.MMSTO) -> parseMmsTo(text = value.drop(PayloadSyntax.MMSTO.length))
            value.hasPrefix(PayloadSyntax.MMS) -> parseMmsUri(uri = value.drop(PayloadSyntax.MMS.length))
            value.hasPrefix(PayloadSyntax.MECARD) -> parseMecard(fieldsText = value.drop(PayloadSyntax.MECARD.length))
            value.hasPrefix(PayloadSyntax.VCARD) -> parseVcard(text = value)
            value.hasPrefix(PayloadSyntax.VCALENDAR) || value.hasPrefix(PayloadSyntax.VEVENT) -> parseEvent(text = value)
            value.hasPrefix(PayloadSyntax.TEL) -> parsePhone(uri = value.drop(PayloadSyntax.TEL.length))
            value.hasPrefix(PayloadSyntax.GEO) -> parseGeo(uri = value.drop(PayloadSyntax.GEO.length))
            value.hasPrefix(PayloadSyntax.MEBKM) -> parseBookmark(fieldsText = value.drop(PayloadSyntax.MEBKM.length))
            value.hasPrefix(PayloadSyntax.OTPAUTH) -> parseOtp(uri = value.drop(PayloadSyntax.OTPAUTH.length))
            value.hasPrefix(PayloadSyntax.BITCOIN) -> parseBitcoin(uri = value.drop(PayloadSyntax.BITCOIN.length))
            value.hasPrefix(PayloadSyntax.HTTP) || value.hasPrefix(PayloadSyntax.HTTPS) -> BarcodeContent.Url(url = value)
            else -> null
        }

        return content ?: BarcodeContent.Text(text = rawValue)
    }

    private fun parseWifi(fieldsText: String): BarcodeContent.Wifi? {
        val fields = parseFields(text = fieldsText)
        val ssid = fields.valueOf(key = "S")?.takeIf { it.isNotEmpty() } ?: return null
        val password = fields.valueOf(key = "P")?.takeIf { it.isNotEmpty() }

        val type = fields.valueOf(key = "T")?.uppercase()
        val encryption = when {
            type == "NOPASS" || type == "" -> WifiEncryption.NONE
            type == "WEP" -> WifiEncryption.WEP
            type?.endsWith("EAP") == true -> WifiEncryption.WPA_ENTERPRISE
            // No type is an open network, unless it gives a password anyway
            type == null -> if (password != null) WifiEncryption.WPA else WifiEncryption.NONE
            else -> WifiEncryption.WPA
        }
        // The bitmap of the modes not to fall back from, in hexadecimal. "SAE" and "WPA3", types older
        // writers give, are the WPA3-Personal only mode the specification writes "T:WPA;R:1"
        val bitmap = fields.valueOf(key = "R")?.trim()?.toIntOrNull(radix = 16) ?: 0
        val transitionDisable = WifiTransitionMode.entries.filter { mode -> bitmap and (1 shl mode.bit) != 0 }.toSet() +
            setOfNotNull(WifiTransitionMode.WPA3_PERSONAL.takeIf { type == "SAE" || type == "WPA3" })
        val isEnterprise = encryption == WifiEncryption.WPA_ENTERPRISE

        return BarcodeContent.Wifi(
            ssid = ssid,
            encryption = encryption,
            password = password.takeUnless { encryption == WifiEncryption.NONE },
            isHidden = fields.valueOf(key = "H").equals(
                other = "true",
                ignoreCase = true,
            ),
            transitionDisable = transitionDisable,
            // "I:" is the identity of an enterprise network for ZXing, the identifier of an SAE password else
            passwordIdentifier = fields.valueOf(key = "I").nullIfBlank().takeUnless { isEnterprise },
            publicKey = fields.valueOf(key = "K").nullIfBlank(),
            eapMethod = fields.valueOf(key = "E").nullIfBlank()?.uppercase().takeIf { isEnterprise },
            identity = fields.valueOf(key = "I").nullIfBlank().takeIf { isEnterprise },
            anonymousIdentity = fields.valueOf(key = "A").nullIfBlank().takeIf { isEnterprise },
            phase2Method = fields.valueOf(key = "PH2").nullIfBlank()?.uppercase().takeIf { isEnterprise },
        )
    }

    private fun parseMatmsg(fieldsText: String): BarcodeContent.Email? {
        val fields = parseFields(text = fieldsText)

        return BarcodeContent.Email(
            to = listOfNotNull(fields.valueOf(key = "TO").nullIfBlank()),
            subject = fields.valueOf(key = "SUB").nullIfBlank(),
            body = fields.valueOf(key = "BODY").nullIfBlank(),
        ).takeUnless { it == BarcodeContent.Email() }
    }

    private fun parseMailto(uri: String): BarcodeContent.Email? {
        val parameters = parseQuery(
            query = uri.substringAfter(
                delimiter = '?',
                missingDelimiterValue = "",
            ),
        )

        // The addresses are separated by commas, in the path as in the "to", "cc" and "bcc" fields
        val addresses = uri.substringBefore('?')
            .split(',')
            .map { address -> percentDecode(value = address) }

        return BarcodeContent.Email(
            to = addresses.mapNotNull { address -> address.trim().nullIfBlank() } + splitAddresses(text = parameters["to"]),
            cc = splitAddresses(text = parameters["cc"]),
            bcc = splitAddresses(text = parameters["bcc"]),
            subject = parameters["subject"].nullIfBlank(),
            body = parameters["body"].nullIfBlank(),
        ).takeUnless { it == BarcodeContent.Email() }
    }

    private fun splitAddresses(text: String?): List<String> =
        text.orEmpty()
            .split(',')
            .mapNotNull { address -> address.trim().nullIfBlank() }

    private fun parseSmsTo(text: String): BarcodeContent.Sms? =
        BarcodeContent.Sms(
            phoneNumber = text.substringBefore(':').nullIfBlank(),
            message = text.substringAfter(
                delimiter = ':',
                missingDelimiterValue = "",
            ).nullIfBlank(),
        ).takeUnless { it == BarcodeContent.Sms() }

    private fun parseSmsUri(uri: String): BarcodeContent.Sms? {
        if ('?' !in uri) {
            return parseSmsTo(text = percentDecode(value = uri))
        }

        val parameters = parseQuery(query = uri.substringAfter('?'))

        return BarcodeContent.Sms(
            phoneNumber = percentDecode(value = uri.substringBefore('?')).nullIfBlank(),
            message = parameters["body"].nullIfBlank(),
        ).takeUnless { it == BarcodeContent.Sms() }
    }

    // "number:message", as an SMSTO payload: no subject
    private fun parseMmsTo(text: String): BarcodeContent.Mms? =
        BarcodeContent.Mms(
            phoneNumber = text.substringBefore(':').nullIfBlank(),
            message = text.substringAfter(
                delimiter = ':',
                missingDelimiterValue = "",
            ).nullIfBlank(),
        ).takeUnless { it == BarcodeContent.Mms() }

    private fun parseMmsUri(uri: String): BarcodeContent.Mms? {
        if ('?' !in uri) {
            return parseMmsTo(text = percentDecode(value = uri))
        }

        val parameters = parseQuery(query = uri.substringAfter('?'))

        return BarcodeContent.Mms(
            phoneNumber = percentDecode(value = uri.substringBefore('?')).nullIfBlank(),
            subject = parameters["subject"].nullIfBlank(),
            message = parameters["body"].nullIfBlank(),
        ).takeUnless { it == BarcodeContent.Mms() }
    }

    private fun parseMecard(fieldsText: String): BarcodeContent.Contact? {
        val fields = parseFields(text = fieldsText)
        // N is "last name,first name", or a whole name without comma: an escaped comma is part of the name
        val nameField = fields.firstOrNull { it.key == "N" }
        val name = nameField?.value.nullIfBlank()
        val nameParts = nameField?.raw?.let { raw -> splitEscaped(text = raw, limit = 2) }

        return BarcodeContent.Contact(
            formattedName = name.takeIf { nameParts?.size == 1 },
            firstName = nameParts?.getOrNull(1).nullIfBlank(),
            lastName = nameParts?.takeIf { it.size == 2 }?.first().nullIfBlank(),
            nickname = fields.valueOf(key = "NICKNAME").nullIfBlank(),
            organization = fields.valueOf(key = "ORG").nullIfBlank(),
            title = fields.valueOf(key = "TITLE").nullIfBlank(),
            // A MeCard has no kind of telephone: a videophone (TEL-AV) is a telephone like the others
            phones = fields.filter { it.key == "TEL" || it.key == "TEL-AV" }
                .mapNotNull { field -> field.value.nullIfBlank()?.let { number -> ContactPhone(number = number) } },
            emails = fields.valuesOf(key = "EMAIL").map { address -> ContactEmail(address = address) },
            addresses = fields.filter { it.key == "ADR" }.mapNotNull { field -> mecardAddress(raw = field.raw) },
            websites = fields.valuesOf(key = "URL").map { url -> ContactWebsite(url = url) },
            birthday = fields.valueOf(key = "BDAY")?.let { birthday -> isoDate(text = birthday) },
            note = fields.valueOf(key = "NOTE").nullIfBlank(),
            format = ContactFormat.MECARD,
        ).takeUnless { it == BarcodeContent.Contact(format = ContactFormat.MECARD) }
    }

    // "PO box,room,street,city,region,postal code,country" in a MeCard; a value without comma is a street
    private fun mecardAddress(raw: String): ContactAddress? {
        val parts = splitEscaped(text = raw).map { part -> part.nullIfBlank() }
        val address = if (parts.size == 1) {
            ContactAddress(street = parts.first())
        } else {
            ContactAddress(
                poBox = parts.getOrNull(0),
                extended = parts.getOrNull(1),
                street = parts.getOrNull(2),
                city = parts.getOrNull(3),
                region = parts.getOrNull(4),
                postalCode = parts.getOrNull(5),
                country = parts.getOrNull(6),
            )
        }
        return address.takeUnless { it == ContactAddress() }
    }

    // A date as a vCard or a MeCard writes it, "1990-05-12" or "19900512" and maybe a time after, in ISO 8601
    private fun isoDate(text: String): String? {
        val digits = text.trim().substringBefore('T').filter { char -> char != '-' }
        if (digits.length != 8 || !digits.all { char -> char.isDigit() }) {
            return null
        }
        return "${digits.take(4)}-${digits.substring(4, 6)}-${digits.takeLast(2)}"
    }

    // Splits at the commas not escaped, at most into [limit] parts, each part unescaped
    private fun splitEscaped(
        text: String,
        limit: Int = Int.MAX_VALUE,
    ): List<String> {
        val parts = mutableListOf(StringBuilder())
        var index = 0
        while (index < text.length) {
            val char = text[index]
            when {
                char == '\\' && index + 1 < text.length -> {
                    parts.last().append(text[index + 1])
                    index++
                }

                char == ',' && parts.size < limit -> parts += StringBuilder()

                else -> parts.last().append(char)
            }
            index++
        }

        return parts.map { part -> part.toString() }
    }

    private fun parseVcard(text: String): BarcodeContent.Contact? {
        var contact = BarcodeContent.Contact()

        unfoldedLines(text = text).forEach { line ->
            val separatorIndex = line.indexOf(':')
            if (separatorIndex <= 0) {
                return@forEach
            }

            // "item1.TEL;TYPE=CELL,VOICE" is the property TEL of the types CELL and VOICE: its group is not needed
            val nameAndParameters = line.take(separatorIndex).split(';')
            val property = nameAndParameters.first().substringAfterLast('.').uppercase()
            val types = vcardTypes(parameters = nameAndParameters.drop(1))
            val value = line.drop(separatorIndex + 1)

            contact = when (property) {
                "FN" -> contact.copy(formattedName = unescapeVcard(value = value).nullIfBlank())
                "N" -> {
                    val components = splitVcardComponents(value = value)
                    contact.copy(
                        lastName = components.getOrNull(0).nullIfBlank(),
                        firstName = components.getOrNull(1).nullIfBlank(),
                        middleName = components.getOrNull(2).nullIfBlank(),
                        namePrefix = components.getOrNull(3).nullIfBlank(),
                        nameSuffix = components.getOrNull(4).nullIfBlank(),
                    )
                }
                "NICKNAME" -> contact.copy(nickname = unescapeVcard(value = value).nullIfBlank())
                // The organization, then its units: the department
                "ORG" -> {
                    val components = splitVcardComponents(value = value)
                    contact.copy(
                        organization = components.first().nullIfBlank(),
                        department = components.drop(1)
                            .filter { it.isNotBlank() }
                            .joinToString(separator = ", ")
                            .nullIfBlank(),
                    )
                }
                "TITLE" -> contact.copy(title = unescapeVcard(value = value).nullIfBlank())
                "TEL" -> contact.copy(
                    phones = contact.phones + listOfNotNull(
                        vcardPhone(value = value)?.let { number -> ContactPhone(number = number, type = phoneType(types = types)) },
                    ),
                )
                "EMAIL" -> contact.copy(
                    emails = contact.emails + listOfNotNull(
                        unescapeVcard(value = value).nullIfBlank()?.let { address -> ContactEmail(address = address, type = entryType(types = types)) },
                    ),
                )
                "ADR" -> contact.copy(addresses = contact.addresses + listOfNotNull(vcardAddress(value = value, types = types)))
                "URL" -> contact.copy(
                    websites = contact.websites + listOfNotNull(
                        unescapeVcard(value = value).nullIfBlank()?.let { url -> ContactWebsite(url = url, type = entryType(types = types)) },
                    ),
                )
                "BDAY" -> contact.copy(birthday = isoDate(text = value))
                "NOTE" -> contact.copy(note = unescapeVcard(value = value).nullIfBlank())
                else -> contact
            }
        }

        return contact.takeUnless { it == BarcodeContent.Contact() }
    }

    // "TYPE=CELL,VOICE", "TYPE=\"cell,voice\"" (vCard 4) or "CELL" alone (vCard 2.1): the types, in capitals
    private fun vcardTypes(parameters: List<String>): Set<String> =
        parameters.flatMap { parameter ->
            val types = if ('=' in parameter) {
                parameter.takeIf { it.substringBefore('=').trim().equals(other = "TYPE", ignoreCase = true) }
                    ?.substringAfter('=')
                    .orEmpty()
            } else {
                parameter
            }
            types.replace("\"", "").split(',').map { type -> type.trim().uppercase() }
        }.toSet()

    private fun phoneType(types: Set<String>): ContactPhoneType =
        when {
            "FAX" in types -> ContactPhoneType.FAX
            "CELL" in types -> ContactPhoneType.MOBILE
            "WORK" in types -> ContactPhoneType.WORK
            "HOME" in types -> ContactPhoneType.HOME
            else -> ContactPhoneType.OTHER
        }

    private fun entryType(types: Set<String>): ContactEntryType =
        when {
            "WORK" in types -> ContactEntryType.WORK
            "HOME" in types -> ContactEntryType.HOME
            else -> ContactEntryType.OTHER
        }

    // "PO box;extended;street;city;region;postal code;country"
    private fun vcardAddress(
        value: String,
        types: Set<String>,
    ): ContactAddress? {
        val components = splitVcardComponents(value = value).map { component -> component.nullIfBlank() }
        val address = ContactAddress(
            poBox = components.getOrNull(0),
            extended = components.getOrNull(1),
            street = components.getOrNull(2),
            city = components.getOrNull(3),
            region = components.getOrNull(4),
            postalCode = components.getOrNull(5),
            country = components.getOrNull(6),
            type = entryType(types = types),
        )
        return address.takeUnless { it.copy(type = ContactEntryType.OTHER) == ContactAddress() }
    }

    private fun parsePhone(uri: String): BarcodeContent.Phone? =
        percentDecode(value = uri).nullIfBlank()?.let { phoneNumber ->
            BarcodeContent.Phone(phoneNumber = phoneNumber)
        }

    // "lat,lon[,alt][;crs=…][;u=…][?q=…]" of RFC 5870. Android puts the place in the query, with its name:
    // "q=lat,lon(name)", or a search around the position: "q=name". A search around 0,0 has no position.
    // The coordinates of another reference system than WGS84 would not be placed on the maps
    private fun parseGeo(uri: String): BarcodeContent.Location? {
        val path = uri.substringBefore('?').split(';')
        val coordinates = path.first().split(',')
        val latitude = coordinates.getOrNull(0)?.trim()?.toDoubleOrNull() ?: return null
        val longitude = coordinates.getOrNull(1)?.trim()?.toDoubleOrNull() ?: return null
        val isAtZero = latitude == 0.0 && longitude == 0.0
        val pathParameters = path.drop(1).associate { parameter ->
            val value = parameter.substringAfter(
                delimiter = '=',
                missingDelimiterValue = "",
            )
            parameter.substringBefore('=').trim().lowercase() to value.trim()
        }
        if (pathParameters["crs"]?.equals(other = "wgs84", ignoreCase = true) == false) {
            return null
        }

        val search = parseQuery(
            query = uri.substringAfter(
                delimiter = '?',
                missingDelimiterValue = "",
            ),
        )["q"].nullIfBlank()
        val place = search?.let { GEO_PLACE.matchEntire(input = search) }
        if (isAtZero && search != null && place == null) {
            return null
        }
        val placeLatitude = place?.groupValues?.get(1)?.toDoubleOrNull()
        val placeLongitude = place?.groupValues?.get(2)?.toDoubleOrNull()

        val location = BarcodeContent.Location(
            latitude = if (isAtZero && placeLatitude != null) placeLatitude else latitude,
            longitude = if (isAtZero && placeLongitude != null) placeLongitude else longitude,
            altitude = coordinates.getOrNull(2)?.trim()?.toDoubleOrNull(),
            uncertainty = pathParameters["u"]?.toDoubleOrNull()?.takeIf { uncertainty -> uncertainty >= 0 },
            label = if (place != null) place.groupValues[3].nullIfBlank() else search,
        )

        return location.takeIf { location.latitude in LATITUDES && location.longitude in LONGITUDES }
    }

    private fun parseBookmark(fieldsText: String): BarcodeContent.Bookmark? {
        val fields = parseFields(text = fieldsText)
        val url = fields.valueOf(key = "URL").nullIfBlank() ?: return null

        return BarcodeContent.Bookmark(
            url = url,
            title = fields.valueOf(key = "TITLE").nullIfBlank(),
        )
    }

    // "type/label?parameters" of the key URI format: the label is "issuer:account", or the account alone
    private fun parseOtp(uri: String): BarcodeContent.Otp? {
        val type = OtpType.entries.firstOrNull { type ->
            type.name.equals(
                other = uri.substringBefore('/'),
                ignoreCase = true,
            )
        } ?: return null
        val parameters = parseQuery(query = uri.substringAfter(delimiter = '?', missingDelimiterValue = ""))
        val secret = parameters["secret"].nullIfBlank() ?: return null
        // An unknown hash would give other codes than the ones the key is for: not taken for SHA1
        val algorithm = parameters["algorithm"].nullIfBlank()?.let { name ->
            OtpAlgorithm.entries.firstOrNull { algorithm -> algorithm.name.equals(other = name, ignoreCase = true) } ?: return null
        }

        // The colon between the issuer and the account may be percent-encoded too, unlike one in the issuer
        val label = uri.substringAfter(delimiter = '/', missingDelimiterValue = "").substringBefore('?')
        val labelParts = if (':' in label) {
            label.split(':', limit = 2).map { part -> percentDecode(value = part) }
        } else {
            percentDecode(value = label).split(':', limit = 2)
        }
        val labelIssuer = labelParts.takeIf { parts -> parts.size == 2 }?.first()?.trim().nullIfBlank()

        return BarcodeContent.Otp(
            secret = secret,
            type = type,
            issuer = parameters["issuer"].nullIfBlank() ?: labelIssuer,
            account = labelParts.last().trim().nullIfBlank(),
            algorithm = algorithm,
            digits = parameters["digits"]?.toIntOrNull(),
            period = parameters["period"]?.toIntOrNull(),
            counter = parameters["counter"]?.toLongOrNull(),
        )
    }

    // "address?amount=…&label=…&message=…" of BIP 21, the amount in bitcoins
    private fun parseBitcoin(uri: String): BarcodeContent.Bitcoin? {
        val address = uri.substringBefore('?').trim().nullIfBlank() ?: return null
        val parameters = parseQuery(query = uri.substringAfter(delimiter = '?', missingDelimiterValue = ""))

        return BarcodeContent.Bitcoin(
            address = address,
            amount = parameters["amount"]?.takeIf { amount -> BITCOIN_AMOUNT.matches(input = amount) },
            label = parameters["label"].nullIfBlank(),
            message = parameters["message"].nullIfBlank(),
        )
    }

    // The first VEVENT, alone or in its VCALENDAR; its VALARM gives the reminder. An event needs its start
    private fun parseEvent(text: String): BarcodeContent.Event? {
        var properties = mapOf<String, EventProperty>()
        val multipleProperties = mutableMapOf<String, MutableList<EventProperty>>()
        var trigger: String? = null
        var isInEvent = false
        var isInAlarm = false

        for (line in unfoldedLines(text = text)) {
            val separatorIndex = line.indexOf(':')
            if (separatorIndex <= 0) {
                continue
            }
            val nameAndParameters = line.take(separatorIndex).split(';')
            val name = nameAndParameters.first().trim().uppercase()
            val value = line.drop(separatorIndex + 1)
            val parameters = nameAndParameters.drop(1).associate { parameter ->
                parameter.substringBefore('=').trim().uppercase() to parameter.substringAfter(delimiter = '=', missingDelimiterValue = "").trim().removeSurrounding("\"")
            }
            when {
                name == "BEGIN" && value.trim().equals(other = "VEVENT", ignoreCase = true) -> isInEvent = true
                name == "END" && value.trim().equals(other = "VEVENT", ignoreCase = true) -> break
                !isInEvent -> Unit
                name == "BEGIN" && value.trim().equals(other = "VALARM", ignoreCase = true) -> isInAlarm = true
                name == "END" && value.trim().equals(other = "VALARM", ignoreCase = true) -> isInAlarm = false
                isInAlarm -> if (name == "TRIGGER" && trigger == null) trigger = value.trim()
                name == "ATTENDEE" || name == "CATEGORIES" -> multipleProperties.getOrPut(name) { mutableListOf() } += EventProperty(value, parameters)
                name !in properties -> properties = properties + (name to EventProperty(value, parameters))
            }
        }

        val start = properties["DTSTART"]?.let { property -> eventDateTime(property = property) } ?: return null
        val end = properties["DTEND"]?.let { property -> eventDateTime(property = property) }
            ?: properties["DURATION"]?.value?.let { duration -> isoDuration(text = duration) }?.let { duration -> start.plus(duration = duration) }
        val geo = properties["GEO"]?.value?.split(';')
        val latitude = geo?.getOrNull(0)?.trim()?.toDoubleOrNull()?.takeIf { it in LATITUDES }
        val longitude = geo?.getOrNull(1)?.trim()?.toDoubleOrNull()?.takeIf { it in LONGITUDES }
        val hasGeo = latitude != null && longitude != null

        return BarcodeContent.Event(
            summary = properties["SUMMARY"]?.text(),
            start = start,
            end = end,
            location = properties["LOCATION"]?.text(),
            latitude = latitude.takeIf { hasGeo },
            longitude = longitude.takeIf { hasGeo },
            description = properties["DESCRIPTION"]?.text(),
            url = properties["URL"]?.value?.trim().nullIfBlank(),
            organizer = properties["ORGANIZER"]?.value?.let { organizer -> calendarAddress(value = organizer) },
            attendees = multipleProperties["ATTENDEE"].orEmpty().mapNotNull { attendee -> calendarAddress(value = attendee.value) },
            recurrence = properties["RRULE"]?.value?.let { rule -> recurrence(rule = rule) },
            // A reminder before the start: "-PT15M". One after it, or at a given time, is left out
            reminderMinutes = trigger?.takeIf { it.startsWith('-') }?.let { reminder -> isoDuration(text = reminder.drop(1)) }?.inWholeMinutes?.toInt(),
            status = properties["STATUS"]?.value?.let { status -> EventStatus.entries.firstOrNull { it.name.equals(other = status.trim(), ignoreCase = true) } },
            transparency = properties["TRANSP"]?.value?.let { transparency ->
                EventTransparency.entries.firstOrNull { it.name.equals(other = transparency.trim(), ignoreCase = true) }
            },
            classification = properties["CLASS"]?.value?.let { classification ->
                EventClassification.entries.firstOrNull { it.name.equals(other = classification.trim(), ignoreCase = true) }
            },
            // 0 is a priority left undefined
            priority = properties["PRIORITY"]?.value?.trim()?.toIntOrNull()?.takeIf { it in 1..9 },
            categories = multipleProperties["CATEGORIES"].orEmpty().flatMap { categories -> splitTextList(value = categories.value) },
        )
    }

    private class EventProperty(
        val value: String,
        val parameters: Map<String, String>,
    ) {
        fun text(): String? =
            unescapeVcard(value = value).nullIfBlank()
    }

    // "20261122T150000Z" in UTC, "20261122T150000" with its TZID or floating, "20261122" for a whole day
    private fun eventDateTime(property: EventProperty): EventDateTime? {
        val value = property.value.trim()
        val date = isoDate(text = value)?.let { date -> localDate(isoDate = date) } ?: return null
        val time = value.substringAfter(delimiter = 'T', missingDelimiterValue = "")
            .removeSuffix("Z")
            .takeIf { it.length >= 4 && it.all { char -> char.isDigit() } }
            ?.let { digits ->
                LocalTime(
                    hour = digits.take(2).toInt().takeIf { it < 24 } ?: return null,
                    minute = digits.substring(2, 4).toInt().takeIf { it < 60 } ?: return null,
                    second = digits.drop(4).take(2).toIntOrNull()?.takeIf { it < 60 } ?: 0,
                )
            }
        val isWholeDay = time == null || property.parameters["VALUE"].equals(other = "DATE", ignoreCase = true)

        return EventDateTime(
            date = date,
            time = time.takeUnless { isWholeDay },
            timeZone = when {
                isWholeDay -> null
                value.endsWith('Z') -> EventDateTime.UTC
                else -> property.parameters["TZID"].nullIfBlank()
            },
        )
    }

    // A duration of ISO 8601 as iCalendar writes it: "P1W", "P1DT2H", "PT90M"
    private fun isoDuration(text: String): Duration? {
        val match = ISO_DURATION.matchEntire(input = text.trim().removePrefix("+")) ?: return null
        val (weeks, days, hours, minutes, seconds) = match.destructured
        return listOf(
            (weeks.toLongOrNull() ?: 0) * 7.days.inWholeSeconds,
            (days.toLongOrNull() ?: 0) * 1.days.inWholeSeconds,
            (hours.toLongOrNull() ?: 0) * 1.hours.inWholeSeconds,
            (minutes.toLongOrNull() ?: 0) * 60,
            seconds.toLongOrNull() ?: 0,
        ).sum().seconds
    }

    // The end an event of a duration reaches: whole days for a whole day, the clock otherwise. A duration of
    // more than a century is not taken: the end would be out of the calendar
    private fun EventDateTime.plus(duration: Duration): EventDateTime? {
        if (duration.inWholeDays > MAX_EVENT_DAYS) {
            return null
        }
        val time = time
        if (time == null) {
            return copy(date = date.plus(duration.inWholeDays.toInt(), DateTimeUnit.DAY))
        }
        val end = LocalDateTime(date, time).toInstant(TimeZone.UTC).plus(duration).toLocalDateTime(TimeZone.UTC)
        return copy(date = end.date, time = end.time)
    }

    // "1990-05-12", a day that exists: February has no 30th, nor a 29th out of a leap year
    private fun localDate(isoDate: String): LocalDate? {
        val (year, month, day) = isoDate.split('-').map { part -> part.toInt() }
        val isLeapYear = year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)
        val monthLength = when (month) {
            2 -> if (isLeapYear) 29 else 28
            4, 6, 9, 11 -> 30
            in 1..12 -> 31
            else -> return null
        }
        if (day !in 1..monthLength) {
            return null
        }
        return LocalDate(year = year, month = month, day = day)
    }

    // "mailto:john@doe.fr", the name of the person (CN) aside
    private fun calendarAddress(value: String): String? =
        value.trim().let { address -> if (address.hasPrefix("mailto:")) address.drop(7) else address }.nullIfBlank()

    // "FREQ=WEEKLY;INTERVAL=2;COUNT=10;BYDAY=MO,WE": a frequency the calendars do not create is left out
    private fun recurrence(rule: String): EventRecurrence? {
        val parts = rule.split(';').associate { part ->
            part.substringBefore('=').trim().uppercase() to part.substringAfter(delimiter = '=', missingDelimiterValue = "").trim()
        }
        val frequency = EventFrequency.entries.firstOrNull { it.name.equals(other = parts["FREQ"], ignoreCase = true) } ?: return null

        return EventRecurrence(
            frequency = frequency,
            interval = parts["INTERVAL"]?.toIntOrNull()?.takeIf { it > 1 },
            count = parts["COUNT"]?.toIntOrNull()?.takeIf { it > 0 },
            until = parts["UNTIL"]?.let { until -> isoDate(text = until) }?.let { date -> localDate(isoDate = date) },
            // A day with a position in the month ("1MO") is left out
            days = parts["BYDAY"].orEmpty().split(',').mapNotNull { day -> WEEK_DAYS[day.trim().uppercase()] }.toSet(),
        )
    }

    // "a,b\,c": the values of a list of texts, an escaped comma in its value
    private fun splitTextList(value: String): List<String> =
        splitEscapedCommas(value = value).mapNotNull { item -> unescapeVcard(value = item).nullIfBlank() }

    private fun splitEscapedCommas(value: String): List<String> {
        val items = mutableListOf(StringBuilder())
        var index = 0
        while (index < value.length) {
            val char = value[index]
            when {
                char == '\\' && index + 1 < value.length -> {
                    items.last().append(char)
                    items.last().append(value[index + 1])
                    index++
                }

                char == ',' -> items += StringBuilder()

                else -> items.last().append(char)
            }
            index++
        }
        return items.map { item -> item.toString() }
    }

    private class Field(
        val key: String,
        val value: String,
        // As written, its escapes kept
        val raw: String,
    )

    // "KEY:value;KEY:value;;", where a backslash escapes the next character and a value may be quoted
    private fun parseFields(text: String): List<Field> {
        val fields = mutableListOf<Field>()
        val current = StringBuilder()
        val raw = StringBuilder()
        var key: String? = null
        var startsWithQuote = false
        var endsWithQuote = false

        fun endField() {
            val fieldKey = key
            if (fieldKey != null) {
                val value = current.toString()
                fields += Field(
                    key = fieldKey.uppercase(),
                    value = if (startsWithQuote && endsWithQuote && value.length >= 2) {
                        value.drop(1).dropLast(1)
                    } else {
                        value
                    },
                    raw = raw.toString(),
                )
            }
            key = null
            current.clear()
            raw.clear()
            startsWithQuote = false
            endsWithQuote = false
        }

        var index = 0
        while (index < text.length) {
            val char = text[index]
            when {
                char == '\\' && index + 1 < text.length -> {
                    current.append(text[index + 1])
                    raw.append(char)
                    raw.append(text[index + 1])
                    endsWithQuote = false
                    index++
                }

                char == ':' && key == null -> {
                    key = current.toString()
                    current.clear()
                    raw.clear()
                }

                char == ';' -> endField()

                else -> {
                    if (key != null && current.isEmpty() && char == '"') {
                        startsWithQuote = true
                    }
                    current.append(char)
                    raw.append(char)
                    endsWithQuote = char == '"'
                }
            }
            index++
        }
        endField()

        return fields
    }

    private fun List<Field>.valueOf(key: String): String? =
        firstOrNull { it.key == key }?.value

    private fun List<Field>.valuesOf(key: String): List<String> =
        filter { it.key == key }
            .map { it.value }
            .filter { it.isNotBlank() }

    private fun unfoldedLines(text: String): List<String> {
        val lines = mutableListOf<String>()

        text.replace(
            oldValue = "\r\n",
            newValue = "\n",
        ).split('\n').forEach { line ->
            // A line starting with a space or a tab continues the previous one
            if ((line.startsWith(' ') || line.startsWith('\t')) && lines.isNotEmpty()) {
                lines[lines.lastIndex] = lines.last() + line.drop(1)
            } else {
                lines += line
            }
        }

        return lines
    }

    private fun splitVcardComponents(value: String): List<String> {
        val components = mutableListOf<String>()
        val current = StringBuilder()

        var index = 0
        while (index < value.length) {
            val char = value[index]
            when {
                char == '\\' && index + 1 < value.length -> {
                    current.append(char)
                    current.append(value[index + 1])
                    index++
                }

                char == ';' -> {
                    components += unescapeVcard(value = current.toString())
                    current.clear()
                }

                else -> current.append(char)
            }
            index++
        }
        components += unescapeVcard(value = current.toString())

        return components
    }

    private fun unescapeVcard(value: String): String =
        buildString {
            var index = 0
            while (index < value.length) {
                val char = value[index]
                if (char == '\\' && index + 1 < value.length) {
                    val escaped = value[index + 1]
                    append(if (escaped == 'n' || escaped == 'N') '\n' else escaped)
                    index += 2
                } else {
                    append(char)
                    index++
                }
            }
        }

    // A vCard 4.0 telephone is a "tel:" URI
    private fun vcardPhone(value: String): String? {
        val phone = unescapeVcard(value = value)
        return if (phone.hasPrefix("tel:")) phone.drop(4).nullIfBlank() else phone.nullIfBlank()
    }

    private fun parseQuery(query: String): Map<String, String> =
        query.split('&')
            .filter { it.isNotEmpty() }
            .associate { parameter ->
                percentDecode(value = parameter.substringBefore('=')).lowercase() to percentDecode(
                    value = parameter.substringAfter(
                        delimiter = '=',
                        missingDelimiterValue = "",
                    ),
                )
            }

    // A malformed sequence (a lone "%", a truncated "%2") stays as typed instead of failing
    private fun percentDecode(value: String): String {
        if ('%' !in value) {
            return value
        }

        val bytes = mutableListOf<Byte>()
        val literal = StringBuilder()

        fun flushLiteral() {
            bytes += literal.toString().encodeToByteArray().toList()
            literal.clear()
        }

        var index = 0
        while (index < value.length) {
            val byte = if (value[index] == '%') value.hexByteAt(index = index + 1) else null
            if (byte != null) {
                flushLiteral()
                bytes += byte
                index += 3
            } else {
                literal.append(value[index])
                index++
            }
        }
        flushLiteral()

        return bytes.toByteArray().decodeToString()
    }

    private fun String.hexByteAt(index: Int): Byte? {
        if (index + 1 >= length) {
            return null
        }

        val high = this[index].digitToIntOrNull(radix = 16) ?: return null
        val low = this[index + 1].digitToIntOrNull(radix = 16) ?: return null

        return (high * 16 + low).toByte()
    }

    private fun String.hasPrefix(prefix: String): Boolean =
        startsWith(
            prefix = prefix,
            ignoreCase = true,
        )

    private fun String?.nullIfBlank(): String? =
        this?.takeUnless { it.isBlank() }
}