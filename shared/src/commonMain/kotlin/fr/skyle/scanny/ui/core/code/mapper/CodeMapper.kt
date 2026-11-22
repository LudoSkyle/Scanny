package fr.skyle.scanny.ui.core.code.mapper

import fr.skyle.scanny.domain.model.BarcodeContent
import fr.skyle.scanny.domain.model.ContactAddress
import fr.skyle.scanny.domain.model.ContactEntryType
import fr.skyle.scanny.domain.model.ContactPhoneType
import fr.skyle.scanny.domain.model.Creation
import fr.skyle.scanny.domain.model.OtpType
import fr.skyle.scanny.domain.model.ScannedCode
import fr.skyle.scanny.domain.model.WifiEncryption
import fr.skyle.scanny.domain.payload.toBitcoinLink
import fr.skyle.scanny.domain.payload.toCoordinateText
import fr.skyle.scanny.domain.payload.toMailtoLink
import fr.skyle.scanny.domain.payload.toOtpauthLink
import fr.skyle.scanny.domain.payload.toSmsLink
import fr.skyle.scanny.domain.payload.toTelLink
import fr.skyle.scanny.platform.link.platformMapsLink
import fr.skyle.scanny.platform.link.platformMmsLink
import fr.skyle.scanny.platform.link.platformSmsLink
import fr.skyle.scanny.ui.core.code.model.CodeAction
import fr.skyle.scanny.ui.core.code.model.CodeField
import fr.skyle.scanny.ui.core.code.model.CodeFieldLabel
import fr.skyle.scanny.ui.core.code.model.CodeRowItem
import fr.skyle.scanny.ui.core.code.model.CodeType

// The symbol of the bitcoin, the same in every language
private const val BITCOIN_UNIT = "BTC"

// The symbols of the second and of the meter, the same in every language
private const val SECOND_UNIT = "s"
private const val METER_UNIT = "m"

internal fun BarcodeContent.toCodeType(): CodeType =
    when (this) {
        is BarcodeContent.Text -> CodeType.TEXT
        is BarcodeContent.Url -> CodeType.LINK
        is BarcodeContent.Email -> CodeType.EMAIL
        is BarcodeContent.Sms -> CodeType.SMS
        is BarcodeContent.Mms -> CodeType.MMS
        is BarcodeContent.Wifi -> CodeType.WIFI
        is BarcodeContent.Contact -> CodeType.CONTACT
        is BarcodeContent.Phone -> CodeType.PHONE
        is BarcodeContent.Location -> CodeType.LOCATION
        is BarcodeContent.Bookmark -> CodeType.BOOKMARK
        is BarcodeContent.Otp -> CodeType.OTP
        is BarcodeContent.Bitcoin -> CodeType.BITCOIN
    }

internal fun BarcodeContent.toCodeAction(canJoinWifi: (BarcodeContent.Wifi) -> Boolean): CodeAction? =
    when (this) {
        is BarcodeContent.Url -> CodeAction.OpenLink(link = url)
        is BarcodeContent.Email -> CodeAction.WriteEmail(link = toMailtoLink())
        is BarcodeContent.Sms -> CodeAction.WriteSms(link = platformSmsLink(smsLink = toSmsLink()))
        is BarcodeContent.Mms -> CodeAction.WriteMms(link = platformMmsLink(mms = this))
        is BarcodeContent.Contact -> CodeAction.AddContact(contact = this)
        is BarcodeContent.Wifi -> CodeAction.JoinWifi(wifi = this).takeIf { canJoinWifi(this) }
        is BarcodeContent.Phone -> CodeAction.Call(link = toTelLink())
        is BarcodeContent.Location -> CodeAction.ShowOnMap(link = platformMapsLink(location = this))
        is BarcodeContent.Bookmark -> CodeAction.OpenLink(link = url)
        is BarcodeContent.Otp -> CodeAction.AddToAuthenticator(link = toOtpauthLink())
        is BarcodeContent.Bitcoin -> CodeAction.OpenWallet(link = toBitcoinLink())
        // Nothing to open for a text
        is BarcodeContent.Text -> null
    }

internal fun ScannedCode.toCodeRowItem(): CodeRowItem =
    CodeRowItem(
        codeId = id,
        type = content.toCodeType(),
        summary = toCodeSummary(),
    )

