package fr.skyle.scanny.ui.screen.createcode.ui.mapper

import fr.skyle.scanny.domain.model.BarcodeContent
import fr.skyle.scanny.domain.model.BarcodeFormat
import fr.skyle.scanny.domain.model.WifiEncryption
import fr.skyle.scanny.ui.screen.createcode.ui.model.CreationForm
import fr.skyle.scanny.ui.screen.createcode.ui.model.CreationKind

// The length of a password Android accepts for a WPA network, which iOS accepts too
private val WPA_PASSWORD_LENGTH = 8..63

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
            val hasScheme = url.startsWith(prefix = HTTP, ignoreCase = true) || url.startsWith(prefix = HTTPS, ignoreCase = true)
            BarcodeContent.Url(url = if (hasScheme) url else "$HTTPS$url")
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

        // Whether its format accepts it is told by the drawing of the code
        CreationKind.BARCODE -> barcodeValue.trim().takeIf { value -> value.isNotEmpty() }?.let { value ->
            BarcodeContent.Text(text = value)
        }
    }

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