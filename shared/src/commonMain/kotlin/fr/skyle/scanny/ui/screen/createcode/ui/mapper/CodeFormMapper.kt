package fr.skyle.scanny.ui.screen.createcode.ui.mapper

import fr.skyle.scanny.domain.model.BarcodeContent
import fr.skyle.scanny.domain.model.BarcodeFormat
import fr.skyle.scanny.domain.model.Creation
import fr.skyle.scanny.domain.model.ScannedCode
import fr.skyle.scanny.domain.payload.toCoordinateText
import fr.skyle.scanny.ui.core.code.mapper.toBarcodeType
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
 * code becomes a QR code holding its content. The editor holds one phone, one e-mail, one address and
 * one website for a contact: the first of each. A one-time password keeps its type, its digits, its
 * period and its algorithm, which the editor does not show.
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
            emailAddress = content.address.orEmpty(),
            emailSubject = content.subject.orEmpty(),
            emailBody = content.body.orEmpty(),
        )

        is BarcodeContent.Sms -> CreationForm(
            kind = CreationKind.SMS,
            smsPhoneNumber = content.phoneNumber.orEmpty(),
            smsMessage = content.message.orEmpty(),
        )

        is BarcodeContent.Wifi -> CreationForm(
            kind = CreationKind.WIFI,
            wifiSsid = content.ssid,
            wifiEncryption = content.encryption,
            wifiPassword = content.password.orEmpty(),
            isWifiHidden = content.isHidden,
        )

        is BarcodeContent.Contact -> CreationForm(
            kind = CreationKind.CONTACT,
            contactName = content.displayName.orEmpty(),
            contactPhone = content.phones.firstOrNull().orEmpty(),
            contactEmail = content.emails.firstOrNull().orEmpty(),
            contactOrganization = content.organization.orEmpty(),
            contactAddress = content.addresses.firstOrNull().orEmpty(),
            contactWebsite = content.urls.firstOrNull().orEmpty(),
        )

        is BarcodeContent.Phone -> CreationForm(
            kind = CreationKind.PHONE,
            phoneNumber = content.phoneNumber,
        )

        is BarcodeContent.Location -> CreationForm(
            kind = CreationKind.LOCATION,
            locationLatitude = content.latitude.toCoordinateText(),
            locationLongitude = content.longitude.toCoordinateText(),
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
            otpAlgorithm = content.algorithm,
            otpDigits = content.digits,
            otpPeriod = content.period,
            otpCounter = content.counter,
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
