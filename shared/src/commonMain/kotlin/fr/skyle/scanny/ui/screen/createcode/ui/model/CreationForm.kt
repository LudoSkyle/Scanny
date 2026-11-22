package fr.skyle.scanny.ui.screen.createcode.ui.model

import fr.skyle.scanny.domain.model.BarcodeContent
import fr.skyle.scanny.domain.model.BarcodeFormat
import fr.skyle.scanny.domain.model.ContactFormat
import fr.skyle.scanny.domain.model.EventClassification
import fr.skyle.scanny.domain.model.EventFrequency
import fr.skyle.scanny.domain.model.EventStatus
import fr.skyle.scanny.domain.model.EventTransparency
import fr.skyle.scanny.domain.model.OtpAlgorithm
import fr.skyle.scanny.domain.model.OtpType
import fr.skyle.scanny.domain.model.WifiEncryption
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

/**
 * What the user typed in the editor, the fields of every kind together: switching to another kind
 * and back loses nothing.
 */
data class CreationForm(
    val kind: CreationKind = CreationKind.TEXT,
    val text: String = "",
    val url: String = "",
    // As typed: addresses separated by commas
    val emailTo: String = "",
    val emailCc: String = "",
    val emailBcc: String = "",
    val emailSubject: String = "",
    val emailBody: String = "",
    // The number and the message of an SMS or of an MMS, kept from one to the other
    val smsPhoneNumber: String = "",
    val smsMessage: String = "",
    val mmsSubject: String = "",
    val phoneNumber: String = "",
    val wifiSsid: String = "",
    val wifiEncryption: WifiEncryption = WifiEncryption.WPA,
    val wifiPassword: String = "",
    val isWifiHidden: Boolean = false,
    // The modes not to fall back from, one per kind of network
    val isWifiWpa3Only: Boolean = false,
    val isWifiEnhancedOpenOnly: Boolean = false,
    val isWifiWpa3EnterpriseOnly: Boolean = false,
    val wifiPasswordIdentifier: String = "",
    val wifiPublicKey: String = "",
    val wifiEapMethod: String = "PEAP",
    val wifiIdentity: String = "",
    val wifiAnonymousIdentity: String = "",
    // Null for none
    val wifiPhase2Method: String? = "MSCHAPV2",
    // A contact: its name in parts, and as shown when it differs from them
    val contactNamePrefix: String = "",
    val contactFirstName: String = "",
    val contactMiddleName: String = "",
    val contactLastName: String = "",
    val contactNameSuffix: String = "",
    val contactDisplayName: String = "",
    val contactNickname: String = "",
    val contactOrganization: String = "",
    val contactDepartment: String = "",
    val contactTitle: String = "",
    // A row to fill for a telephone, an e-mail and a website, none for an address, which takes more room
    val contactPhones: List<ContactPhoneField> = listOf(ContactPhoneField()),
    val contactEmails: List<ContactEntryField> = listOf(ContactEntryField()),
    val contactAddresses: List<ContactAddressField> = emptyList(),
    val contactWebsites: List<ContactEntryField> = listOf(ContactEntryField()),
    // In ISO 8601, empty when none
    val contactBirthday: String = "",
    val contactNote: String = "",
    val contactFormat: ContactFormat = ContactFormat.VCARD,
    // As typed: a decimal comma or point
    val locationLatitude: String = "",
    val locationLongitude: String = "",
    val locationAltitude: String = "",
    val locationUncertainty: String = "",
    val locationLabel: String = "",
    val bookmarkUrl: String = "",
    val bookmarkTitle: String = "",
    val otpIssuer: String = "",
    val otpAccount: String = "",
    val otpSecret: String = "",
    val otpType: OtpType = OtpType.TOTP,
    val otpAlgorithm: OtpAlgorithm = BarcodeContent.Otp.DEFAULT_ALGORITHM,
    val otpDigits: Int = BarcodeContent.Otp.DEFAULT_DIGITS,
    // As typed: the period of a TOTP key, in seconds, and the counter of a HOTP one
    val otpPeriod: String = BarcodeContent.Otp.DEFAULT_PERIOD.toString(),
    val otpCounter: String = "",
    val bitcoinAddress: String = "",
    // As typed: a decimal comma or point
    val bitcoinAmount: String = "",
    val bitcoinLabel: String = "",
    val bitcoinMessage: String = "",
    val eventSummary: String = "",
    val isEventAllDay: Boolean = false,
    val eventStartDate: LocalDate? = null,
    val eventStartTime: LocalTime? = null,
    // The last day of an event of whole days, as the user counts it: iCalendar ends it the day after
    val eventEndDate: LocalDate? = null,
    val eventEndTime: LocalTime? = null,
    // The zone of the times, null for the time wherever one is: the zone of the device for a new event
    val eventTimeZone: String? = null,
    val eventLocation: String = "",
    // As typed: a decimal comma or point
    val eventLatitude: String = "",
    val eventLongitude: String = "",
    val eventDescription: String = "",
    val eventUrl: String = "",
    val eventOrganizer: String = "",
    // As typed: addresses separated by commas
    val eventAttendees: String = "",
    val eventFrequency: EventFrequency? = null,
    // As typed: every how many periods, 1 when empty
    val eventInterval: String = "",
    val eventDays: Set<DayOfWeek> = emptySet(),
    val eventRecurrenceEnd: RecurrenceEnd = RecurrenceEnd.NEVER,
    val eventCount: String = "",
    val eventUntil: LocalDate? = null,
    val eventReminderMinutes: Int? = null,
    val eventStatus: EventStatus? = null,
    val eventTransparency: EventTransparency? = null,
    val eventClassification: EventClassification? = null,
    val eventPriority: Int? = null,
    // As typed: categories separated by commas
    val eventCategories: String = "",
    val barcodeFormat: BarcodeFormat = BarcodeFormat.EAN_13,
    val barcodeValue: String = "",
)