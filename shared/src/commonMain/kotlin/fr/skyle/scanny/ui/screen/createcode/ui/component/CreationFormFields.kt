package fr.skyle.scanny.ui.screen.createcode.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import fr.skyle.scanny.designsystem.core.dropdownfield.SCDropdownField
import fr.skyle.scanny.designsystem.core.textfield.SCTextFieldDefaults
import fr.skyle.scanny.designsystem.theme.SCTheme
import fr.skyle.scanny.domain.model.BarcodeFormat
import fr.skyle.scanny.domain.model.ContactFormat
import fr.skyle.scanny.domain.model.OtpAlgorithm
import fr.skyle.scanny.domain.model.OtpType
import fr.skyle.scanny.domain.model.WifiEncryption
import fr.skyle.scanny.platform.keyboard.coordinateKeyboardType
import fr.skyle.scanny.resources.Res
import fr.skyle.scanny.resources.account
import fr.skyle.scanny.resources.address
import fr.skyle.scanny.resources.algorithm
import fr.skyle.scanny.resources.altitude_in_meters
import fr.skyle.scanny.resources.amount_in_bitcoins
import fr.skyle.scanny.resources.barcode_value
import fr.skyle.scanny.resources.blind_carbon_copy
import fr.skyle.scanny.resources.carbon_copy
import fr.skyle.scanny.resources.counter
import fr.skyle.scanny.resources.digits
import fr.skyle.scanny.resources.email
import fr.skyle.scanny.resources.format
import fr.skyle.scanny.resources.issuer
import fr.skyle.scanny.resources.latitude
import fr.skyle.scanny.resources.link
import fr.skyle.scanny.resources.longitude
import fr.skyle.scanny.resources.message
import fr.skyle.scanny.resources.name
import fr.skyle.scanny.resources.network
import fr.skyle.scanny.resources.organization
import fr.skyle.scanny.resources.otp_type
import fr.skyle.scanny.resources.password
import fr.skyle.scanny.resources.period_in_seconds
import fr.skyle.scanny.resources.phone
import fr.skyle.scanny.resources.phone_number
import fr.skyle.scanny.resources.place
import fr.skyle.scanny.resources.recipient
import fr.skyle.scanny.resources.recipients
import fr.skyle.scanny.resources.secret_key
import fr.skyle.scanny.resources.security
import fr.skyle.scanny.resources.subject
import fr.skyle.scanny.resources.text
import fr.skyle.scanny.resources.title
import fr.skyle.scanny.resources.uncertainty_in_meters
import fr.skyle.scanny.resources.website
import fr.skyle.scanny.ui.core.code.mapper.toBarcodeType
import fr.skyle.scanny.ui.core.code.mapper.toText
import fr.skyle.scanny.ui.core.preview.AppPreviewWithBackground
import fr.skyle.scanny.ui.screen.createcode.ui.mapper.toText
import fr.skyle.scanny.ui.screen.createcode.ui.model.CreationForm
import fr.skyle.scanny.ui.screen.createcode.ui.model.CreationKind
import org.jetbrains.compose.resources.stringResource

// The lengths of a one-time password the key URI format allows
private val OTP_DIGITS = listOf(6, 8)

// The barcodes qrose draws, the formats a barcode can be created in
private val barcodeFormats = BarcodeFormat.entries.filter { format -> format.toBarcodeType() != null }

