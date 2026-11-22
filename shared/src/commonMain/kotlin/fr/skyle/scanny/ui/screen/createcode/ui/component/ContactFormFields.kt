package fr.skyle.scanny.ui.screen.createcode.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import fr.skyle.scanny.designsystem.core.button.SCButton
import fr.skyle.scanny.designsystem.core.button.SCButtonDefaults
import fr.skyle.scanny.designsystem.core.datefield.SCDateField
import fr.skyle.scanny.designsystem.core.dropdownfield.SCDropdownField
import fr.skyle.scanny.designsystem.core.iconbutton.SCIconButton
import fr.skyle.scanny.designsystem.core.iconbutton.SCIconButtonDefaults
import fr.skyle.scanny.designsystem.core.textfield.SCTextFieldDefaults
import fr.skyle.scanny.designsystem.theme.SCTheme
import fr.skyle.scanny.domain.model.ContactEntryType
import fr.skyle.scanny.domain.model.ContactFormat
import fr.skyle.scanny.domain.model.ContactPhoneType
import fr.skyle.scanny.resources.Res
import fr.skyle.scanny.resources.add_address
import fr.skyle.scanny.resources.add_email
import fr.skyle.scanny.resources.add_phone
import fr.skyle.scanny.resources.add_website
import fr.skyle.scanny.resources.birthday
import fr.skyle.scanny.resources.city
import fr.skyle.scanny.resources.content_type
import fr.skyle.scanny.resources.country
import fr.skyle.scanny.resources.department
import fr.skyle.scanny.resources.display_name
import fr.skyle.scanny.resources.email
import fr.skyle.scanny.resources.extended_address
import fr.skyle.scanny.resources.first_name
import fr.skyle.scanny.resources.format
import fr.skyle.scanny.resources.ic_delete
import fr.skyle.scanny.resources.job_title
import fr.skyle.scanny.resources.last_name
import fr.skyle.scanny.resources.middle_name
import fr.skyle.scanny.resources.name_prefix
import fr.skyle.scanny.resources.name_suffix
import fr.skyle.scanny.resources.nickname
import fr.skyle.scanny.resources.note
import fr.skyle.scanny.resources.organization
import fr.skyle.scanny.resources.phone
import fr.skyle.scanny.resources.po_box
import fr.skyle.scanny.resources.postal_code
import fr.skyle.scanny.resources.region
import fr.skyle.scanny.resources.remove
import fr.skyle.scanny.resources.street
import fr.skyle.scanny.resources.website
import fr.skyle.scanny.ui.core.date.component.calendarDayText
import fr.skyle.scanny.ui.core.date.model.CalendarDay
import fr.skyle.scanny.ui.core.preview.AppPreviewWithBackground
import fr.skyle.scanny.ui.screen.createcode.ui.mapper.toText
import fr.skyle.scanny.ui.screen.createcode.ui.model.ContactAddressField
import fr.skyle.scanny.ui.screen.createcode.ui.model.ContactEntryField
import fr.skyle.scanny.ui.screen.createcode.ui.model.ContactPhoneField
import fr.skyle.scanny.ui.screen.createcode.ui.model.CreationForm
import fr.skyle.scanny.ui.screen.createcode.ui.model.CreationKind
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Instant

// The width of the kind of an entry, beside its value
private val ENTRY_TYPE_WIDTH = 136.dp

/**
 * The fields of a contact, those of a vCard or of a MeCard: a MeCard holds neither the other parts of a
 * name, nor a department, a title or the kinds of the entries, which are hidden while it is chosen.
 */