/** What a code holds, in one line: the recipient of an e-mail, the name of a network… */
internal fun ScannedCode.toCodeSummary(): String =
    content.toSummary(rawValue = rawValue)

internal fun Creation.toCodeSummary(): String =
    content.toSummary(rawValue = rawValue)

// The text of the code when its content has nothing better to show
private fun BarcodeContent.toSummary(rawValue: String): String =
    when (this) {
        is BarcodeContent.Text -> text
        is BarcodeContent.Url -> url
        is BarcodeContent.Email -> to.firstOrNull() ?: cc.firstOrNull() ?: bcc.firstOrNull() ?: subject ?: body
        is BarcodeContent.Sms -> phoneNumber ?: message
        is BarcodeContent.Mms -> phoneNumber ?: subject ?: message
        is BarcodeContent.Wifi -> ssid
        is BarcodeContent.Contact -> displayName ?: organization ?: phones.firstOrNull()?.number ?: emails.firstOrNull()?.address
        is BarcodeContent.Phone -> phoneNumber
        is BarcodeContent.Location -> label ?: coordinates()
        is BarcodeContent.Bookmark -> title ?: url
        // "Scanny: john@doe.fr", as the authenticator apps name a key
        is BarcodeContent.Otp -> listOfNotNull(issuer, account).joinToString(separator = ": ").ifEmpty { null }
        is BarcodeContent.Bitcoin -> label ?: address
    } ?: rawValue