/** The fields of the kind of code chosen: a text, a link, the fields of an e-mail… */
@Composable
fun CreationFormFields(
    form: CreationForm,
    onFormChanged: (CreationForm) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(space = 16.dp),
    ) {
        when (form.kind) {
            CreationKind.TEXT -> CreationTextField(
                modifier = Modifier.fillMaxWidth(),
                value = form.text,
                onValueChange = { text -> onFormChanged(form.copy(text = text)) },
                label = stringResource(Res.string.text),
                isRequired = true,
                minLines = 4,
            )

            CreationKind.LINK -> CreationTextField(
                modifier = Modifier.fillMaxWidth(),
                value = form.url,
                onValueChange = { url -> onFormChanged(form.copy(url = url)) },
                label = stringResource(Res.string.link),
                isRequired = true,
                keyboardType = KeyboardType.Uri,
            )

            CreationKind.EMAIL -> {
                // Several addresses each, separated by commas
                CreationTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = form.emailTo,
                    onValueChange = { to -> onFormChanged(form.copy(emailTo = to)) },
                    label = stringResource(Res.string.recipients),
                    keyboardType = KeyboardType.Email,
                )
                CreationTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = form.emailCc,
                    onValueChange = { cc -> onFormChanged(form.copy(emailCc = cc)) },
                    label = stringResource(Res.string.carbon_copy),
                    keyboardType = KeyboardType.Email,
                )
                CreationTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = form.emailBcc,
                    onValueChange = { bcc -> onFormChanged(form.copy(emailBcc = bcc)) },
                    label = stringResource(Res.string.blind_carbon_copy),
                    keyboardType = KeyboardType.Email,
                )
                CreationTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = form.emailSubject,
                    onValueChange = { subject -> onFormChanged(form.copy(emailSubject = subject)) },
                    label = stringResource(Res.string.subject),
                )
                CreationTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = form.emailBody,
                    onValueChange = { body -> onFormChanged(form.copy(emailBody = body)) },
                    label = stringResource(Res.string.message),
                    minLines = 3,
                )
            }

            CreationKind.SMS -> {
                CreationTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = form.smsPhoneNumber,
                    onValueChange = { phoneNumber -> onFormChanged(form.copy(smsPhoneNumber = phoneNumber)) },
                    label = stringResource(Res.string.phone_number),
                    isRequired = true,
                    keyboardType = KeyboardType.Phone,
                )
                CreationTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = form.smsMessage,
                    onValueChange = { message -> onFormChanged(form.copy(smsMessage = message)) },
                    label = stringResource(Res.string.message),
                    minLines = 3,
                )
            }

            CreationKind.MMS -> {
                CreationTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = form.smsPhoneNumber,
                    onValueChange = { phoneNumber -> onFormChanged(form.copy(smsPhoneNumber = phoneNumber)) },
                    label = stringResource(Res.string.phone_number),
                    isRequired = true,
                    keyboardType = KeyboardType.Phone,
                )
                CreationTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = form.mmsSubject,
                    onValueChange = { subject -> onFormChanged(form.copy(mmsSubject = subject)) },
                    label = stringResource(Res.string.subject),
                )
                CreationTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = form.smsMessage,
                    onValueChange = { message -> onFormChanged(form.copy(smsMessage = message)) },
                    label = stringResource(Res.string.message),
                    minLines = 3,
                )
            }

            CreationKind.PHONE -> CreationTextField(
                modifier = Modifier.fillMaxWidth(),
                value = form.phoneNumber,
                onValueChange = { phoneNumber -> onFormChanged(form.copy(phoneNumber = phoneNumber)) },
                label = stringResource(Res.string.phone_number),
                isRequired = true,
                keyboardType = KeyboardType.Phone,
            )

            CreationKind.WIFI -> {
                CreationTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = form.wifiSsid,
                    onValueChange = { ssid -> onFormChanged(form.copy(wifiSsid = ssid)) },
                    label = stringResource(Res.string.network),
                    isRequired = true,
                )
                SCDropdownField(
                    modifier = Modifier.fillMaxWidth(),
                    label = stringResource(Res.string.security),
                    placeholder = stringResource(Res.string.security),
                    options = WifiEncryption.entries,
                    selectedOption = form.wifiEncryption,
                    optionText = { encryption -> stringResource(encryption.toText()) },
                    onOptionSelected = { encryption -> onFormChanged(form.copy(wifiEncryption = encryption)) },
                    style = SCTextFieldDefaults.Style.LIGHT,
                )
                if (form.wifiEncryption != WifiEncryption.NONE) {
                    CreationTextField(
                    modifier = Modifier.fillMaxWidth(),
                        value = form.wifiPassword,
                        onValueChange = { password -> onFormChanged(form.copy(wifiPassword = password)) },
                        label = stringResource(Res.string.password),
                        isRequired = true,
                        keyboardType = KeyboardType.Password,
                    )
                }
                HiddenNetworkRow(
                    modifier = Modifier.fillMaxWidth(),
                    isHidden = form.isWifiHidden,
                    onHiddenChange = { isHidden -> onFormChanged(form.copy(isWifiHidden = isHidden)) },
                )
            }

            CreationKind.CONTACT -> {
                CreationTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = form.contactName,
                    onValueChange = { name -> onFormChanged(form.copy(contactName = name)) },
                    label = stringResource(Res.string.name),
                    isRequired = true,
                )
                CreationTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = form.contactPhone,
                    onValueChange = { phone -> onFormChanged(form.copy(contactPhone = phone)) },
                    label = stringResource(Res.string.phone),
                    keyboardType = KeyboardType.Phone,
                )
                CreationTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = form.contactEmail,
                    onValueChange = { email -> onFormChanged(form.copy(contactEmail = email)) },
                    label = stringResource(Res.string.email),
                    keyboardType = KeyboardType.Email,
                )
                CreationTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = form.contactOrganization,
                    onValueChange = { organization -> onFormChanged(form.copy(contactOrganization = organization)) },
                    label = stringResource(Res.string.organization),
                )
                CreationTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = form.contactAddress,
                    onValueChange = { address -> onFormChanged(form.copy(contactAddress = address)) },
                    label = stringResource(Res.string.address),
                    minLines = 2,
                )
                CreationTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = form.contactWebsite,
                    onValueChange = { website -> onFormChanged(form.copy(contactWebsite = website)) },
                    label = stringResource(Res.string.website),
                    keyboardType = KeyboardType.Uri,
                )
                // A MeCard is shorter, and so a smaller code: its size shows under the fields
                SegmentedChoice(
                    modifier = Modifier.fillMaxWidth(),
                    label = stringResource(Res.string.format),
                    options = ContactFormat.entries,
                    selectedOption = form.contactFormat,
                    optionText = { format -> stringResource(format.toText()) },
                    onOptionSelected = { format -> onFormChanged(form.copy(contactFormat = format)) },
                )
            }

            CreationKind.LOCATION -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(space = 16.dp),
                ) {
                    CreationTextField(
                        modifier = Modifier.weight(1f),
                        value = form.locationLatitude,
                        onValueChange = { latitude -> onFormChanged(form.copy(locationLatitude = latitude)) },
                        label = stringResource(Res.string.latitude),
                        isRequired = true,
                        keyboardType = coordinateKeyboardType,
                    )
                    CreationTextField(
                        modifier = Modifier.weight(1f),
                        value = form.locationLongitude,
                        onValueChange = { longitude -> onFormChanged(form.copy(locationLongitude = longitude)) },
                        label = stringResource(Res.string.longitude),
                        isRequired = true,
                        keyboardType = coordinateKeyboardType,
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(space = 16.dp),
                ) {
                    CreationTextField(
                        modifier = Modifier.weight(1f),
                        value = form.locationAltitude,
                        onValueChange = { altitude -> onFormChanged(form.copy(locationAltitude = altitude)) },
                        label = stringResource(Res.string.altitude_in_meters),
                        keyboardType = coordinateKeyboardType,
                    )
                    CreationTextField(
                        modifier = Modifier.weight(1f),
                        value = form.locationUncertainty,
                        onValueChange = { uncertainty -> onFormChanged(form.copy(locationUncertainty = uncertainty)) },
                        label = stringResource(Res.string.uncertainty_in_meters),
                        keyboardType = KeyboardType.Decimal,
                    )
                }
                CreationTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = form.locationLabel,
                    onValueChange = { label -> onFormChanged(form.copy(locationLabel = label)) },
                    label = stringResource(Res.string.place),
                )
            }

            CreationKind.BOOKMARK -> {
                CreationTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = form.bookmarkTitle,
                    onValueChange = { title -> onFormChanged(form.copy(bookmarkTitle = title)) },
                    label = stringResource(Res.string.title),
                )
                CreationTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = form.bookmarkUrl,
                    onValueChange = { url -> onFormChanged(form.copy(bookmarkUrl = url)) },
                    label = stringResource(Res.string.link),
                    isRequired = true,
                    keyboardType = KeyboardType.Uri,
                )
            }

            CreationKind.OTP -> {
                SegmentedChoice(
                    modifier = Modifier.fillMaxWidth(),
                    label = stringResource(Res.string.otp_type),
                    options = OtpType.entries,
                    selectedOption = form.otpType,
                    optionText = { type -> type.name },
                    onOptionSelected = { type -> onFormChanged(form.copy(otpType = type)) },
                )
                CreationTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = form.otpIssuer,
                    onValueChange = { issuer -> onFormChanged(form.copy(otpIssuer = issuer)) },
                    label = stringResource(Res.string.issuer),
                )
                CreationTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = form.otpAccount,
                    onValueChange = { account -> onFormChanged(form.copy(otpAccount = account)) },
                    label = stringResource(Res.string.account),
                    isRequired = true,
                    keyboardType = KeyboardType.Email,
                )
                // Typed as a password: the keyboard does not correct it
                CreationTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = form.otpSecret,
                    onValueChange = { secret -> onFormChanged(form.copy(otpSecret = secret)) },
                    label = stringResource(Res.string.secret_key),
                    isRequired = true,
                    keyboardType = KeyboardType.Password,
                )
                SegmentedChoice(
                    modifier = Modifier.fillMaxWidth(),
                    label = stringResource(Res.string.algorithm),
                    options = OtpAlgorithm.entries,
                    selectedOption = form.otpAlgorithm,
                    optionText = { algorithm -> algorithm.name },
                    onOptionSelected = { algorithm -> onFormChanged(form.copy(otpAlgorithm = algorithm)) },
                )
                SegmentedChoice(
                    modifier = Modifier.fillMaxWidth(),
                    label = stringResource(Res.string.digits),
                    // The two lengths of the format, and the one of a scanned key if it has another
                    options = (OTP_DIGITS + form.otpDigits).distinct().sorted(),
                    selectedOption = form.otpDigits,
                    optionText = { digits -> digits.toString() },
                    onOptionSelected = { digits -> onFormChanged(form.copy(otpDigits = digits)) },
                )
                when (form.otpType) {
                    OtpType.TOTP -> CreationTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = form.otpPeriod,
                        onValueChange = { period -> onFormChanged(form.copy(otpPeriod = period)) },
                        label = stringResource(Res.string.period_in_seconds),
                        isRequired = true,
                        keyboardType = KeyboardType.Number,
                    )

                    OtpType.HOTP -> CreationTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = form.otpCounter,
                        onValueChange = { counter -> onFormChanged(form.copy(otpCounter = counter)) },
                        label = stringResource(Res.string.counter),
                        isRequired = true,
                        keyboardType = KeyboardType.Number,
                    )
                }
            }

            CreationKind.BITCOIN -> {
                // Typed as a link: the keyboard neither capitalizes nor corrects it
                CreationTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = form.bitcoinAddress,
                    onValueChange = { address -> onFormChanged(form.copy(bitcoinAddress = address)) },
                    label = stringResource(Res.string.address),
                    isRequired = true,
                    keyboardType = KeyboardType.Uri,
                )
                CreationTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = form.bitcoinAmount,
                    onValueChange = { amount -> onFormChanged(form.copy(bitcoinAmount = amount)) },
                    label = stringResource(Res.string.amount_in_bitcoins),
                    keyboardType = KeyboardType.Decimal,
                )
                CreationTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = form.bitcoinLabel,
                    onValueChange = { label -> onFormChanged(form.copy(bitcoinLabel = label)) },
                    label = stringResource(Res.string.recipient),
                )
                CreationTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = form.bitcoinMessage,
                    onValueChange = { message -> onFormChanged(form.copy(bitcoinMessage = message)) },
                    label = stringResource(Res.string.message),
                )
            }

            CreationKind.BARCODE -> {
                SCDropdownField(
                    modifier = Modifier.fillMaxWidth(),
                    label = stringResource(Res.string.format),
                    placeholder = stringResource(Res.string.format),
                    options = barcodeFormats,
                    selectedOption = form.barcodeFormat,
                    optionText = { format -> stringResource(format.toText()) },
                    onOptionSelected = { format -> onFormChanged(form.copy(barcodeFormat = format)) },
                    style = SCTextFieldDefaults.Style.LIGHT,
                )
                CreationTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = form.barcodeValue,
                    onValueChange = { value -> onFormChanged(form.copy(barcodeValue = value)) },
                    label = stringResource(Res.string.barcode_value),
                    isRequired = true,
                )
            }
        }
    }
}

private class CreationKindProvider : PreviewParameterProvider<CreationKind> {
    override val values = CreationKind.entries.asSequence()
}

@AppPreviewWithBackground
@Composable
private fun PreviewCreationFormFields(
    @PreviewParameter(CreationKindProvider::class) kind: CreationKind,
) {
    SCTheme {
        CreationFormFields(
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = 16.dp),
            form = CreationForm(kind = kind),
            onFormChanged = {},
        )
    }
}