@Composable
fun ContactFormFields(
    form: CreationForm,
    onFormChanged: (CreationForm) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isVcard = form.contactFormat == ContactFormat.VCARD

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(space = 16.dp),
    ) {
        // A MeCard is shorter, and so a smaller code: its size shows under the fields
        SegmentedChoice(
            modifier = Modifier.fillMaxWidth(),
            label = stringResource(Res.string.format),
            options = ContactFormat.entries,
            selectedOption = form.contactFormat,
            optionText = { format -> stringResource(format.toText()) },
            onOptionSelected = { format -> onFormChanged(form.copy(contactFormat = format)) },
        )
        if (isVcard) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(space = 16.dp),
            ) {
                CreationTextField(
                    modifier = Modifier.weight(1f),
                    value = form.contactNamePrefix,
                    onValueChange = { prefix -> onFormChanged(form.copy(contactNamePrefix = prefix)) },
                    label = stringResource(Res.string.name_prefix),
                )
                CreationTextField(
                    modifier = Modifier.weight(1f),
                    value = form.contactNameSuffix,
                    onValueChange = { suffix -> onFormChanged(form.copy(contactNameSuffix = suffix)) },
                    label = stringResource(Res.string.name_suffix),
                )
            }
        }
        CreationTextField(
            modifier = Modifier.fillMaxWidth(),
            value = form.contactFirstName,
            onValueChange = { firstName -> onFormChanged(form.copy(contactFirstName = firstName)) },
            label = stringResource(Res.string.first_name),
        )
        if (isVcard) {
            CreationTextField(
                modifier = Modifier.fillMaxWidth(),
                value = form.contactMiddleName,
                onValueChange = { middleName -> onFormChanged(form.copy(contactMiddleName = middleName)) },
                label = stringResource(Res.string.middle_name),
            )
        }
        CreationTextField(
            modifier = Modifier.fillMaxWidth(),
            value = form.contactLastName,
            onValueChange = { lastName -> onFormChanged(form.copy(contactLastName = lastName)) },
            label = stringResource(Res.string.last_name),
        )
        if (isVcard) {
            // Made of the parts of the name when left empty
            CreationTextField(
                modifier = Modifier.fillMaxWidth(),
                value = form.contactDisplayName,
                onValueChange = { displayName -> onFormChanged(form.copy(contactDisplayName = displayName)) },
                label = stringResource(Res.string.display_name),
            )
        }
        CreationTextField(
            modifier = Modifier.fillMaxWidth(),
            value = form.contactNickname,
            onValueChange = { nickname -> onFormChanged(form.copy(contactNickname = nickname)) },
            label = stringResource(Res.string.nickname),
        )
        CreationTextField(
            modifier = Modifier.fillMaxWidth(),
            value = form.contactOrganization,
            onValueChange = { organization -> onFormChanged(form.copy(contactOrganization = organization)) },
            label = stringResource(Res.string.organization),
        )
        if (isVcard) {
            CreationTextField(
                modifier = Modifier.fillMaxWidth(),
                value = form.contactDepartment,
                onValueChange = { department -> onFormChanged(form.copy(contactDepartment = department)) },
                label = stringResource(Res.string.department),
            )
            CreationTextField(
                modifier = Modifier.fillMaxWidth(),
                value = form.contactTitle,
                onValueChange = { title -> onFormChanged(form.copy(contactTitle = title)) },
                label = stringResource(Res.string.job_title),
            )
        }

        form.contactPhones.forEachIndexed { index, phone ->
            EntryRow(
                value = phone.number,
                onValueChange = { number -> onFormChanged(form.copy(contactPhones = form.contactPhones.replaced(index, phone.copy(number = number)))) },
                label = stringResource(Res.string.phone),
                keyboardType = KeyboardType.Phone,
                types = ContactPhoneType.entries.takeIf { isVcard },
                selectedType = phone.type,
                typeText = { type -> stringResource(type.toText()) },
                onTypeSelected = { type -> onFormChanged(form.copy(contactPhones = form.contactPhones.replaced(index, phone.copy(type = type)))) },
                onRemoveClicked = { onFormChanged(form.copy(contactPhones = form.contactPhones.removed(index))) },
            )
        }
        AddButton(
            text = stringResource(Res.string.add_phone),
            onClick = { onFormChanged(form.copy(contactPhones = form.contactPhones + ContactPhoneField())) },
        )

        form.contactEmails.forEachIndexed { index, email ->
            EntryRow(
                value = email.value,
                onValueChange = { address -> onFormChanged(form.copy(contactEmails = form.contactEmails.replaced(index, email.copy(value = address)))) },
                label = stringResource(Res.string.email),
                keyboardType = KeyboardType.Email,
                types = ContactEntryType.entries.takeIf { isVcard },
                selectedType = email.type,
                typeText = { type -> stringResource(type.toText()) },
                onTypeSelected = { type -> onFormChanged(form.copy(contactEmails = form.contactEmails.replaced(index, email.copy(type = type)))) },
                onRemoveClicked = { onFormChanged(form.copy(contactEmails = form.contactEmails.removed(index))) },
            )
        }
        AddButton(
            text = stringResource(Res.string.add_email),
            onClick = { onFormChanged(form.copy(contactEmails = form.contactEmails + ContactEntryField())) },
        )

        form.contactAddresses.forEachIndexed { index, address ->
            AddressFields(
                address = address,
                isTypeShown = isVcard,
                onAddressChange = { changedAddress ->
                    onFormChanged(form.copy(contactAddresses = form.contactAddresses.replaced(index, changedAddress)))
                },
                onRemoveClicked = { onFormChanged(form.copy(contactAddresses = form.contactAddresses.removed(index))) },
            )
        }
        AddButton(
            text = stringResource(Res.string.add_address),
            onClick = { onFormChanged(form.copy(contactAddresses = form.contactAddresses + ContactAddressField())) },
        )

        form.contactWebsites.forEachIndexed { index, website ->
            EntryRow(
                value = website.value,
                onValueChange = { url -> onFormChanged(form.copy(contactWebsites = form.contactWebsites.replaced(index, website.copy(value = url)))) },
                label = stringResource(Res.string.website),
                keyboardType = KeyboardType.Uri,
                types = ContactEntryType.entries.takeIf { isVcard },
                selectedType = website.type,
                typeText = { type -> stringResource(type.toText()) },
                onTypeSelected = { type -> onFormChanged(form.copy(contactWebsites = form.contactWebsites.replaced(index, website.copy(type = type)))) },
                onRemoveClicked = { onFormChanged(form.copy(contactWebsites = form.contactWebsites.removed(index))) },
            )
        }
        AddButton(
            text = stringResource(Res.string.add_website),
            onClick = { onFormChanged(form.copy(contactWebsites = form.contactWebsites + ContactEntryField())) },
        )

        val birthday = form.contactBirthday.ifEmpty { null }?.let { birthday -> LocalDate.parse(birthday) }
        SCDateField(
            modifier = Modifier.fillMaxWidth(),
            label = stringResource(Res.string.birthday),
            dateText = birthday?.let { date -> calendarDayText(day = CalendarDay.Date(date = date)) }.orEmpty(),
            selectedDateMillis = birthday?.atStartOfDayIn(TimeZone.UTC)?.toEpochMilliseconds(),
            // The calendar gives the day at midnight UTC
            onDateSelected = { millis ->
                val date = millis?.let { Instant.fromEpochMilliseconds(millis).toLocalDateTime(TimeZone.UTC).date }
                onFormChanged(form.copy(contactBirthday = date?.toString().orEmpty()))
            },
            style = SCTextFieldDefaults.Style.LIGHT,
        )
        CreationTextField(
            modifier = Modifier.fillMaxWidth(),
            value = form.contactNote,
            onValueChange = { note -> onFormChanged(form.copy(contactNote = note)) },
            label = stringResource(Res.string.note),
            minLines = 3,
        )
    }
}

