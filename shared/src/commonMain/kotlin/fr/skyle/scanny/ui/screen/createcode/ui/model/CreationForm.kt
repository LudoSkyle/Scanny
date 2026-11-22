package fr.skyle.scanny.ui.screen.createcode.ui.model

import fr.skyle.scanny.domain.model.BarcodeContent
import fr.skyle.scanny.domain.model.BarcodeFormat
import fr.skyle.scanny.domain.model.ContactFormat
import fr.skyle.scanny.domain.model.OtpAlgorithm
import fr.skyle.scanny.domain.model.OtpType
import fr.skyle.scanny.domain.model.WifiEncryption

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
    val barcodeFormat: BarcodeFormat = BarcodeFormat.EAN_13,
    val barcodeValue: String = "",
)