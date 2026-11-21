package fr.skyle.scanny.ui.screen.createcode.ui.mapper

import fr.skyle.scanny.domain.model.BarcodeContent
import fr.skyle.scanny.domain.model.ScannedCode
import fr.skyle.scanny.ui.core.code.mapper.toBarcodeType
import fr.skyle.scanny.ui.screen.createcode.ui.model.CreationForm
import fr.skyle.scanny.ui.screen.createcode.ui.model.CreationKind

/**
 * The form a scanned code fills, to be changed and created again. A barcode of a format the editor
 * draws stays a barcode of its value; any other code becomes a QR code holding its content. The
 * editor holds one phone, one e-mail, one address and one website for a contact: the first of each.
 */
internal fun ScannedCode.toCreationForm(): CreationForm {
    val barcodeFormat = format?.takeIf { format -> format.toBarcodeType() != null }
    if (barcodeFormat != null) {
        return CreationForm(
            kind = CreationKind.BARCODE,
            barcodeFormat = barcodeFormat,
            barcodeValue = rawValue,
        )
    }

    return when (val content = content) {
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
    }
}
