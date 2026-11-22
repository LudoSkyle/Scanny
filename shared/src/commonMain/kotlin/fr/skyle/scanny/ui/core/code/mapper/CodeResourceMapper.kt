package fr.skyle.scanny.ui.core.code.mapper

import fr.skyle.scanny.domain.model.BarcodeFormat
import fr.skyle.scanny.resources.Res
import fr.skyle.scanny.resources.account
import fr.skyle.scanny.resources.add_contact
import fr.skyle.scanny.resources.add_to_authenticator
import fr.skyle.scanny.resources.address
import fr.skyle.scanny.resources.algorithm
import fr.skyle.scanny.resources.altitude
import fr.skyle.scanny.resources.amount
import fr.skyle.scanny.resources.aztec
import fr.skyle.scanny.resources.bitcoin
import fr.skyle.scanny.resources.blind_carbon_copy
import fr.skyle.scanny.resources.bookmark
import fr.skyle.scanny.resources.call
import fr.skyle.scanny.resources.carbon_copy
import fr.skyle.scanny.resources.codabar
import fr.skyle.scanny.resources.code_128
import fr.skyle.scanny.resources.code_39
import fr.skyle.scanny.resources.code_93
import fr.skyle.scanny.resources.contact
import fr.skyle.scanny.resources.coordinates
import fr.skyle.scanny.resources.counter
import fr.skyle.scanny.resources.data_matrix
import fr.skyle.scanny.resources.digits
import fr.skyle.scanny.resources.ean_13
import fr.skyle.scanny.resources.ean_8
import fr.skyle.scanny.resources.email
import fr.skyle.scanny.resources.format_aztec_example
import fr.skyle.scanny.resources.format_aztec_structure
import fr.skyle.scanny.resources.format_aztec_use
import fr.skyle.scanny.resources.format_codabar_example
import fr.skyle.scanny.resources.format_codabar_structure
import fr.skyle.scanny.resources.format_codabar_use
import fr.skyle.scanny.resources.format_code_128_example
import fr.skyle.scanny.resources.format_code_128_structure
import fr.skyle.scanny.resources.format_code_128_use
import fr.skyle.scanny.resources.format_code_39_example
import fr.skyle.scanny.resources.format_code_39_structure
import fr.skyle.scanny.resources.format_code_39_use
import fr.skyle.scanny.resources.format_code_93_example
import fr.skyle.scanny.resources.format_code_93_structure
import fr.skyle.scanny.resources.format_code_93_use
import fr.skyle.scanny.resources.format_data_matrix_example
import fr.skyle.scanny.resources.format_data_matrix_structure
import fr.skyle.scanny.resources.format_data_matrix_use
import fr.skyle.scanny.resources.format_ean_13_example
import fr.skyle.scanny.resources.format_ean_13_structure
import fr.skyle.scanny.resources.format_ean_13_use
import fr.skyle.scanny.resources.format_ean_8_example
import fr.skyle.scanny.resources.format_ean_8_structure
import fr.skyle.scanny.resources.format_ean_8_use
import fr.skyle.scanny.resources.format_itf_example
import fr.skyle.scanny.resources.format_itf_structure
import fr.skyle.scanny.resources.format_itf_use
import fr.skyle.scanny.resources.format_pdf417_example
import fr.skyle.scanny.resources.format_pdf417_structure
import fr.skyle.scanny.resources.format_pdf417_use
import fr.skyle.scanny.resources.format_qr_code_example
import fr.skyle.scanny.resources.format_qr_code_structure
import fr.skyle.scanny.resources.format_qr_code_use
import fr.skyle.scanny.resources.format_upc_a_example
import fr.skyle.scanny.resources.format_upc_a_structure
import fr.skyle.scanny.resources.format_upc_a_use
import fr.skyle.scanny.resources.format_upc_e_example
import fr.skyle.scanny.resources.format_upc_e_structure
import fr.skyle.scanny.resources.format_upc_e_use
import fr.skyle.scanny.resources.ic_bookmark
import fr.skyle.scanny.resources.ic_call
import fr.skyle.scanny.resources.ic_currency_bitcoin
import fr.skyle.scanny.resources.ic_link
import fr.skyle.scanny.resources.ic_location_on
import fr.skyle.scanny.resources.ic_mail
import fr.skyle.scanny.resources.ic_mms
import fr.skyle.scanny.resources.ic_notes
import fr.skyle.scanny.resources.ic_password
import fr.skyle.scanny.resources.ic_person
import fr.skyle.scanny.resources.ic_sms
import fr.skyle.scanny.resources.ic_wifi
import fr.skyle.scanny.resources.issuer
import fr.skyle.scanny.resources.itf
import fr.skyle.scanny.resources.job_title
import fr.skyle.scanny.resources.join_network
import fr.skyle.scanny.resources.link
import fr.skyle.scanny.resources.location
import fr.skyle.scanny.resources.message
import fr.skyle.scanny.resources.mms
import fr.skyle.scanny.resources.name
import fr.skyle.scanny.resources.network
import fr.skyle.scanny.resources.open_in_wallet
import fr.skyle.scanny.resources.open_link
import fr.skyle.scanny.resources.organization
import fr.skyle.scanny.resources.otp
import fr.skyle.scanny.resources.otp_type
import fr.skyle.scanny.resources.password
import fr.skyle.scanny.resources.pdf417
import fr.skyle.scanny.resources.period
import fr.skyle.scanny.resources.phone
import fr.skyle.scanny.resources.phone_number
import fr.skyle.scanny.resources.place
import fr.skyle.scanny.resources.qr_code
import fr.skyle.scanny.resources.recipient
import fr.skyle.scanny.resources.secret_key
import fr.skyle.scanny.resources.security
import fr.skyle.scanny.resources.show_on_map
import fr.skyle.scanny.resources.sms
import fr.skyle.scanny.resources.subject
import fr.skyle.scanny.resources.text
import fr.skyle.scanny.resources.title
import fr.skyle.scanny.resources.uncertainty
import fr.skyle.scanny.resources.upc_a
import fr.skyle.scanny.resources.upc_e
import fr.skyle.scanny.resources.website
import fr.skyle.scanny.resources.wifi
import fr.skyle.scanny.resources.write_email
import fr.skyle.scanny.resources.write_mms
import fr.skyle.scanny.resources.write_sms
import fr.skyle.scanny.ui.core.code.model.CodeAction
import fr.skyle.scanny.ui.core.code.model.CodeFieldLabel
import fr.skyle.scanny.ui.core.code.model.CodeType
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource

