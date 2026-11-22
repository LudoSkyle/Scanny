package fr.skyle.scanny.platform.contact

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.provider.ContactsContract
import android.provider.ContactsContract.CommonDataKinds.Email
import android.provider.ContactsContract.CommonDataKinds.Event
import android.provider.ContactsContract.CommonDataKinds.Nickname
import android.provider.ContactsContract.CommonDataKinds.Note
import android.provider.ContactsContract.CommonDataKinds.Organization
import android.provider.ContactsContract.CommonDataKinds.Phone
import android.provider.ContactsContract.CommonDataKinds.StructuredName
import android.provider.ContactsContract.CommonDataKinds.StructuredPostal
import android.provider.ContactsContract.CommonDataKinds.Website
import fr.skyle.scanny.domain.model.BarcodeContent
import fr.skyle.scanny.domain.model.ContactEntryType
import fr.skyle.scanny.domain.model.ContactPhoneType

internal class AndroidContactAdder(
    private val context: Context,
) : ContactAdder {

    override fun addContact(contact: BarcodeContent.Contact) {
        // The contacts app lets the user create a contact, or complete an existing one
        val intent = Intent(Intent.ACTION_INSERT_OR_EDIT).apply {
            type = ContactsContract.Contacts.CONTENT_ITEM_TYPE
            putParcelableArrayListExtra(ContactsContract.Intents.Insert.DATA, contact.toDataRows())
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        context.startActivity(intent)
    }
}

/**
 * The rows of data of the contact, one per field: rather than the extras of the intent, which hold one
 * name in a single piece and stop at three telephones and three e-mails.
 */
internal fun BarcodeContent.Contact.toDataRows(): ArrayList<ContentValues> =
    ArrayList(
        listOfNotNull(
            nameRow(),
            nickname?.let { nickname ->
                dataRow(mimeType = Nickname.CONTENT_ITEM_TYPE) {
                    put(Nickname.NAME, nickname)
                }
            },
            organizationRow(),
            birthday?.let { birthday ->
                dataRow(mimeType = Event.CONTENT_ITEM_TYPE) {
                    put(Event.START_DATE, birthday)
                    put(Event.TYPE, Event.TYPE_BIRTHDAY)
                }
            },
            note?.let { note ->
                dataRow(mimeType = Note.CONTENT_ITEM_TYPE) {
                    put(Note.NOTE, note)
                }
            },
        ) + phones.map { phone ->
            dataRow(mimeType = Phone.CONTENT_ITEM_TYPE) {
                put(Phone.NUMBER, phone.number)
                put(Phone.TYPE, phone.type.toPhoneType())
            }
        } + emails.map { email ->
            dataRow(mimeType = Email.CONTENT_ITEM_TYPE) {
                put(Email.ADDRESS, email.address)
                put(Email.TYPE, email.type.toType(home = Email.TYPE_HOME, work = Email.TYPE_WORK, other = Email.TYPE_OTHER))
            }
        } + addresses.map { address ->
            dataRow(mimeType = StructuredPostal.CONTENT_ITEM_TYPE) {
                // The contacts app of Google shows the formatted address alone
                put(
                    StructuredPostal.FORMATTED_ADDRESS,
                    listOfNotNull(
                        address.street,
                        address.extended,
                        address.poBox,
                        listOfNotNull(address.postalCode, address.city).joinToString(separator = " ").ifEmpty { null },
                        address.region,
                        address.country,
                    ).joinToString(separator = "\n"),
                )
                put(StructuredPostal.POBOX, address.poBox)
                // The extended address (an apartment, a building) comes with the street, as an envelope writes it
                put(StructuredPostal.STREET, listOfNotNull(address.street, address.extended).joinToString(separator = "\n").ifEmpty { null })
                put(StructuredPostal.CITY, address.city)
                put(StructuredPostal.REGION, address.region)
                put(StructuredPostal.POSTCODE, address.postalCode)
                put(StructuredPostal.COUNTRY, address.country)
                put(
                    StructuredPostal.TYPE,
                    address.type.toType(home = StructuredPostal.TYPE_HOME, work = StructuredPostal.TYPE_WORK, other = StructuredPostal.TYPE_OTHER),
                )
            }
        } + websites.map { website ->
            dataRow(mimeType = Website.CONTENT_ITEM_TYPE) {
                put(Website.URL, website.url)
                put(Website.TYPE, website.type.toType(home = Website.TYPE_HOME, work = Website.TYPE_WORK, other = Website.TYPE_OTHER))
            }
        },
    )

private fun BarcodeContent.Contact.nameRow(): ContentValues? =
    if (listOf(formattedName, namePrefix, firstName, middleName, lastName, nameSuffix).all { part -> part == null }) {
        null
    } else {
        dataRow(mimeType = StructuredName.CONTENT_ITEM_TYPE) {
            put(StructuredName.DISPLAY_NAME, displayName)
            put(StructuredName.PREFIX, namePrefix)
            put(StructuredName.GIVEN_NAME, firstName)
            put(StructuredName.MIDDLE_NAME, middleName)
            put(StructuredName.FAMILY_NAME, lastName)
            put(StructuredName.SUFFIX, nameSuffix)
        }
    }

private fun BarcodeContent.Contact.organizationRow(): ContentValues? =
    if (organization == null && department == null && title == null) {
        null
    } else {
        dataRow(mimeType = Organization.CONTENT_ITEM_TYPE) {
            put(Organization.COMPANY, organization)
            put(Organization.DEPARTMENT, department)
            put(Organization.TITLE, title)
        }
    }

private fun ContactPhoneType.toPhoneType(): Int =
    when (this) {
        ContactPhoneType.MOBILE -> Phone.TYPE_MOBILE
        ContactPhoneType.HOME -> Phone.TYPE_HOME
        ContactPhoneType.WORK -> Phone.TYPE_WORK
        ContactPhoneType.FAX -> Phone.TYPE_OTHER_FAX
        ContactPhoneType.OTHER -> Phone.TYPE_OTHER
    }

private fun ContactEntryType.toType(
    home: Int,
    work: Int,
    other: Int,
): Int =
    when (this) {
        ContactEntryType.HOME -> home
        ContactEntryType.WORK -> work
        ContactEntryType.OTHER -> other
    }

private fun dataRow(
    mimeType: String,
    values: ContentValues.() -> Unit,
): ContentValues =
    ContentValues().apply {
        put(ContactsContract.Data.MIMETYPE, mimeType)
        values()
    }
