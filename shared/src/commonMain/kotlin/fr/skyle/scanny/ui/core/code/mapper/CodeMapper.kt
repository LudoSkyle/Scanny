package fr.skyle.scanny.ui.core.code.mapper

import fr.skyle.scanny.domain.model.BarcodeContent
import fr.skyle.scanny.domain.model.ScannedCode
import fr.skyle.scanny.domain.model.WifiEncryption
import fr.skyle.scanny.domain.payload.toMailtoLink
import fr.skyle.scanny.domain.payload.toSmsLink
import fr.skyle.scanny.platform.link.platformSmsLink
import fr.skyle.scanny.ui.core.code.model.CodeAction
import fr.skyle.scanny.ui.core.code.model.CodeField
import fr.skyle.scanny.ui.core.code.model.CodeFieldLabel
import fr.skyle.scanny.ui.core.code.model.CodeType

internal fun BarcodeContent.toCodeType(): CodeType =
    when (this) {
        is BarcodeContent.Text -> CodeType.TEXT
        is BarcodeContent.Url -> CodeType.LINK
        is BarcodeContent.Email -> CodeType.EMAIL
        is BarcodeContent.Sms -> CodeType.SMS
        is BarcodeContent.Wifi -> CodeType.WIFI
        is BarcodeContent.Contact -> CodeType.CONTACT
    }

internal fun BarcodeContent.toCodeAction(canJoinWifi: (BarcodeContent.Wifi) -> Boolean): CodeAction? =
    when (this) {
        is BarcodeContent.Url -> CodeAction.OpenLink(link = url)
        is BarcodeContent.Email -> CodeAction.WriteEmail(link = toMailtoLink())
        is BarcodeContent.Sms -> CodeAction.WriteSms(link = platformSmsLink(smsLink = toSmsLink()))
        is BarcodeContent.Contact -> CodeAction.AddContact(contact = this)
        is BarcodeContent.Wifi -> CodeAction.JoinWifi(wifi = this).takeIf { canJoinWifi(this) }
        // Nothing to open for a text
        is BarcodeContent.Text -> null
    }

/** What a code holds, in one line: the recipient of an e-mail, the name of a network… */
internal fun ScannedCode.toCodeSummary(): String =
    when (val content = content) {
        is BarcodeContent.Text -> content.text
        is BarcodeContent.Url -> content.url
        is BarcodeContent.Email -> content.address ?: content.subject ?: content.body
        is BarcodeContent.Sms -> content.phoneNumber ?: content.message
        is BarcodeContent.Wifi -> content.ssid
        is BarcodeContent.Contact ->
            content.displayName ?: content.organization ?: content.phones.firstOrNull() ?: content.emails.firstOrNull()
    } ?: rawValue

internal fun BarcodeContent.toCodeFields(): List<CodeField> =
    when (this) {
        is BarcodeContent.Text -> listOf(
            CodeField(
                label = null,
                value = text,
            ),
        )

        is BarcodeContent.Url -> listOf(
            CodeField(
                label = null,
                value = url,
            ),
        )

        is BarcodeContent.Email -> listOfNotNull(
            codeField(
                label = CodeFieldLabel.RECIPIENT,
                value = address,
            ),
            codeField(
                label = CodeFieldLabel.SUBJECT,
                value = subject,
            ),
            codeField(
                label = CodeFieldLabel.MESSAGE,
                value = body,
            ),
        )

        is BarcodeContent.Sms -> listOfNotNull(
            codeField(
                label = CodeFieldLabel.PHONE_NUMBER,
                value = phoneNumber,
            ),
            codeField(
                label = CodeFieldLabel.MESSAGE,
                value = message,
            ),
        )

        // An open network has no security and no password to show
        is BarcodeContent.Wifi -> listOfNotNull(
            codeField(
                label = CodeFieldLabel.NETWORK,
                value = ssid,
            ),
            codeField(
                label = CodeFieldLabel.SECURITY,
                // The names of the encryptions are the ones the settings of the phones show
                value = encryption.name.takeIf { encryption != WifiEncryption.NONE },
            ),
            codeField(
                label = CodeFieldLabel.PASSWORD,
                value = password.takeIf { encryption != WifiEncryption.NONE },
            ),
        )

        is BarcodeContent.Contact -> listOfNotNull(
            codeField(
                label = CodeFieldLabel.NAME,
                value = displayName,
            ),
            codeField(
                label = CodeFieldLabel.ORGANIZATION,
                value = organization,
            ),
            codeField(
                label = CodeFieldLabel.JOB_TITLE,
                value = title,
            ),
        ) + codeFields(
            label = CodeFieldLabel.PHONE,
            values = phones,
        ) + codeFields(
            label = CodeFieldLabel.EMAIL,
            values = emails,
        ) + codeFields(
            label = CodeFieldLabel.ADDRESS,
            values = addresses,
        ) + codeFields(
            label = CodeFieldLabel.WEBSITE,
            values = urls,
        )
    }

private fun codeField(
    label: CodeFieldLabel,
    value: String?,
): CodeField? =
    value?.let {
        CodeField(
            label = label,
            value = value,
        )
    }

private fun codeFields(
    label: CodeFieldLabel,
    values: List<String>,
): List<CodeField> =
    values.map { value ->
        CodeField(
            label = label,
            value = value,
        )
    }