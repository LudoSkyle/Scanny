package fr.skyle.scanny.domain.payload

import fr.skyle.scanny.domain.model.BarcodeContent
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

    fun encode(content: BarcodeContent): String =
        when (content) {
            is BarcodeContent.Text -> content.text
            is BarcodeContent.Url -> content.url
            is BarcodeContent.Email -> encodeEmail(email = content)
            is BarcodeContent.Sms -> encodeSms(sms = content)
            is BarcodeContent.Wifi -> encodeWifi(wifi = content)
            is BarcodeContent.Contact -> encodeContact(contact = content)
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
            email.subject?.let { subject ->
                "subject=${percentEncode(value = subject, allowed = URI_UNRESERVED)}"
            },
            email.body?.let { body ->
                "body=${percentEncode(value = body, allowed = URI_UNRESERVED)}"
            },
        )

        return buildString {
            append(PayloadSyntax.MAILTO)
            append(
                percentEncode(
                    value = email.address.orEmpty(),
                    allowed = MAILTO_ADDRESS_ALLOWED,
                ),
            )
            if (parameters.isNotEmpty()) {
                append('?')
                append(parameters.joinToString(separator = "&"))
            }
        }
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

    private fun encodeWifi(wifi: BarcodeContent.Wifi): String {
        val type = when (wifi.encryption) {
            WifiEncryption.NONE -> "nopass"
            WifiEncryption.WEP -> "WEP"
            WifiEncryption.WPA -> "WPA"
            WifiEncryption.WPA3 -> "SAE"
        }

        return buildString {
            append(PayloadSyntax.WIFI)
            append("T:$type;")
            append("S:${escapeField(value = wifi.ssid)};")
            if (wifi.encryption != WifiEncryption.NONE && wifi.password != null) {
                append("P:${escapeField(value = wifi.password)};")
            }
            if (wifi.isHidden) {
                append("H:true;")
            }
            append(';')
        }
    }

    // vCard 3.0. Only the text properties are escaped: a telephone, an e-mail or an URL is written as is
    private fun encodeContact(contact: BarcodeContent.Contact): String =
        buildList {
            add("BEGIN:VCARD")
            add("VERSION:3.0")
            if (contact.lastName != null || contact.firstName != null) {
                val lastName = escapeVcard(value = contact.lastName.orEmpty())
                val firstName = escapeVcard(value = contact.firstName.orEmpty())
                add("N:$lastName;$firstName;;;")
            }
            contact.formattedName?.let { formattedName ->
                add("FN:${escapeVcard(value = formattedName)}")
            }
            contact.organization?.let { organization ->
                add("ORG:${escapeVcard(value = organization)}")
            }
            contact.title?.let { title ->
                add("TITLE:${escapeVcard(value = title)}")
            }
            contact.phones.forEach { phone ->
                add("TEL:$phone")
            }
            contact.emails.forEach { email ->
                add("EMAIL:$email")
            }
            contact.addresses.forEach { address ->
                add("ADR:;;${escapeVcard(value = address)};;;;")
            }
            contact.urls.forEach { url ->
                add("URL:$url")
            }
            add("END:VCARD")
        }.joinToString(separator = "\n")

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