/** Whether the raw text of a code says more than its content: a text or a link is its own raw text. */
internal fun BarcodeContent.hasDistinctRawValue(): Boolean =
    this !is BarcodeContent.Text && this !is BarcodeContent.Url

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

        is BarcodeContent.Email -> codeFields(
            label = CodeFieldLabel.RECIPIENT,
            values = to,
        ) + codeFields(
            label = CodeFieldLabel.CARBON_COPY,
            values = cc,
        ) + codeFields(
            label = CodeFieldLabel.BLIND_CARBON_COPY,
            values = bcc,
        ) + listOfNotNull(
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

        is BarcodeContent.Mms -> listOfNotNull(
            codeField(
                label = CodeFieldLabel.PHONE_NUMBER,
                value = phoneNumber,
            ),
            codeField(
                label = CodeFieldLabel.SUBJECT,
                value = subject,
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

        // Each telephone, address, e-mail and website under the name of what it is for
        is BarcodeContent.Contact -> listOfNotNull(
            codeField(
                label = CodeFieldLabel.NAME,
                value = displayName,
            ),
            codeField(
                label = CodeFieldLabel.NICKNAME,
                value = nickname,
            ),
            codeField(
                label = CodeFieldLabel.ORGANIZATION,
                value = organization,
            ),
            codeField(
                label = CodeFieldLabel.DEPARTMENT,
                value = department,
            ),
            codeField(
                label = CodeFieldLabel.JOB_TITLE,
                value = title,
            ),
        ) + phones.map { phone ->
            CodeField(
                label = phone.type.toCodeFieldLabel(),
                value = phone.number,
            )
        } + emails.map { email ->
            CodeField(
                label = email.type.toCodeFieldLabel(
                    home = CodeFieldLabel.HOME_EMAIL,
                    work = CodeFieldLabel.WORK_EMAIL,
                    other = CodeFieldLabel.EMAIL,
                ),
                value = email.address,
            )
        } + addresses.map { address ->
            CodeField(
                label = address.type.toCodeFieldLabel(
                    home = CodeFieldLabel.HOME_ADDRESS,
                    work = CodeFieldLabel.WORK_ADDRESS,
                    other = CodeFieldLabel.ADDRESS,
                ),
                value = address.toText(),
            )
        } + websites.map { website ->
            CodeField(
                label = website.type.toCodeFieldLabel(
                    home = CodeFieldLabel.HOME_WEBSITE,
                    work = CodeFieldLabel.WORK_WEBSITE,
                    other = CodeFieldLabel.WEBSITE,
                ),
                value = website.url,
            )
        } + listOfNotNull(
            codeField(
                label = CodeFieldLabel.BIRTHDAY,
                value = birthday,
            ),
            codeField(
                label = CodeFieldLabel.NOTE,
                value = note,
            ),
        )

        is BarcodeContent.Phone -> listOf(
            CodeField(
                label = CodeFieldLabel.PHONE_NUMBER,
                value = phoneNumber,
            ),
        )

        is BarcodeContent.Location -> listOfNotNull(
            codeField(
                label = CodeFieldLabel.PLACE,
                value = label,
            ),
            CodeField(
                label = CodeFieldLabel.COORDINATES,
                value = coordinates(),
            ),
            codeField(
                label = CodeFieldLabel.ALTITUDE,
                value = altitude?.let { altitude -> "${altitude.toCoordinateText()} $METER_UNIT" },
            ),
            codeField(
                label = CodeFieldLabel.UNCERTAINTY,
                value = uncertainty?.let { uncertainty -> "± ${uncertainty.toCoordinateText()} $METER_UNIT" },
            ),
        )

        is BarcodeContent.Bookmark -> listOfNotNull(
            codeField(
                label = CodeFieldLabel.TITLE,
                value = title,
            ),
            CodeField(
                label = CodeFieldLabel.LINK,
                value = url,
            ),
        )

        // A parameter the key leaves to its default shows its default: the code is computed with it
        is BarcodeContent.Otp -> listOfNotNull(
            codeField(
                label = CodeFieldLabel.ISSUER,
                value = issuer,
            ),
            codeField(
                label = CodeFieldLabel.ACCOUNT,
                value = account,
            ),
            CodeField(
                label = CodeFieldLabel.SECRET_KEY,
                value = secret,
            ),
            CodeField(
                label = CodeFieldLabel.OTP_TYPE,
                value = type.name,
            ),
            CodeField(
                label = CodeFieldLabel.ALGORITHM,
                value = (algorithm ?: BarcodeContent.Otp.DEFAULT_ALGORITHM).name,
            ),
            CodeField(
                label = CodeFieldLabel.DIGITS,
                value = (digits ?: BarcodeContent.Otp.DEFAULT_DIGITS).toString(),
            ),
            when (type) {
                OtpType.TOTP -> CodeField(
                    label = CodeFieldLabel.PERIOD,
                    value = "${period ?: BarcodeContent.Otp.DEFAULT_PERIOD} $SECOND_UNIT",
                )

                OtpType.HOTP -> codeField(
                    label = CodeFieldLabel.COUNTER,
                    value = counter?.toString(),
                )
            },
        )

        is BarcodeContent.Bitcoin -> listOfNotNull(
            CodeField(
                label = CodeFieldLabel.ADDRESS,
                value = address,
            ),
            codeField(
                label = CodeFieldLabel.AMOUNT,
                value = amount?.let { amount -> "$amount $BITCOIN_UNIT" },
            ),
            codeField(
                label = CodeFieldLabel.RECIPIENT,
                value = label,
            ),
            codeField(
                label = CodeFieldLabel.MESSAGE,
                value = message,
            ),
        )
    }

private fun ContactPhoneType.toCodeFieldLabel(): CodeFieldLabel =
    when (this) {
        ContactPhoneType.MOBILE -> CodeFieldLabel.MOBILE
        ContactPhoneType.HOME -> CodeFieldLabel.HOME_PHONE
        ContactPhoneType.WORK -> CodeFieldLabel.WORK_PHONE
        ContactPhoneType.FAX -> CodeFieldLabel.FAX
        ContactPhoneType.OTHER -> CodeFieldLabel.PHONE
    }

private fun ContactEntryType.toCodeFieldLabel(
    home: CodeFieldLabel,
    work: CodeFieldLabel,
    other: CodeFieldLabel,
): CodeFieldLabel =
    when (this) {
        ContactEntryType.HOME -> home
        ContactEntryType.WORK -> work
        ContactEntryType.OTHER -> other
    }

// As written on an envelope: the street and what completes it, then the city after its postal code
private fun ContactAddress.toText(): String =
    listOfNotNull(
        street,
        extended,
        poBox,
        listOfNotNull(postalCode, city).joinToString(separator = " ").ifEmpty { null },
        region,
        country,
    ).joinToString(separator = "\n")

// "48.8584, 2.2945", as the maps apps take them in their search
private fun BarcodeContent.Location.coordinates(): String =
    "${latitude.toCoordinateText()}, ${longitude.toCoordinateText()}"

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