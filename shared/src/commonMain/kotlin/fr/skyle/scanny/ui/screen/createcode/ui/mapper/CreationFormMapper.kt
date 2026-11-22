package fr.skyle.scanny.ui.screen.createcode.ui.mapper

import fr.skyle.scanny.domain.model.BarcodeContent
import fr.skyle.scanny.domain.model.BarcodeFormat
import fr.skyle.scanny.domain.model.ContactAddress
import fr.skyle.scanny.domain.model.ContactEmail
import fr.skyle.scanny.domain.model.ContactEntryType
import fr.skyle.scanny.domain.model.ContactFormat
import fr.skyle.scanny.domain.model.ContactPhone
import fr.skyle.scanny.domain.model.ContactPhoneType
import fr.skyle.scanny.domain.model.ContactWebsite
import fr.skyle.scanny.domain.model.EventDateTime
import fr.skyle.scanny.domain.model.EventFrequency
import fr.skyle.scanny.domain.model.EventRecurrence
import fr.skyle.scanny.domain.model.OtpType
import fr.skyle.scanny.domain.model.WifiEncryption
import fr.skyle.scanny.domain.model.WifiTransitionMode
import fr.skyle.scanny.ui.screen.createcode.ui.model.CreationForm
import fr.skyle.scanny.ui.screen.createcode.ui.model.CreationKind
import fr.skyle.scanny.ui.screen.createcode.ui.model.RecurrenceEnd
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.plus

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

        // RFC 6068 asks for no field in particular, but a code holds at least one
        CreationKind.EMAIL -> BarcodeContent.Email(
            to = emailTo.toAddresses(),
            cc = emailCc.toAddresses(),
            bcc = emailBcc.toAddresses(),
            subject = emailSubject.ifBlank { null },
            body = emailBody.ifBlank { null },
        ).takeUnless { email -> email == BarcodeContent.Email() }

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

        CreationKind.CONTACT -> toContact()

        CreationKind.LOCATION -> toLocation()

        CreationKind.BOOKMARK -> bookmarkUrl.trim().takeIf { url -> url.isNotEmpty() }?.let { url ->
            BarcodeContent.Bookmark(
                url = url.withScheme(),
                title = bookmarkTitle.trim().ifEmpty { null },
            )
        }

        CreationKind.OTP -> toOtp()

        CreationKind.BITCOIN -> toBitcoin()

        CreationKind.EVENT -> toEvent()

        // Whether its format accepts it is told by the drawing of the code
        CreationKind.BARCODE -> barcodeValue.trim().takeIf { value -> value.isNotEmpty() }?.let { value ->
            BarcodeContent.Text(text = value)
        }
    }

// A vCard needs a name to show: the one typed, else the parts of the name, else the organization. A
// MeCard needs a last or a first name. The fields a MeCard does not hold (the other parts of the name, the
// department, the title and the kinds of the entries) stay in the form, but are not written
private fun CreationForm.toContact(): BarcodeContent.Contact? {
    val isVcard = contactFormat == ContactFormat.VCARD
    fun String.field(isHeld: Boolean = true): String? = trim().ifEmpty { null }?.takeIf { isHeld }

    val namePrefix = contactNamePrefix.field(isHeld = isVcard)
    val firstName = contactFirstName.field()
    val middleName = contactMiddleName.field(isHeld = isVcard)
    val lastName = contactLastName.field()
    val nameSuffix = contactNameSuffix.field(isHeld = isVcard)
    val organization = contactOrganization.field()
    val formattedName = if (isVcard) {
        contactDisplayName.field()
            ?: listOfNotNull(namePrefix, firstName, middleName, lastName, nameSuffix).joinToString(separator = " ").ifEmpty { null }
            ?: organization
    } else {
        null
    }
    val hasName = if (isVcard) formattedName != null else firstName != null || lastName != null
    if (!hasName) {
        return null
    }

    return BarcodeContent.Contact(
        formattedName = formattedName,
        namePrefix = namePrefix,
        firstName = firstName,
        middleName = middleName,
        lastName = lastName,
        nameSuffix = nameSuffix,
        nickname = contactNickname.field(),
        organization = organization,
        department = contactDepartment.field(isHeld = isVcard),
        title = contactTitle.field(isHeld = isVcard),
        phones = contactPhones.mapNotNull { phone ->
            phone.number.field()?.let { number ->
                ContactPhone(number = number, type = if (isVcard) phone.type else ContactPhoneType.OTHER)
            }
        },
        emails = contactEmails.mapNotNull { email ->
            email.value.field()?.let { address ->
                ContactEmail(address = address, type = if (isVcard) email.type else ContactEntryType.OTHER)
            }
        },
        addresses = contactAddresses.mapNotNull { address ->
            ContactAddress(
                poBox = address.poBox.field(),
                extended = address.extended.field(),
                street = address.street.field(),
                city = address.city.field(),
                region = address.region.field(),
                postalCode = address.postalCode.field(),
                country = address.country.field(),
                type = if (isVcard) address.type else ContactEntryType.OTHER,
            ).takeUnless { it.copy(type = ContactEntryType.OTHER) == ContactAddress() }
        },
        websites = contactWebsites.mapNotNull { website ->
            website.value.field()?.let { url ->
                ContactWebsite(url = url, type = if (isVcard) website.type else ContactEntryType.OTHER)
            }
        },
        birthday = contactBirthday.ifEmpty { null },
        note = contactNote.ifBlank { null },
        format = contactFormat,
    )
}

