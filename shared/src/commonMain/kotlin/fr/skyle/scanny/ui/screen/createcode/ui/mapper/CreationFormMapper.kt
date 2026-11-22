package fr.skyle.scanny.ui.screen.createcode.ui.mapper

import fr.skyle.scanny.domain.model.BarcodeContent
import fr.skyle.scanny.domain.model.BarcodeFormat
import fr.skyle.scanny.domain.model.WifiEncryption
import fr.skyle.scanny.ui.screen.createcode.ui.model.CreationForm
import fr.skyle.scanny.ui.screen.createcode.ui.model.CreationKind

// The length of a password Android accepts for a WPA network, which iOS accepts too
private val WPA_PASSWORD_LENGTH = 8..63

private val LATITUDES = -90.0..90.0
private val LONGITUDES = -180.0..180.0

private const val HTTP = "http://"
private const val HTTPS = "https://"

/** The format of the code created: a QR code, or the barcode chosen. */
internal val CreationForm.format: BarcodeFormat
    get() = if (kind == CreationKind.BARCODE) barcodeFormat else BarcodeFormat.QR_CODE

/**
 * The content of the code the form describes, null while a field it needs is missing or wrong. A link
 * typed without its scheme gets `https://`: without it, a scanner would read a plain text.
 */
internal fun CreationForm.toBarcodeContent(): BarcodeContent? =
    when (kind) {
        CreationKind.TEXT -> text.takeIf { text -> text.isNotBlank() }?.let { text ->
            BarcodeContent.Text(text = text)
        }

        CreationKind.LINK -> url.trim().takeIf { url -> url.isNotEmpty() }?.let { url ->
            BarcodeContent.Url(url = url.withScheme())
        }

        CreationKind.EMAIL -> emailAddress.trim().takeIf { address -> address.isNotEmpty() }?.let { address ->
            BarcodeContent.Email(
                address = address,
                subject = emailSubject.ifBlank { null },
                body = emailBody.ifBlank { null },
            )
        }

        CreationKind.SMS -> smsPhoneNumber.trim().takeIf { phoneNumber -> phoneNumber.isNotEmpty() }?.let { phoneNumber ->
            BarcodeContent.Sms(
                phoneNumber = phoneNumber,
                message = smsMessage.ifBlank { null },
            )
        }

        CreationKind.PHONE -> phoneNumber.trim().takeIf { phoneNumber -> phoneNumber.isNotEmpty() }?.let { phoneNumber ->
            BarcodeContent.Phone(phoneNumber = phoneNumber)
        }

        CreationKind.WIFI -> toWifi()

        CreationKind.CONTACT -> contactName.trim().takeIf { name -> name.isNotEmpty() }?.let { name ->
            BarcodeContent.Contact(
                formattedName = name,
                organization = contactOrganization.trim().ifEmpty { null },
                phones = listOfNotNull(contactPhone.trim().ifEmpty { null }),
                emails = listOfNotNull(contactEmail.trim().ifEmpty { null }),
                addresses = listOfNotNull(contactAddress.trim().ifEmpty { null }),
                urls = listOfNotNull(contactWebsite.trim().ifEmpty { null }),
            )
        }

        CreationKind.LOCATION -> toLocation()

        CreationKind.BOOKMARK -> bookmarkUrl.trim().takeIf { url -> url.isNotEmpty() }?.let { url ->
            BarcodeContent.Bookmark(
                url = url.withScheme(),
                title = bookmarkTitle.trim().ifEmpty { null },
            )
        }

        // Whether its format accepts it is told by the drawing of the code
        CreationKind.BARCODE -> barcodeValue.trim().takeIf { value -> value.isNotEmpty() }?.let { value ->
            BarcodeContent.Text(text = value)
        }
    }

private fun String.withScheme(): String {
    val hasScheme = startsWith(prefix = HTTP, ignoreCase = true) || startsWith(prefix = HTTPS, ignoreCase = true)
    return if (hasScheme) this else "$HTTPS$this"
}

// A latitude and a longitude on the map, typed with a decimal comma or point
private fun CreationForm.toLocation(): BarcodeContent.Location? {
    val latitude = locationLatitude.toCoordinate()?.takeIf { latitude -> latitude in LATITUDES } ?: return null
    val longitude = locationLongitude.toCoordinate()?.takeIf { longitude -> longitude in LONGITUDES } ?: return null

    return BarcodeContent.Location(
        latitude = latitude,
        longitude = longitude,
        label = locationLabel.trim().ifEmpty { null },
    )
}

private fun String.toCoordinate(): Double? =
    trim()
        .replace(
            oldChar = ',',
            newChar = '.',
        )
        .toDoubleOrNull()

// A name; a password for a network that asks for one, of the length WPA needs
private fun CreationForm.toWifi(): BarcodeContent.Wifi? {
    if (wifiSsid.isEmpty()) {
        return null
    }

    val isPasswordValid = when (wifiEncryption) {
        WifiEncryption.NONE -> true
        WifiEncryption.WEP -> wifiPassword.isNotEmpty()
        WifiEncryption.WPA,
        WifiEncryption.WPA3,
        -> wifiPassword.length in WPA_PASSWORD_LENGTH
    }
    if (!isPasswordValid) {
        return null
    }

    return BarcodeContent.Wifi(
        ssid = wifiSsid,
        encryption = wifiEncryption,
        password = wifiPassword.takeIf { wifiEncryption != WifiEncryption.NONE },
        isHidden = isWifiHidden,
    )
}