// A value, the kind of what it is for when the format holds it, and a button that takes the row out
@Composable
private fun <T> EntryRow(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    keyboardType: KeyboardType,
    types: List<T>?,
    selectedType: T,
    typeText: @Composable (T) -> String,
    onTypeSelected: (T) -> Unit,
    onRemoveClicked: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(space = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CreationTextField(
            modifier = Modifier.weight(1f),
            value = value,
            onValueChange = onValueChange,
            label = label,
            keyboardType = keyboardType,
        )
        if (types != null) {
            SCDropdownField(
                modifier = Modifier.width(ENTRY_TYPE_WIDTH),
                label = stringResource(Res.string.content_type),
                placeholder = stringResource(Res.string.content_type),
                options = types,
                selectedOption = selectedType,
                optionText = typeText,
                onOptionSelected = onTypeSelected,
                style = SCTextFieldDefaults.Style.LIGHT,
            )
        }
        RemoveButton(onClick = onRemoveClicked)
    }
}

@Composable
private fun AddressFields(
    address: ContactAddressField,
    isTypeShown: Boolean,
    onAddressChange: (ContactAddressField) -> Unit,
    onRemoveClicked: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(space = 8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(space = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CreationTextField(
                modifier = Modifier.weight(1f),
                value = address.street,
                onValueChange = { street -> onAddressChange(address.copy(street = street)) },
                label = stringResource(Res.string.street),
            )
            if (isTypeShown) {
                SCDropdownField(
                    modifier = Modifier.width(ENTRY_TYPE_WIDTH),
                    label = stringResource(Res.string.content_type),
                    placeholder = stringResource(Res.string.content_type),
                    options = ContactEntryType.entries,
                    selectedOption = address.type,
                    optionText = { type -> stringResource(type.toText()) },
                    onOptionSelected = { type -> onAddressChange(address.copy(type = type)) },
                    style = SCTextFieldDefaults.Style.LIGHT,
                )
            }
            RemoveButton(onClick = onRemoveClicked)
        }
        CreationTextField(
            modifier = Modifier.fillMaxWidth(),
            value = address.extended,
            onValueChange = { extended -> onAddressChange(address.copy(extended = extended)) },
            label = stringResource(Res.string.extended_address),
        )
        CreationTextField(
            modifier = Modifier.fillMaxWidth(),
            value = address.poBox,
            onValueChange = { poBox -> onAddressChange(address.copy(poBox = poBox)) },
            label = stringResource(Res.string.po_box),
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(space = 8.dp),
        ) {
            CreationTextField(
                modifier = Modifier.weight(1f),
                value = address.postalCode,
                onValueChange = { postalCode -> onAddressChange(address.copy(postalCode = postalCode)) },
                label = stringResource(Res.string.postal_code),
            )
            CreationTextField(
                modifier = Modifier.weight(2f),
                value = address.city,
                onValueChange = { city -> onAddressChange(address.copy(city = city)) },
                label = stringResource(Res.string.city),
            )
        }
        CreationTextField(
            modifier = Modifier.fillMaxWidth(),
            value = address.region,
            onValueChange = { region -> onAddressChange(address.copy(region = region)) },
            label = stringResource(Res.string.region),
        )
        CreationTextField(
            modifier = Modifier.fillMaxWidth(),
            value = address.country,
            onValueChange = { country -> onAddressChange(address.copy(country = country)) },
            label = stringResource(Res.string.country),
        )
    }
}

