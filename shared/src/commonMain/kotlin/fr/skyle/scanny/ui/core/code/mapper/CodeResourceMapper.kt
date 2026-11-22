package fr.skyle.scanny.ui.core.code.mapper

import fr.skyle.scanny.domain.model.BarcodeFormat
import fr.skyle.scanny.resources.Res
import fr.skyle.scanny.resources.add_contact
import fr.skyle.scanny.resources.address
import fr.skyle.scanny.resources.aztec
import fr.skyle.scanny.resources.codabar
import fr.skyle.scanny.resources.code_128
import fr.skyle.scanny.resources.code_39
import fr.skyle.scanny.resources.code_93
import fr.skyle.scanny.resources.contact
import fr.skyle.scanny.resources.data_matrix
import fr.skyle.scanny.resources.ean_13
import fr.skyle.scanny.resources.ean_8
import fr.skyle.scanny.resources.email
import fr.skyle.scanny.resources.format_aztec_info
import fr.skyle.scanny.resources.format_codabar_info
import fr.skyle.scanny.resources.format_code_128_info
import fr.skyle.scanny.resources.format_code_39_info
import fr.skyle.scanny.resources.format_code_93_info
import fr.skyle.scanny.resources.format_data_matrix_info
import fr.skyle.scanny.resources.format_ean_13_info
import fr.skyle.scanny.resources.format_ean_8_info
import fr.skyle.scanny.resources.format_itf_info
import fr.skyle.scanny.resources.format_pdf417_info
import fr.skyle.scanny.resources.format_qr_code_info
import fr.skyle.scanny.resources.format_upc_a_info
import fr.skyle.scanny.resources.format_upc_e_info
import fr.skyle.scanny.resources.ic_link
import fr.skyle.scanny.resources.ic_mail
import fr.skyle.scanny.resources.ic_notes
import fr.skyle.scanny.resources.ic_person
import fr.skyle.scanny.resources.ic_sms
import fr.skyle.scanny.resources.ic_wifi
import fr.skyle.scanny.resources.itf
import fr.skyle.scanny.resources.job_title
import fr.skyle.scanny.resources.join_network
import fr.skyle.scanny.resources.link
import fr.skyle.scanny.resources.message
import fr.skyle.scanny.resources.name
import fr.skyle.scanny.resources.network
import fr.skyle.scanny.resources.open_link
import fr.skyle.scanny.resources.organization
import fr.skyle.scanny.resources.password
import fr.skyle.scanny.resources.pdf417
import fr.skyle.scanny.resources.phone
import fr.skyle.scanny.resources.phone_number
import fr.skyle.scanny.resources.qr_code
import fr.skyle.scanny.resources.recipient
import fr.skyle.scanny.resources.security
import fr.skyle.scanny.resources.sms
import fr.skyle.scanny.resources.subject
import fr.skyle.scanny.resources.text
import fr.skyle.scanny.resources.type_contact_info
import fr.skyle.scanny.resources.type_email_info
import fr.skyle.scanny.resources.type_link_info
import fr.skyle.scanny.resources.type_sms_info
import fr.skyle.scanny.resources.type_text_info
import fr.skyle.scanny.resources.type_wifi_info
import fr.skyle.scanny.resources.upc_a
import fr.skyle.scanny.resources.upc_e
import fr.skyle.scanny.resources.website
import fr.skyle.scanny.resources.wifi
import fr.skyle.scanny.resources.write_email
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
        CodeType.WIFI -> Res.string.wifi
        CodeType.CONTACT -> Res.string.contact
    }

internal fun CodeType.toIcon(): DrawableResource =
    when (this) {
        CodeType.TEXT -> Res.drawable.ic_notes
        CodeType.LINK -> Res.drawable.ic_link
        CodeType.EMAIL -> Res.drawable.ic_mail
        CodeType.SMS -> Res.drawable.ic_sms
        CodeType.WIFI -> Res.drawable.ic_wifi
        CodeType.CONTACT -> Res.drawable.ic_person
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
    }

internal fun CodeAction.toText(): StringResource =
    when (this) {
        is CodeAction.OpenLink -> Res.string.open_link
        is CodeAction.WriteEmail -> Res.string.write_email
        is CodeAction.WriteSms -> Res.string.write_sms
        is CodeAction.AddContact -> Res.string.add_contact
        is CodeAction.JoinWifi -> Res.string.join_network
    }
/** What a code of this type holds, and what Scanny does with it. */
internal fun CodeType.toInfo(): StringResource =
    when (this) {
        CodeType.TEXT -> Res.string.type_text_info
        CodeType.LINK -> Res.string.type_link_info
        CodeType.EMAIL -> Res.string.type_email_info
        CodeType.SMS -> Res.string.type_sms_info
        CodeType.WIFI -> Res.string.type_wifi_info
        CodeType.CONTACT -> Res.string.type_contact_info
    }

/** What a code of this format looks like, what it holds, and where it is found. */
internal fun BarcodeFormat.toInfo(): StringResource =
    when (this) {
        BarcodeFormat.QR_CODE -> Res.string.format_qr_code_info
        BarcodeFormat.DATA_MATRIX -> Res.string.format_data_matrix_info
        BarcodeFormat.AZTEC -> Res.string.format_aztec_info
        BarcodeFormat.PDF417 -> Res.string.format_pdf417_info
        BarcodeFormat.EAN_13 -> Res.string.format_ean_13_info
        BarcodeFormat.EAN_8 -> Res.string.format_ean_8_info
        BarcodeFormat.UPC_A -> Res.string.format_upc_a_info
        BarcodeFormat.UPC_E -> Res.string.format_upc_e_info
        BarcodeFormat.CODE_39 -> Res.string.format_code_39_info
        BarcodeFormat.CODE_93 -> Res.string.format_code_93_info
        BarcodeFormat.CODE_128 -> Res.string.format_code_128_info
        BarcodeFormat.ITF -> Res.string.format_itf_info
        BarcodeFormat.CODABAR -> Res.string.format_codabar_info
    }