internal fun CodeType.toText(): StringResource =
    when (this) {
        CodeType.TEXT -> Res.string.text
        CodeType.LINK -> Res.string.link
        CodeType.EMAIL -> Res.string.email
        CodeType.SMS -> Res.string.sms
        CodeType.MMS -> Res.string.mms
        CodeType.WIFI -> Res.string.wifi
        CodeType.CONTACT -> Res.string.contact
        CodeType.PHONE -> Res.string.phone
        CodeType.LOCATION -> Res.string.location
        CodeType.BOOKMARK -> Res.string.bookmark
        CodeType.OTP -> Res.string.otp
        CodeType.BITCOIN -> Res.string.bitcoin
    }

internal fun CodeType.toIcon(): DrawableResource =
    when (this) {
        CodeType.TEXT -> Res.drawable.ic_notes
        CodeType.LINK -> Res.drawable.ic_link
        CodeType.EMAIL -> Res.drawable.ic_mail
        CodeType.SMS -> Res.drawable.ic_sms
        CodeType.MMS -> Res.drawable.ic_mms
        CodeType.WIFI -> Res.drawable.ic_wifi
        CodeType.CONTACT -> Res.drawable.ic_person
        CodeType.PHONE -> Res.drawable.ic_call
        CodeType.LOCATION -> Res.drawable.ic_location_on
        CodeType.BOOKMARK -> Res.drawable.ic_bookmark
        CodeType.OTP -> Res.drawable.ic_password
        CodeType.BITCOIN -> Res.drawable.ic_currency_bitcoin
    }

