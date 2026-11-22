package fr.skyle.scanny.ui.screen.createcode.ui.mapper

import fr.skyle.scanny.domain.model.BarcodeContent
import fr.skyle.scanny.domain.model.BarcodeFormat
import fr.skyle.scanny.domain.model.ContactFormat
import fr.skyle.scanny.domain.model.Creation
import fr.skyle.scanny.domain.model.ScannedCode
import fr.skyle.scanny.domain.model.WifiEncryption
import fr.skyle.scanny.domain.model.WifiTransitionMode
import fr.skyle.scanny.domain.payload.toCoordinateText
import fr.skyle.scanny.ui.core.code.mapper.toBarcodeType
import fr.skyle.scanny.ui.screen.createcode.ui.model.ContactAddressField
import fr.skyle.scanny.ui.screen.createcode.ui.model.ContactEntryField
import fr.skyle.scanny.ui.screen.createcode.ui.model.ContactPhoneField
import fr.skyle.scanny.ui.screen.createcode.ui.model.CreationForm
import fr.skyle.scanny.ui.screen.createcode.ui.model.CreationKind

/** The form a scanned code fills, to be changed and created as a new code. */
internal fun ScannedCode.toCreationForm(): CreationForm =
    content.toCreationForm(
        rawValue = rawValue,
        format = format,
    )

/** The form a created code fills, to be changed and created as a new code: its style goes apart. */
internal fun Creation.toCreationForm(): CreationForm =
    content.toCreationForm(
        rawValue = rawValue,
        format = format,
    )

/**
 * The form a code fills. A barcode of a format the editor draws stays a barcode of its value; any other
 * code becomes a QR code holding its content.
 * A parameter a one-time password leaves to its default shows its default.
 */
