package fr.skyle.scanny.ui.screen.createcode.ui.model

import fr.skyle.scanny.domain.model.BarcodeFormat
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
    val smsPhoneNumber: String = "",
    val smsMessage: String = "",
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
    val barcodeFormat: BarcodeFormat = BarcodeFormat.EAN_13,
    val barcodeValue: String = "",
)