package fr.skyle.scanny.platform.contact

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.provider.ContactsContract
import fr.skyle.scanny.domain.model.BarcodeContent

internal class AndroidContactAdder(
    private val context: Context,
) : ContactAdder {

    override fun addContact(contact: BarcodeContent.Contact) {
        // The contacts app lets the user create a contact, or complete an existing one
        val intent = Intent(Intent.ACTION_INSERT_OR_EDIT).apply {
            type = ContactsContract.Contacts.CONTENT_ITEM_TYPE
            contact.displayName?.let { name ->
                putExtra(ContactsContract.Intents.Insert.NAME, name)
            }
            contact.organization?.let { organization ->
                putExtra(ContactsContract.Intents.Insert.COMPANY, organization)
            }
            contact.title?.let { title ->
                putExtra(ContactsContract.Intents.Insert.JOB_TITLE, title)
            }
            putParcelableArrayListExtra(ContactsContract.Intents.Insert.DATA, contact.toDataRows())
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        context.startActivity(intent)
    }
}

// Rows of data rather than the PHONE or EMAIL extras, which stop at three of each
private fun BarcodeContent.Contact.toDataRows(): ArrayList<ContentValues> =
    ArrayList(
        phones.map { phone ->
            dataRow(
                mimeType = ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE,
                valueColumn = ContactsContract.CommonDataKinds.Phone.NUMBER,
                value = phone,
                type = ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE,
            )
        } + emails.map { email ->
            dataRow(
                mimeType = ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE,
                valueColumn = ContactsContract.CommonDataKinds.Email.ADDRESS,
                value = email,
                type = ContactsContract.CommonDataKinds.Email.TYPE_OTHER,
            )
        } + addresses.map { address ->
            dataRow(
                mimeType = ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE,
                valueColumn = ContactsContract.CommonDataKinds.StructuredPostal.FORMATTED_ADDRESS,
                value = address,
                type = ContactsContract.CommonDataKinds.StructuredPostal.TYPE_OTHER,
            )
        } + urls.map { url ->
            dataRow(
                mimeType = ContactsContract.CommonDataKinds.Website.CONTENT_ITEM_TYPE,
                valueColumn = ContactsContract.CommonDataKinds.Website.URL,
                value = url,
                type = ContactsContract.CommonDataKinds.Website.TYPE_OTHER,
            )
        },
    )

private fun dataRow(
    mimeType: String,
    valueColumn: String,
    value: String,
    type: Int,
): ContentValues =
    ContentValues().apply {
        put(ContactsContract.Data.MIMETYPE, mimeType)
        put(valueColumn, value)
        // The column of the type has the same name for every kind of data
        put(ContactsContract.CommonDataKinds.Phone.TYPE, type)
    }