internal fun BarcodeFormat.toText(): StringResource =
    when (this) {
        BarcodeFormat.QR_CODE -> Res.string.qr_code
        BarcodeFormat.DATA_MATRIX -> Res.string.data_matrix
        BarcodeFormat.AZTEC -> Res.string.aztec
        BarcodeFormat.PDF417 -> Res.string.pdf417
        BarcodeFormat.EAN_13 -> Res.string.ean_13
        BarcodeFormat.EAN_8 -> Res.string.ean_8
        BarcodeFormat.UPC_A -> Res.string.upc_a
        BarcodeFormat.UPC_E -> Res.string.upc_e
        BarcodeFormat.CODE_39 -> Res.string.code_39
        BarcodeFormat.CODE_93 -> Res.string.code_93
        BarcodeFormat.CODE_128 -> Res.string.code_128
        BarcodeFormat.ITF -> Res.string.itf
        BarcodeFormat.CODABAR -> Res.string.codabar
    }

internal fun CodeFieldLabel.toText(): StringResource =
    when (this) {
        CodeFieldLabel.NETWORK -> Res.string.network
        CodeFieldLabel.SECURITY -> Res.string.security
        CodeFieldLabel.PASSWORD -> Res.string.password
        CodeFieldLabel.RECIPIENT -> Res.string.recipient
        CodeFieldLabel.CARBON_COPY -> Res.string.carbon_copy
        CodeFieldLabel.BLIND_CARBON_COPY -> Res.string.blind_carbon_copy
        CodeFieldLabel.SUBJECT -> Res.string.subject
        CodeFieldLabel.MESSAGE -> Res.string.message
        CodeFieldLabel.PHONE_NUMBER -> Res.string.phone_number
        CodeFieldLabel.NAME -> Res.string.name
        CodeFieldLabel.ORGANIZATION -> Res.string.organization
        CodeFieldLabel.JOB_TITLE -> Res.string.job_title
        CodeFieldLabel.PHONE -> Res.string.phone
        CodeFieldLabel.EMAIL -> Res.string.email
        CodeFieldLabel.ADDRESS -> Res.string.address
        CodeFieldLabel.WEBSITE -> Res.string.website
        CodeFieldLabel.PLACE -> Res.string.place
        CodeFieldLabel.COORDINATES -> Res.string.coordinates
        CodeFieldLabel.ALTITUDE -> Res.string.altitude
        CodeFieldLabel.UNCERTAINTY -> Res.string.uncertainty
        CodeFieldLabel.TITLE -> Res.string.title
        CodeFieldLabel.LINK -> Res.string.link
        CodeFieldLabel.ISSUER -> Res.string.issuer
        CodeFieldLabel.ACCOUNT -> Res.string.account
        CodeFieldLabel.SECRET_KEY -> Res.string.secret_key
        CodeFieldLabel.OTP_TYPE -> Res.string.otp_type
        CodeFieldLabel.ALGORITHM -> Res.string.algorithm
        CodeFieldLabel.DIGITS -> Res.string.digits
        CodeFieldLabel.PERIOD -> Res.string.period
        CodeFieldLabel.COUNTER -> Res.string.counter
        CodeFieldLabel.AMOUNT -> Res.string.amount
    }

