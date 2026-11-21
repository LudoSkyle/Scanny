package fr.skyle.scanny.ui.screen.createcode.ui.mapper

import fr.skyle.scanny.domain.model.CodeShape
import fr.skyle.scanny.domain.model.WifiEncryption
import fr.skyle.scanny.resources.Res
import fr.skyle.scanny.resources.barcode
import fr.skyle.scanny.resources.color_black
import fr.skyle.scanny.resources.color_brown
import fr.skyle.scanny.resources.color_cream
import fr.skyle.scanny.resources.color_green
import fr.skyle.scanny.resources.color_indigo
import fr.skyle.scanny.resources.color_lavender
import fr.skyle.scanny.resources.color_light_blue
import fr.skyle.scanny.resources.color_light_green
import fr.skyle.scanny.resources.color_pink
import fr.skyle.scanny.resources.color_purple
import fr.skyle.scanny.resources.color_red
import fr.skyle.scanny.resources.color_white
import fr.skyle.scanny.resources.contact
import fr.skyle.scanny.resources.email
import fr.skyle.scanny.resources.link
import fr.skyle.scanny.resources.shape_circle
import fr.skyle.scanny.resources.shape_rounded
import fr.skyle.scanny.resources.shape_square
import fr.skyle.scanny.resources.sms
import fr.skyle.scanny.resources.text
import fr.skyle.scanny.resources.wifi
import fr.skyle.scanny.resources.wifi_security_none
import fr.skyle.scanny.resources.wifi_security_wep
import fr.skyle.scanny.resources.wifi_security_wpa
import fr.skyle.scanny.resources.wifi_security_wpa3
import fr.skyle.scanny.ui.screen.createcode.ui.model.CodeBackgroundColor
import fr.skyle.scanny.ui.screen.createcode.ui.model.CodeDotColor
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

internal fun CodeShape.toText(): StringResource =
    when (this) {
        CodeShape.SQUARE -> Res.string.shape_square
        CodeShape.ROUNDED -> Res.string.shape_rounded
        CodeShape.CIRCLE -> Res.string.shape_circle
    }

internal fun CodeDotColor.toText(): StringResource =
    when (this) {
        CodeDotColor.BLACK -> Res.string.color_black
        CodeDotColor.INDIGO -> Res.string.color_indigo
        CodeDotColor.GREEN -> Res.string.color_green
        CodeDotColor.RED -> Res.string.color_red
        CodeDotColor.PURPLE -> Res.string.color_purple
        CodeDotColor.BROWN -> Res.string.color_brown
    }

internal fun CodeBackgroundColor.toText(): StringResource =
    when (this) {
        CodeBackgroundColor.WHITE -> Res.string.color_white
        CodeBackgroundColor.CREAM -> Res.string.color_cream
        CodeBackgroundColor.LIGHT_BLUE -> Res.string.color_light_blue
        CodeBackgroundColor.LIGHT_GREEN -> Res.string.color_light_green
        CodeBackgroundColor.PINK -> Res.string.color_pink
        CodeBackgroundColor.LAVENDER -> Res.string.color_lavender
    }
