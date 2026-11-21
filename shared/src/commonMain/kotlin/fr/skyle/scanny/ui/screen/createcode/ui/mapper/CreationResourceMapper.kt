package fr.skyle.scanny.ui.screen.createcode.ui.mapper

import fr.skyle.scanny.domain.model.WifiEncryption
import fr.skyle.scanny.resources.Res
import fr.skyle.scanny.resources.barcode
import fr.skyle.scanny.resources.contact
import fr.skyle.scanny.resources.email
import fr.skyle.scanny.resources.link
import fr.skyle.scanny.resources.sms
import fr.skyle.scanny.resources.text
import fr.skyle.scanny.resources.wifi
import fr.skyle.scanny.resources.wifi_security_none
import fr.skyle.scanny.resources.wifi_security_wep
import fr.skyle.scanny.resources.wifi_security_wpa
import fr.skyle.scanny.resources.wifi_security_wpa3
import fr.skyle.scanny.ui.screen.createcode.ui.model.CreationKind
import org.jetbrains.compose.resources.StringResource

internal fun CreationKind.toText(): StringResource =
    when (this) {
        CreationKind.TEXT -> Res.string.text
        CreationKind.LINK -> Res.string.link
        CreationKind.EMAIL -> Res.string.email
        CreationKind.SMS -> Res.string.sms
        CreationKind.WIFI -> Res.string.wifi
        CreationKind.CONTACT -> Res.string.contact
        CreationKind.BARCODE -> Res.string.barcode
    }

internal fun WifiEncryption.toText(): StringResource =
    when (this) {
        WifiEncryption.NONE -> Res.string.wifi_security_none
        WifiEncryption.WEP -> Res.string.wifi_security_wep
        WifiEncryption.WPA -> Res.string.wifi_security_wpa
        WifiEncryption.WPA3 -> Res.string.wifi_security_wpa3
    }