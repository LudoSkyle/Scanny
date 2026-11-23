package fr.skyle.scanny.domain.payload

import fr.skyle.scanny.domain.model.BarcodeContent

// The links that open an app on a content: public, unlike the encoder they reuse

/** The mailto: link that opens the mail app on this e-mail, written. */
fun BarcodeContent.Email.toMailtoLink(): String =
    BarcodeContentEncoder.encode(content = this)

/** The sms: link of RFC 5724 that opens the messages app on this message, written. */
fun BarcodeContent.Sms.toSmsLink(): String =
    BarcodeContentEncoder.smsLink(sms = this)

/**
 * The mms: link that opens the messages app on this message, written, its subject on its first line: the
 * messages apps leave out the subject of a link.
 */
fun BarcodeContent.Mms.toMmsLink(): String =
    BarcodeContentEncoder.mmsLink(mms = this)

/** The tel: link that opens the phone app on this number, to call it. */
fun BarcodeContent.Phone.toTelLink(): String =
    BarcodeContentEncoder.encode(content = this)

/** The geo: link of RFC 5870 that opens the maps of Android on this place, pinned. */
fun BarcodeContent.Location.toGeoLink(): String =
    BarcodeContentEncoder.geoLink(location = this)

/** The link of Apple Maps on this place, pinned: iOS opens it in Maps, and has no app for a geo: link. */
fun BarcodeContent.Location.toAppleMapsLink(): String =
    BarcodeContentEncoder.appleMapsLink(location = this)

/** The otpauth:// link that opens an authenticator app on this key, to add it. */
fun BarcodeContent.Otp.toOtpauthLink(): String =
    BarcodeContentEncoder.encode(content = this)

/** The bitcoin: link of BIP 21 that opens a wallet on this payment. */
fun BarcodeContent.Bitcoin.toBitcoinLink(): String =
    BarcodeContentEncoder.encode(content = this)

/** The RRULE of iCalendar this event repeats with, as the calendar of Android takes it, or null. */
fun BarcodeContent.Event.toRecurrenceRule(): String? =
    recurrence?.let { recurrence -> BarcodeContentEncoder.recurrenceRule(recurrence = recurrence, start = start) }
