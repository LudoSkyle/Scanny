package fr.skyle.scanny.ui.screen.createcode.ui.model

import fr.skyle.scanny.domain.model.BarcodeFormat
import fr.skyle.scanny.domain.model.ContactFormat
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
    val emailAddress: String = "",
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
    val contactName: String = "",
    val contactPhone: String = "",
    val contactEmail: String = "",
    val contactOrganization: String = "",
    val contactAddress: String = "",
    val contactWebsite: String = "",
    val contactFormat: ContactFormat = ContactFormat.VCARD,
    // As typed: a decimal comma or point
    val locationLatitude: String = "",
    val locationLongitude: String = "",
    val locationLabel: String = "",
    val bookmarkUrl: String = "",
    val bookmarkTitle: String = "",
    val otpIssuer: String = "",
    val otpAccount: String = "",
    val otpSecret: String = "",
    // Kept from a scanned key, not shown: a code with other digits or another period gives other codes
    val otpType: OtpType = OtpType.TOTP,
    val otpAlgorithm: String? = null,
    val otpDigits: Int? = null,
    val otpPeriod: Int? = null,
    val otpCounter: Long? = null,
    val bitcoinAddress: String = "",
    // As typed: a decimal comma or point
    val bitcoinAmount: String = "",
    val bitcoinLabel: String = "",
    val bitcoinMessage: String = "",
    val barcodeFormat: BarcodeFormat = BarcodeFormat.EAN_13,
    val barcodeValue: String = "",
)