private fun BarcodeContent.toCreationForm(
    rawValue: String,
    format: BarcodeFormat?,
): CreationForm {
    val barcodeFormat = format?.takeIf { format -> format.toBarcodeType() != null }
    if (barcodeFormat != null) {
        return CreationForm(
            kind = CreationKind.BARCODE,
            barcodeFormat = barcodeFormat,
            barcodeValue = rawValue,
        )
    }

    return when (val content = this) {
        is BarcodeContent.Text -> CreationForm(
            kind = CreationKind.TEXT,
            text = content.text,
        )

        is BarcodeContent.Url -> CreationForm(
            kind = CreationKind.LINK,
            url = content.url,
        )

        is BarcodeContent.Email -> CreationForm(
            kind = CreationKind.EMAIL,
            emailTo = content.to.joinToString(separator = ", "),
            emailCc = content.cc.joinToString(separator = ", "),
            emailBcc = content.bcc.joinToString(separator = ", "),
            emailSubject = content.subject.orEmpty(),
            emailBody = content.body.orEmpty(),
        )

        is BarcodeContent.Sms -> CreationForm(
            kind = CreationKind.SMS,
            smsPhoneNumber = content.phoneNumber.orEmpty(),
            smsMessage = content.message.orEmpty(),
        )

        is BarcodeContent.Mms -> CreationForm(
            kind = CreationKind.MMS,
            smsPhoneNumber = content.phoneNumber.orEmpty(),
            mmsSubject = content.subject.orEmpty(),
            smsMessage = content.message.orEmpty(),
        )

        is BarcodeContent.Wifi -> CreationForm(
            kind = CreationKind.WIFI,
            wifiSsid = content.ssid,
            wifiEncryption = content.encryption,
            wifiPassword = content.password.orEmpty(),
            isWifiHidden = content.isHidden,
            isWifiWpa3Only = WifiTransitionMode.WPA3_PERSONAL in content.transitionDisable,
            isWifiEnhancedOpenOnly = WifiTransitionMode.ENHANCED_OPEN in content.transitionDisable,
            isWifiWpa3EnterpriseOnly = WifiTransitionMode.WPA3_ENTERPRISE in content.transitionDisable,
            wifiPasswordIdentifier = content.passwordIdentifier.orEmpty(),
            wifiPublicKey = content.publicKey.orEmpty(),
            wifiEapMethod = content.eapMethod ?: CreationForm().wifiEapMethod,
            wifiIdentity = content.identity.orEmpty(),
            wifiAnonymousIdentity = content.anonymousIdentity.orEmpty(),
            wifiPhase2Method = if (content.encryption == WifiEncryption.WPA_ENTERPRISE) content.phase2Method else CreationForm().wifiPhase2Method,
        )

        is BarcodeContent.Contact -> CreationForm(
            kind = CreationKind.CONTACT,
            contactNamePrefix = content.namePrefix.orEmpty(),
            contactFirstName = content.firstName.orEmpty(),
            contactMiddleName = content.middleName.orEmpty(),
            // A MeCard named in one piece shows it as its last name: the editor has no name shown for it
            contactLastName = content.lastName
                ?: content.formattedName.takeIf { content.format == ContactFormat.MECARD && content.firstName == null }.orEmpty(),
            contactNameSuffix = content.nameSuffix.orEmpty(),
            // The name shown of a vCard, only when the editor would not make it the same from the other fields
            contactDisplayName = content.formattedName
                ?.takeUnless { formattedName -> content.format == ContactFormat.MECARD || formattedName == content.defaultFormattedName() }
                .orEmpty(),
            contactNickname = content.nickname.orEmpty(),
            contactOrganization = content.organization.orEmpty(),
            contactDepartment = content.department.orEmpty(),
            contactTitle = content.title.orEmpty(),
            contactPhones = content.phones
                .map { phone -> ContactPhoneField(number = phone.number, type = phone.type) }
                .ifEmpty { listOf(ContactPhoneField()) },
            contactEmails = content.emails
                .map { email -> ContactEntryField(value = email.address, type = email.type) }
                .ifEmpty { listOf(ContactEntryField()) },
            contactAddresses = content.addresses.map { address ->
                ContactAddressField(
                    street = address.street.orEmpty(),
                    extended = address.extended.orEmpty(),
                    poBox = address.poBox.orEmpty(),
                    postalCode = address.postalCode.orEmpty(),
                    city = address.city.orEmpty(),
                    region = address.region.orEmpty(),
                    country = address.country.orEmpty(),
                    type = address.type,
                )
            },
            contactWebsites = content.websites
                .map { website -> ContactEntryField(value = website.url, type = website.type) }
                .ifEmpty { listOf(ContactEntryField()) },
            contactBirthday = content.birthday.orEmpty(),
            contactNote = content.note.orEmpty(),
            contactFormat = content.format,
        )

        is BarcodeContent.Phone -> CreationForm(
            kind = CreationKind.PHONE,
            phoneNumber = content.phoneNumber,
        )

        is BarcodeContent.Location -> CreationForm(
            kind = CreationKind.LOCATION,
            locationLatitude = content.latitude.toCoordinateText(),
            locationLongitude = content.longitude.toCoordinateText(),
            locationAltitude = content.altitude?.toCoordinateText().orEmpty(),
            locationUncertainty = content.uncertainty?.toCoordinateText().orEmpty(),
            locationLabel = content.label.orEmpty(),
        )

        is BarcodeContent.Bookmark -> CreationForm(
            kind = CreationKind.BOOKMARK,
            bookmarkUrl = content.url,
            bookmarkTitle = content.title.orEmpty(),
        )

        is BarcodeContent.Otp -> CreationForm(
            kind = CreationKind.OTP,
            otpIssuer = content.issuer.orEmpty(),
            otpAccount = content.account.orEmpty(),
            otpSecret = content.secret,
            otpType = content.type,
            otpAlgorithm = content.algorithm ?: BarcodeContent.Otp.DEFAULT_ALGORITHM,
            otpDigits = content.digits ?: BarcodeContent.Otp.DEFAULT_DIGITS,
            otpPeriod = (content.period ?: BarcodeContent.Otp.DEFAULT_PERIOD).toString(),
            otpCounter = content.counter?.toString().orEmpty(),
        )

        is BarcodeContent.Bitcoin -> CreationForm(
            kind = CreationKind.BITCOIN,
            bitcoinAddress = content.address,
            bitcoinAmount = content.amount.orEmpty(),
            bitcoinLabel = content.label.orEmpty(),
            bitcoinMessage = content.message.orEmpty(),
        )
    }
}

// The name the editor shows for a vCard when none is typed: the parts of the name, else the organization
private fun BarcodeContent.Contact.defaultFormattedName(): String? =
    listOfNotNull(namePrefix, firstName, middleName, lastName, nameSuffix).joinToString(separator = " ").ifEmpty { null } ?: organization
