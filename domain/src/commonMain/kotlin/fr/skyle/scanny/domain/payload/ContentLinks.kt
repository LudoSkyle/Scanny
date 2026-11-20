package fr.skyle.scanny.domain.payload

import fr.skyle.scanny.domain.model.BarcodeContent

// The links that open an app on a content: public, unlike the encoder they reuse

/** The mailto: link that opens the mail app on this e-mail, written. */
fun BarcodeContent.Email.toMailtoLink(): String =
    BarcodeContentEncoder.encode(content = this)

/** The sms: link of RFC 5724 that opens the messages app on this message, written. */
fun BarcodeContent.Sms.toSmsLink(): String =
    BarcodeContentEncoder.smsLink(sms = this)