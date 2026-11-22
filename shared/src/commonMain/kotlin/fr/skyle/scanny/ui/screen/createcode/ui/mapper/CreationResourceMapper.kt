package fr.skyle.scanny.ui.screen.createcode.ui.mapper

import fr.skyle.scanny.domain.model.CodeErrorCorrection
import fr.skyle.scanny.domain.model.CodeGradient
import fr.skyle.scanny.domain.model.CodeLogoMargin
import fr.skyle.scanny.domain.model.CodeShape
import fr.skyle.scanny.domain.model.ContactEntryType
import fr.skyle.scanny.domain.model.ContactFormat
import fr.skyle.scanny.domain.model.ContactPhoneType
import fr.skyle.scanny.domain.model.WifiEncryption
import fr.skyle.scanny.resources.Res
import fr.skyle.scanny.resources.barcode
import fr.skyle.scanny.resources.bitcoin
import fr.skyle.scanny.resources.bookmark
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
import fr.skyle.scanny.resources.content_too_long_for_logo
import fr.skyle.scanny.resources.email
import fr.skyle.scanny.resources.error_correction_auto
import fr.skyle.scanny.resources.error_correction_auto_description
import fr.skyle.scanny.resources.error_correction_high
import fr.skyle.scanny.resources.error_correction_high_description
import fr.skyle.scanny.resources.error_correction_low
import fr.skyle.scanny.resources.error_correction_low_description
import fr.skyle.scanny.resources.error_correction_medium
import fr.skyle.scanny.resources.error_correction_medium_description
import fr.skyle.scanny.resources.error_correction_quartile
import fr.skyle.scanny.resources.error_correction_quartile_description
import fr.skyle.scanny.resources.error_correction_too_high_for_content
import fr.skyle.scanny.resources.error_correction_too_low_for_logo
import fr.skyle.scanny.resources.fax
import fr.skyle.scanny.resources.gradient_linear
import fr.skyle.scanny.resources.gradient_none
import fr.skyle.scanny.resources.gradient_radial
import fr.skyle.scanny.resources.link
import fr.skyle.scanny.resources.location
import fr.skyle.scanny.resources.logo_margin_large
import fr.skyle.scanny.resources.logo_margin_none
import fr.skyle.scanny.resources.logo_margin_normal
import fr.skyle.scanny.resources.logo_needs_highest_correction
import fr.skyle.scanny.resources.mecard
import fr.skyle.scanny.resources.mms
import fr.skyle.scanny.resources.mobile
import fr.skyle.scanny.resources.otp
import fr.skyle.scanny.resources.phone
import fr.skyle.scanny.resources.shape_circle
import fr.skyle.scanny.resources.shape_rounded
import fr.skyle.scanny.resources.shape_square
import fr.skyle.scanny.resources.sms
import fr.skyle.scanny.resources.text
import fr.skyle.scanny.resources.type_home
import fr.skyle.scanny.resources.type_other
import fr.skyle.scanny.resources.type_work
import fr.skyle.scanny.resources.vcard
import fr.skyle.scanny.resources.wifi
import fr.skyle.scanny.resources.wifi_security_none
import fr.skyle.scanny.resources.wifi_security_wep
import fr.skyle.scanny.resources.wifi_security_wpa
import fr.skyle.scanny.resources.wifi_security_wpa_enterprise
import fr.skyle.scanny.ui.screen.createcode.ui.model.CodeBackgroundColor
import fr.skyle.scanny.ui.screen.createcode.ui.model.CodeDotColor
import fr.skyle.scanny.ui.screen.createcode.ui.model.CreationKind
import fr.skyle.scanny.ui.screen.createcode.ui.model.ErrorCorrectionBlocker
import fr.skyle.scanny.ui.screen.createcode.ui.model.LogoBlocker
import org.jetbrains.compose.resources.StringResource