// Separated by commas, semicolons or spaces, which no address holds
private fun String.toAddresses(): List<String> =
    split(',', ';', ' ', '\n').map { address -> address.trim() }.filter { address -> address.isNotEmpty() }

private fun String.withScheme(): String {
    val hasScheme = startsWith(prefix = HTTP, ignoreCase = true) || startsWith(prefix = HTTPS, ignoreCase = true)
    return if (hasScheme) this else "$HTTPS$this"
}

// A latitude and a longitude on the map; an altitude and an uncertainty in meters if any. Each typed with
// a decimal comma or point
private fun CreationForm.toLocation(): BarcodeContent.Location? {
    val latitude = locationLatitude.toCoordinate()?.takeIf { latitude -> latitude in LATITUDES } ?: return null
    val longitude = locationLongitude.toCoordinate()?.takeIf { longitude -> longitude in LONGITUDES } ?: return null
    val altitude = locationAltitude.ifBlank { null }?.let { altitude -> altitude.toCoordinate() ?: return null }
    val uncertainty = locationUncertainty.ifBlank { null }?.let { uncertainty ->
        uncertainty.toCoordinate()?.takeIf { meters -> meters >= 0 } ?: return null
    }

    return BarcodeContent.Location(
        latitude = latitude,
        longitude = longitude,
        altitude = altitude,
        uncertainty = uncertainty,
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

// A title and a start; an end after it, a time for each day of an event at a time. The last day of an
// event of whole days is the day before the end iCalendar writes. A recurrence ends after a number of times
// or on a last day, as chosen
private fun CreationForm.toEvent(): BarcodeContent.Event? {
    val summary = eventSummary.trim().takeIf { summary -> summary.isNotEmpty() } ?: return null
    val startDate = eventStartDate ?: return null
    val timeZone = eventTimeZone.takeUnless { isEventAllDay }
    val start = if (isEventAllDay) {
        EventDateTime(date = startDate)
    } else {
        EventDateTime(date = startDate, time = eventStartTime ?: return null, timeZone = timeZone)
    }
    val end = when {
        isEventAllDay -> eventEndDate?.let { lastDay -> EventDateTime(date = lastDay.plus(1, DateTimeUnit.DAY)) }
        eventEndTime != null -> EventDateTime(date = eventEndDate ?: startDate, time = eventEndTime, timeZone = timeZone)
        eventEndDate != null -> return null
        else -> null
    }
    if (end != null && end.sortKey() < start.sortKey()) {
        return null
    }

    val latitude = eventLatitude.ifBlank { null }?.let { latitude -> latitude.toCoordinate()?.takeIf { it in LATITUDES } ?: return null }
    val longitude = eventLongitude.ifBlank { null }?.let { longitude -> longitude.toCoordinate()?.takeIf { it in LONGITUDES } ?: return null }
    if ((latitude == null) != (longitude == null)) {
        return null
    }

    val recurrence = eventFrequency?.let { frequency ->
        EventRecurrence(
            frequency = frequency,
            interval = eventInterval.ifBlank { null }?.let { interval -> interval.trim().toIntOrNull()?.takeIf { it > 0 } ?: return null }?.takeIf { it > 1 },
            count = when (eventRecurrenceEnd) {
                RecurrenceEnd.COUNT -> eventCount.trim().toIntOrNull()?.takeIf { it > 0 } ?: return null
                RecurrenceEnd.NEVER, RecurrenceEnd.UNTIL -> null
            },
            until = when (eventRecurrenceEnd) {
                RecurrenceEnd.UNTIL -> eventUntil?.takeIf { until -> until >= startDate } ?: return null
                RecurrenceEnd.NEVER, RecurrenceEnd.COUNT -> null
            },
            days = eventDays.takeIf { frequency == EventFrequency.WEEKLY }.orEmpty(),
        )
    }

    return BarcodeContent.Event(
        summary = summary,
        start = start,
        end = end,
        location = eventLocation.trim().ifEmpty { null },
        latitude = latitude,
        longitude = longitude,
        description = eventDescription.ifBlank { null },
        url = eventUrl.trim().ifEmpty { null }?.withScheme(),
        organizer = eventOrganizer.trim().ifEmpty { null },
        attendees = eventAttendees.toAddresses(),
        recurrence = recurrence,
        reminderMinutes = eventReminderMinutes,
        status = eventStatus,
        transparency = eventTransparency,
        classification = eventClassification,
        priority = eventPriority,
        categories = eventCategories.split(',').map { category -> category.trim() }.filter { category -> category.isNotEmpty() },
    )
}

// The start and the end of an event share their zone: their days and times compare as they are
private fun EventDateTime.sortKey(): String =
    "$date ${time ?: ""}"

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

/** The methods of EAP an enterprise network is joined with, as the WIFI: format of ZXing names them. */
internal val WIFI_EAP_METHODS = listOf("PEAP", "TTLS", "TLS", "PWD", "SIM", "AKA", "AKA'")

/** The authentications of phase 2 inside the tunnel of PEAP or TTLS, null for none. */
internal val WIFI_PHASE2_METHODS = listOf(null, "PAP", "MSCHAP", "MSCHAPV2", "GTC")

/** Whether a method of EAP takes a password: a certificate or a SIM card stands for it in the others. */
internal fun isPasswordEap(method: String): Boolean =
    method in setOf("PEAP", "TTLS", "PWD")

/** Whether a method of EAP opens a tunnel, inside which a phase 2 authenticates. */
internal fun isTunneledEap(method: String): Boolean =
    method in setOf("PEAP", "TTLS")

// A name; a password for a network that asks for one: of the length WPA2 needs for a network it may join,
// of any length for a WPA3 only one. An identity for an enterprise network. The mode not to fall back from
// of its kind; the public key of SAE-PK makes it SAE-PK only, and so WPA3 only
private fun CreationForm.toWifi(): BarcodeContent.Wifi? {
    if (wifiSsid.isEmpty()) {
        return null
    }

    val publicKey = wifiPublicKey.trim().ifEmpty { null }.takeIf { wifiEncryption == WifiEncryption.WPA }
    val transitionDisable = when (wifiEncryption) {
        WifiEncryption.NONE -> setOfNotNull(WifiTransitionMode.ENHANCED_OPEN.takeIf { isWifiEnhancedOpenOnly })
        WifiEncryption.WEP -> emptySet()
        WifiEncryption.WPA -> setOfNotNull(
            WifiTransitionMode.WPA3_PERSONAL.takeIf { isWifiWpa3Only || publicKey != null },
            WifiTransitionMode.SAE_PK.takeIf { publicKey != null },
        )
        WifiEncryption.WPA_ENTERPRISE -> setOfNotNull(WifiTransitionMode.WPA3_ENTERPRISE.takeIf { isWifiWpa3EnterpriseOnly })
    }
    val isEnterprise = wifiEncryption == WifiEncryption.WPA_ENTERPRISE
    val hasPassword = when (wifiEncryption) {
        WifiEncryption.NONE -> false
        WifiEncryption.WEP, WifiEncryption.WPA -> true
        WifiEncryption.WPA_ENTERPRISE -> isPasswordEap(method = wifiEapMethod)
    }

    val isPasswordValid = when {
        !hasPassword -> true
        wifiEncryption == WifiEncryption.WPA && WifiTransitionMode.WPA3_PERSONAL !in transitionDisable -> wifiPassword.length in WPA_PASSWORD_LENGTH
        else -> wifiPassword.isNotEmpty()
    }
    val identity = wifiIdentity.trim().ifEmpty { null }.takeIf { isEnterprise }
    if (!isPasswordValid || (isEnterprise && identity == null)) {
        return null
    }

    return BarcodeContent.Wifi(
        ssid = wifiSsid,
        encryption = wifiEncryption,
        password = wifiPassword.takeIf { hasPassword },
        isHidden = isWifiHidden,
        transitionDisable = transitionDisable,
        passwordIdentifier = wifiPasswordIdentifier.trim().ifEmpty { null }.takeIf { wifiEncryption == WifiEncryption.WPA },
        publicKey = publicKey,
        eapMethod = wifiEapMethod.takeIf { isEnterprise },
        identity = identity,
        anonymousIdentity = wifiAnonymousIdentity.trim().ifEmpty { null }.takeIf { isEnterprise },
        phase2Method = wifiPhase2Method.takeIf { isEnterprise && isTunneledEap(method = wifiEapMethod) },
    )
}