@Composable
private fun AddButton(
    text: String,
    onClick: () -> Unit,
) {
    SCButton(
        modifier = Modifier.fillMaxWidth(),
        text = text,
        onClick = onClick,
        style = SCButtonDefaults.Style.SECONDARY,
        size = SCButtonDefaults.Size.MEDIUM,
    )
}

@Composable
private fun RemoveButton(onClick: () -> Unit) {
    SCIconButton(
        icon = painterResource(Res.drawable.ic_delete),
        contentDescription = stringResource(Res.string.remove),
        onClick = onClick,
        style = SCIconButtonDefaults.Style.DARK,
        size = SCIconButtonDefaults.Size.MEDIUM,
    )
}

private fun <T> List<T>.replaced(
    index: Int,
    element: T,
): List<T> =
    toMutableList().apply { set(index, element) }

private fun <T> List<T>.removed(index: Int): List<T> =
    filterIndexed { elementIndex, _ -> elementIndex != index }

@AppPreviewWithBackground
@Composable
private fun PreviewContactFormFields() {
    SCTheme {
        ContactFormFields(
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = 16.dp),
            form = CreationForm(
                kind = CreationKind.CONTACT,
                contactFirstName = "John",
                contactLastName = "Doe",
                contactAddresses = listOf(ContactAddressField(city = "Paris")),
                contactBirthday = "1990-05-12",
            ),
            onFormChanged = {},
        )
    }
}
