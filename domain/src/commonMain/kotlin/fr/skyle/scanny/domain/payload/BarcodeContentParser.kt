package fr.skyle.scanny.domain.payload

import fr.skyle.scanny.domain.model.BarcodeContent
import fr.skyle.scanny.domain.model.WifiEncryption

/**
 * Interprets the raw text of a code. Both platforms go through it, and never through the typed
 * results of ML Kit, which iOS has no equivalent of. It never throws: what it does not recognize is
 * a text.
 */
internal object BarcodeContentParser {

    // The place in the query of a geo: link, "lat,lon" and its name between parentheses if any
    private val GEO_PLACE = Regex("""\s*([-+]?[0-9.]+)\s*,\s*([-+]?[0-9.]+)\s*(?:\((.*)\))?\s*""")
    private val LATITUDES = -90.0..90.0
    private val LONGITUDES = -180.0..180.0

    fun parse(rawValue: String): BarcodeContent {
        val value = rawValue.trim()

        val content = when {
            value.hasPrefix(PayloadSyntax.WIFI) -> parseWifi(fieldsText = value.drop(PayloadSyntax.WIFI.length))
            value.hasPrefix(PayloadSyntax.MATMSG) -> parseMatmsg(fieldsText = value.drop(PayloadSyntax.MATMSG.length))
            value.hasPrefix(PayloadSyntax.MAILTO) -> parseMailto(uri = value.drop(PayloadSyntax.MAILTO.length))
            value.hasPrefix(PayloadSyntax.SMSTO) -> parseSmsTo(text = value.drop(PayloadSyntax.SMSTO.length))
            value.hasPrefix(PayloadSyntax.SMS) -> parseSmsUri(uri = value.drop(PayloadSyntax.SMS.length))
            value.hasPrefix(PayloadSyntax.MECARD) -> parseMecard(fieldsText = value.drop(PayloadSyntax.MECARD.length))
            value.hasPrefix(PayloadSyntax.VCARD) -> parseVcard(text = value)
            value.hasPrefix(PayloadSyntax.TEL) -> parsePhone(uri = value.drop(PayloadSyntax.TEL.length))
            value.hasPrefix(PayloadSyntax.GEO) -> parseGeo(uri = value.drop(PayloadSyntax.GEO.length))
            value.hasPrefix(PayloadSyntax.MEBKM) -> parseBookmark(fieldsText = value.drop(PayloadSyntax.MEBKM.length))
            value.hasPrefix(PayloadSyntax.HTTP) || value.hasPrefix(PayloadSyntax.HTTPS) -> BarcodeContent.Url(url = value)
            else -> null
        }

        return content ?: BarcodeContent.Text(text = rawValue)
    }

    private fun parseWifi(fieldsText: String): BarcodeContent.Wifi? {
        val fields = parseFields(text = fieldsText)
        val ssid = fields.valueOf(key = "S")?.takeIf { it.isNotEmpty() } ?: return null
        val password = fields.valueOf(key = "P")?.takeIf { it.isNotEmpty() }

        val encryption = when (fields.valueOf(key = "T")?.uppercase()) {
            "NOPASS", "" -> WifiEncryption.NONE
            "WEP" -> WifiEncryption.WEP
            "SAE", "WPA3" -> WifiEncryption.WPA3
            // No type is an open network, unless it gives a password anyway
            null -> if (password != null) WifiEncryption.WPA else WifiEncryption.NONE
            else -> WifiEncryption.WPA
        }

        return BarcodeContent.Wifi(
            ssid = ssid,
            encryption = encryption,
            password = password.takeUnless { encryption == WifiEncryption.NONE },
            isHidden = fields.valueOf(key = "H").equals(
                other = "true",
                ignoreCase = true,
            ),
        )
    }

