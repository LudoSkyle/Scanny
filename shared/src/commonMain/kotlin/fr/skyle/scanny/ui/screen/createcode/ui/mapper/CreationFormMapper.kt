package fr.skyle.scanny.ui.screen.createcode.ui.mapper

import fr.skyle.scanny.domain.model.BarcodeContent
import fr.skyle.scanny.domain.model.BarcodeFormat
import fr.skyle.scanny.domain.model.OtpType
import fr.skyle.scanny.domain.model.WifiEncryption
import fr.skyle.scanny.ui.screen.createcode.ui.model.CreationForm
import fr.skyle.scanny.ui.screen.createcode.ui.model.CreationKind

// The length of a password Android accepts for a WPA network, which iOS accepts too
private val WPA_PASSWORD_LENGTH = 8..63

private val LATITUDES = -90.0..90.0
private val LONGITUDES = -180.0..180.0

// Base32, 80 bits at least: the shortest key the authenticator apps take
private val OTP_SECRET = Regex("""[A-Z2-7]{16,}=*""")

// Loose on purpose: an address of the network or of its tests, legacy or segwit
private val BITCOIN_ADDRESS = Regex("""[A-Za-z0-9]{25,90}""")

// In bitcoins, to the satoshi
private val BITCOIN_AMOUNT = Regex("""[0-9]+(\.[0-9]{1,8})?""")

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

        CreationKind.MMS -> smsPhoneNumber.trim().takeIf { phoneNumber -> phoneNumber.isNotEmpty() }?.let { phoneNumber ->
            BarcodeContent.Mms(
                phoneNumber = phoneNumber,
                subject = mmsSubject.ifBlank { null },
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
                format = contactFormat,
            )
        }

        CreationKind.LOCATION -> toLocation()

        CreationKind.BOOKMARK -> bookmarkUrl.trim().takeIf { url -> url.isNotEmpty() }?.let { url ->
            BarcodeContent.Bookmark(
                url = url.withScheme(),
                title = bookmarkTitle.trim().ifEmpty { null },
            )
        }

        CreationKind.OTP -> toOtp()

        CreationKind.BITCOIN -> toBitcoin()

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

// What the key URI format asks for: an account, a key in base32 (typed as the services show it, in
// groups and in lower case), and the counter of a HOTP key. The parameters left to their default are
// not written: the code is smaller, and reads the same
private fun CreationForm.toOtp(): BarcodeContent.Otp? {
    val secret = otpSecret.filterNot { char -> char.isWhitespace() || char == '-' }.uppercase()
    val account = otpAccount.trim().takeIf { account -> account.isNotEmpty() } ?: return null
    if (!OTP_SECRET.matches(input = secret)) {
        return null
    }
    val period = when (otpType) {
        OtpType.TOTP -> otpPeriod.trim().toIntOrNull()?.takeIf { period -> period > 0 } ?: return null
        OtpType.HOTP -> null
    }
    val counter = when (otpType) {
        OtpType.TOTP -> null
        OtpType.HOTP -> otpCounter.trim().toLongOrNull()?.takeIf { counter -> counter >= 0 } ?: return null
    }

    return BarcodeContent.Otp(
        secret = secret,
        type = otpType,
        issuer = otpIssuer.trim().ifEmpty { null },
        account = account,
        algorithm = otpAlgorithm.takeUnless { algorithm -> algorithm == BarcodeContent.Otp.DEFAULT_ALGORITHM },
        digits = otpDigits.takeUnless { digits -> digits == BarcodeContent.Otp.DEFAULT_DIGITS },
        period = period?.takeUnless { period -> period == BarcodeContent.Otp.DEFAULT_PERIOD },
        counter = counter,
    )
}

// An address; an amount if any, typed with a decimal comma or point
private fun CreationForm.toBitcoin(): BarcodeContent.Bitcoin? {
    val address = bitcoinAddress.trim().takeIf { address -> BITCOIN_ADDRESS.matches(input = address) } ?: return null
    val amount = bitcoinAmount.trim()
        .replace(
            oldChar = ',',
            newChar = '.',
        )
        .ifEmpty { null }
    if (amount != null && !BITCOIN_AMOUNT.matches(input = amount)) {
        return null
    }

    return BarcodeContent.Bitcoin(
        address = address,
        amount = amount,
        label = bitcoinLabel.trim().ifEmpty { null },
        message = bitcoinMessage.trim().ifEmpty { null },
    )
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