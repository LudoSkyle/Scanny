package fr.skyle.scanny.platform.link

import fr.skyle.scanny.domain.model.BarcodeContent
import fr.skyle.scanny.domain.payload.toSmsLink

// Messages on iOS has no mms: link: the message goes through an sms: one, its subject on its first line
internal actual fun platformMmsLink(mms: BarcodeContent.Mms): String {
    val sms = BarcodeContent.Sms(
        phoneNumber = mms.phoneNumber,
        message = listOfNotNull(mms.subject, mms.message)
            .joinToString(separator = "\n")
            .ifEmpty { null },
    )

    return platformSmsLink(smsLink = sms.toSmsLink())
}