    private fun parseMatmsg(fieldsText: String): BarcodeContent.Email? {
        val fields = parseFields(text = fieldsText)

        return BarcodeContent.Email(
            address = fields.valueOf(key = "TO").nullIfBlank(),
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

        return BarcodeContent.Email(
            address = percentDecode(value = uri.substringBefore('?')).nullIfBlank(),
            subject = parameters["subject"].nullIfBlank(),
            body = parameters["body"].nullIfBlank(),
        ).takeUnless { it == BarcodeContent.Email() }
    }

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

    private fun parseMecard(fieldsText: String): BarcodeContent.Contact? {
        val fields = parseFields(text = fieldsText)
        // N is "last name,first name", or a whole name without comma
        val name = fields.valueOf(key = "N").nullIfBlank()
        val nameParts = name?.split(',', limit = 2)

        return BarcodeContent.Contact(
            formattedName = name.takeIf { nameParts?.size == 1 },
            firstName = nameParts?.getOrNull(1).nullIfBlank(),
            lastName = nameParts?.takeIf { it.size == 2 }?.first().nullIfBlank(),
            organization = fields.valueOf(key = "ORG").nullIfBlank(),
            title = fields.valueOf(key = "TITLE").nullIfBlank(),
            phones = fields.valuesOf(key = "TEL"),
            emails = fields.valuesOf(key = "EMAIL"),
            addresses = fields.valuesOf(key = "ADR"),
            urls = fields.valuesOf(key = "URL"),
        ).takeUnless { it == BarcodeContent.Contact() }
    }

    private fun parseVcard(text: String): BarcodeContent.Contact? {
        var contact = BarcodeContent.Contact()

        unfoldedLines(text = text).forEach { line ->
            val separatorIndex = line.indexOf(':')
            if (separatorIndex <= 0) {
                return@forEach
            }

            // "item1.TEL;TYPE=CELL" is the property TEL: its group and its parameters are not needed
            val property = line.take(separatorIndex)
                .substringBefore(';')
                .substringAfterLast('.')
                .uppercase()
            val value = line.drop(separatorIndex + 1)

            contact = when (property) {
                "FN" -> contact.copy(formattedName = unescapeVcard(value = value).nullIfBlank())
                "N" -> {
                    val components = splitVcardComponents(value = value)
                    contact.copy(
                        lastName = components.getOrNull(0).nullIfBlank(),
                        firstName = components.getOrNull(1).nullIfBlank(),
                    )
                }
                "ORG" -> contact.copy(organization = joinVcardComponents(value = value))
                "TITLE" -> contact.copy(title = unescapeVcard(value = value).nullIfBlank())
                "TEL" -> contact.copy(phones = contact.phones + listOfNotNull(vcardPhone(value = value)))
                "EMAIL" -> contact.copy(emails = contact.emails + listOfNotNull(unescapeVcard(value = value).nullIfBlank()))
                "ADR" -> contact.copy(addresses = contact.addresses + listOfNotNull(joinVcardComponents(value = value)))
                "URL" -> contact.copy(urls = contact.urls + listOfNotNull(unescapeVcard(value = value).nullIfBlank()))
                else -> contact
            }
        }

        return contact.takeUnless { it == BarcodeContent.Contact() }
    }

    private fun parsePhone(uri: String): BarcodeContent.Phone? =
        percentDecode(value = uri).nullIfBlank()?.let { phoneNumber ->
            BarcodeContent.Phone(phoneNumber = phoneNumber)
        }

    // "lat,lon[,alt][;parameters][?q=…]" of RFC 5870. Android puts the place in the query, with its name:
    // "q=lat,lon(name)", or a search around the position: "q=name". A search around 0,0 has no position
    private fun parseGeo(uri: String): BarcodeContent.Location? {
        val coordinates = uri.substringBefore('?')
            .substringBefore(';')
            .split(',')
        val latitude = coordinates.getOrNull(0)?.trim()?.toDoubleOrNull() ?: return null
        val longitude = coordinates.getOrNull(1)?.trim()?.toDoubleOrNull() ?: return null
        val isAtZero = latitude == 0.0 && longitude == 0.0

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

    private class Field(
        val key: String,
        val value: String,
    )

    // "KEY:value;KEY:value;;", where a backslash escapes the next character and a value may be quoted
    private fun parseFields(text: String): List<Field> {
        val fields = mutableListOf<Field>()
        val current = StringBuilder()
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
                )
            }
            key = null
            current.clear()
            startsWithQuote = false
            endsWithQuote = false
        }

        var index = 0
        while (index < text.length) {
            val char = text[index]
            when {
                char == '\\' && index + 1 < text.length -> {
                    current.append(text[index + 1])
                    endsWithQuote = false
                    index++
                }

                char == ':' && key == null -> {
                    key = current.toString()
                    current.clear()
                }

                char == ';' -> endField()

                else -> {
                    if (key != null && current.isEmpty() && char == '"') {
                        startsWithQuote = true
                    }
                    current.append(char)
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

    private fun joinVcardComponents(value: String): String? =
        splitVcardComponents(value = value)
            .filter { it.isNotBlank() }
            .joinToString(separator = ", ")
            .nullIfBlank()

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