internal fun CodeAction.toText(): StringResource =
    when (this) {
        is CodeAction.OpenLink -> Res.string.open_link
        is CodeAction.WriteEmail -> Res.string.write_email
        is CodeAction.WriteSms -> Res.string.write_sms
        is CodeAction.WriteMms -> Res.string.write_mms
        is CodeAction.Call -> Res.string.call
        is CodeAction.ShowOnMap -> Res.string.show_on_map
        is CodeAction.AddToAuthenticator -> Res.string.add_to_authenticator
        is CodeAction.OpenWallet -> Res.string.open_in_wallet
        is CodeAction.AddContact -> Res.string.add_contact
        is CodeAction.JoinWifi -> Res.string.join_network
    }

/** Where a code of this format is found. */
internal fun BarcodeFormat.toUse(): StringResource =
    when (this) {
        BarcodeFormat.QR_CODE -> Res.string.format_qr_code_use
        BarcodeFormat.DATA_MATRIX -> Res.string.format_data_matrix_use
        BarcodeFormat.AZTEC -> Res.string.format_aztec_use
        BarcodeFormat.PDF417 -> Res.string.format_pdf417_use
        BarcodeFormat.EAN_13 -> Res.string.format_ean_13_use
        BarcodeFormat.EAN_8 -> Res.string.format_ean_8_use
        BarcodeFormat.UPC_A -> Res.string.format_upc_a_use
        BarcodeFormat.UPC_E -> Res.string.format_upc_e_use
        BarcodeFormat.CODE_39 -> Res.string.format_code_39_use
        BarcodeFormat.CODE_93 -> Res.string.format_code_93_use
        BarcodeFormat.CODE_128 -> Res.string.format_code_128_use
        BarcodeFormat.ITF -> Res.string.format_itf_use
        BarcodeFormat.CODABAR -> Res.string.format_codabar_use
    }

/** How a code of this format is made. */
internal fun BarcodeFormat.toStructure(): StringResource =
    when (this) {
        BarcodeFormat.QR_CODE -> Res.string.format_qr_code_structure
        BarcodeFormat.DATA_MATRIX -> Res.string.format_data_matrix_structure
        BarcodeFormat.AZTEC -> Res.string.format_aztec_structure
        BarcodeFormat.PDF417 -> Res.string.format_pdf417_structure
        BarcodeFormat.EAN_13 -> Res.string.format_ean_13_structure
        BarcodeFormat.EAN_8 -> Res.string.format_ean_8_structure
        BarcodeFormat.UPC_A -> Res.string.format_upc_a_structure
        BarcodeFormat.UPC_E -> Res.string.format_upc_e_structure
        BarcodeFormat.CODE_39 -> Res.string.format_code_39_structure
        BarcodeFormat.CODE_93 -> Res.string.format_code_93_structure
        BarcodeFormat.CODE_128 -> Res.string.format_code_128_structure
        BarcodeFormat.ITF -> Res.string.format_itf_structure
        BarcodeFormat.CODABAR -> Res.string.format_codabar_structure
    }

/** A code of this format, taken apart. */
internal fun BarcodeFormat.toExample(): StringResource =
    when (this) {
        BarcodeFormat.QR_CODE -> Res.string.format_qr_code_example
        BarcodeFormat.DATA_MATRIX -> Res.string.format_data_matrix_example
        BarcodeFormat.AZTEC -> Res.string.format_aztec_example
        BarcodeFormat.PDF417 -> Res.string.format_pdf417_example
        BarcodeFormat.EAN_13 -> Res.string.format_ean_13_example
        BarcodeFormat.EAN_8 -> Res.string.format_ean_8_example
        BarcodeFormat.UPC_A -> Res.string.format_upc_a_example
        BarcodeFormat.UPC_E -> Res.string.format_upc_e_example
        BarcodeFormat.CODE_39 -> Res.string.format_code_39_example
        BarcodeFormat.CODE_93 -> Res.string.format_code_93_example
        BarcodeFormat.CODE_128 -> Res.string.format_code_128_example
        BarcodeFormat.ITF -> Res.string.format_itf_example
        BarcodeFormat.CODABAR -> Res.string.format_codabar_example
    }
