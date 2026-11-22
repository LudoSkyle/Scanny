package fr.skyle.scanny.domain.payload

import fr.skyle.scanny.domain.model.BarcodeContent

// The links that open an app on a content: public, unlike the encoder they reuse

/** The mailto: link that opens the mail app on this e-mail, written. */
fun BarcodeContent.Email.toMailtoLink(): String =
    BarcodeContentEncoder.encode(content = this)

/** The sms: link of RFC 5724 that opens the messages app on this message, written. */
fun BarcodeContent.Sms.toSmsLink(): String =
    BarcodeContentEncoder.smsLink(sms = this)

/** The tel: link that opens the phone app on this number, to call it. */
fun BarcodeContent.Phone.toTelLink(): String =
    BarcodeContentEncoder.encode(content = this)

/** The geo: link of RFC 5870 that opens the maps of Android on this place, pinned. */
fun BarcodeContent.Location.toGeoLink(): String =
    BarcodeContentEncoder.geoLink(location = this)

/** The link of Apple Maps on this place, pinned: iOS opens it in Maps, and has no app for a geo: link. */
fun BarcodeContent.Location.toAppleMapsLink(): String =
    BarcodeContentEncoder.appleMapsLink(location = this)