internal fun CreationKind.toText(): StringResource =
    when (this) {
        CreationKind.TEXT -> Res.string.text
        CreationKind.LINK -> Res.string.link
        CreationKind.EMAIL -> Res.string.email
        CreationKind.SMS -> Res.string.sms
        CreationKind.MMS -> Res.string.mms
        CreationKind.PHONE -> Res.string.phone
        CreationKind.WIFI -> Res.string.wifi
        CreationKind.CONTACT -> Res.string.contact
        CreationKind.LOCATION -> Res.string.location
        CreationKind.BOOKMARK -> Res.string.bookmark
        CreationKind.OTP -> Res.string.otp
        CreationKind.BITCOIN -> Res.string.bitcoin
        CreationKind.BARCODE -> Res.string.barcode
    }

internal fun ContactFormat.toText(): StringResource =
    when (this) {
        ContactFormat.VCARD -> Res.string.vcard
        ContactFormat.MECARD -> Res.string.mecard
    }

internal fun ContactPhoneType.toText(): StringResource =
    when (this) {
        ContactPhoneType.MOBILE -> Res.string.mobile
        ContactPhoneType.HOME -> Res.string.type_home
        ContactPhoneType.WORK -> Res.string.type_work
        ContactPhoneType.FAX -> Res.string.fax
        ContactPhoneType.OTHER -> Res.string.type_other
    }

internal fun ContactEntryType.toText(): StringResource =
    when (this) {
        ContactEntryType.HOME -> Res.string.type_home
        ContactEntryType.WORK -> Res.string.type_work
        ContactEntryType.OTHER -> Res.string.type_other
    }

internal fun WifiEncryption.toText(): StringResource =
    when (this) {
        WifiEncryption.NONE -> Res.string.wifi_security_none
        WifiEncryption.WEP -> Res.string.wifi_security_wep
        WifiEncryption.WPA -> Res.string.wifi_security_wpa
        WifiEncryption.WPA_ENTERPRISE -> Res.string.wifi_security_wpa_enterprise
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

internal fun CodeErrorCorrection.toText(): StringResource =
    when (this) {
        CodeErrorCorrection.AUTO -> Res.string.error_correction_auto
        CodeErrorCorrection.LOW -> Res.string.error_correction_low
        CodeErrorCorrection.MEDIUM -> Res.string.error_correction_medium
        CodeErrorCorrection.QUARTILE -> Res.string.error_correction_quartile
        CodeErrorCorrection.HIGH -> Res.string.error_correction_high
    }

internal fun CodeErrorCorrection.toDescription(): StringResource =
    when (this) {
        CodeErrorCorrection.AUTO -> Res.string.error_correction_auto_description
        CodeErrorCorrection.LOW -> Res.string.error_correction_low_description
        CodeErrorCorrection.MEDIUM -> Res.string.error_correction_medium_description
        CodeErrorCorrection.QUARTILE -> Res.string.error_correction_quartile_description
        CodeErrorCorrection.HIGH -> Res.string.error_correction_high_description
    }

internal fun ErrorCorrectionBlocker.toText(): StringResource =
    when (this) {
        ErrorCorrectionBlocker.TOO_HIGH_FOR_CONTENT -> Res.string.error_correction_too_high_for_content
        ErrorCorrectionBlocker.TOO_LOW_FOR_LOGO -> Res.string.error_correction_too_low_for_logo
    }

internal fun LogoBlocker.toText(): StringResource =
    when (this) {
        LogoBlocker.CORRECTION_TOO_LOW -> Res.string.logo_needs_highest_correction
        LogoBlocker.CONTENT_TOO_LONG -> Res.string.content_too_long_for_logo
    }

internal fun CodeGradient.toText(): StringResource =
    when (this) {
        CodeGradient.NONE -> Res.string.gradient_none
        CodeGradient.LINEAR -> Res.string.gradient_linear
        CodeGradient.RADIAL -> Res.string.gradient_radial
    }

internal fun CodeLogoMargin.toText(): StringResource =
    when (this) {
        CodeLogoMargin.NONE -> Res.string.logo_margin_none
        CodeLogoMargin.NORMAL -> Res.string.logo_margin_normal
        CodeLogoMargin.LARGE -> Res.string.logo_margin_large
    }
