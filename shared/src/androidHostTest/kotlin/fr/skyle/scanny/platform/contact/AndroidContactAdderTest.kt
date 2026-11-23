package fr.skyle.scanny.platform.contact

import android.content.ContentValues
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
import fr.skyle.scanny.domain.model.ContactAddress
import fr.skyle.scanny.domain.model.ContactEmail
import fr.skyle.scanny.domain.model.ContactEntryType
import fr.skyle.scanny.domain.model.ContactPhone
import fr.skyle.scanny.domain.model.ContactPhoneType
import fr.skyle.scanny.domain.model.ContactWebsite
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import kotlin.test.Test
import kotlin.test.assertEquals

@RunWith(RobolectricTestRunner::class)
class AndroidContactAdderTest {

    @Test
    fun `every field of a contact goes in its row of data with its kind`() {
        // When
        val rows = CONTACT.toDataRows()

        // Then
        assertEquals(
            expected = listOf(
                listOf("Dr. John Quincy Doe Jr.", "Dr.", "John", "Quincy", "Doe", "Jr."),
                listOf("Johnny"),
                listOf("Scanny", "Mobile", "Lead"),
                listOf("1990-05-12", Event.TYPE_BIRTHDAY.toString()),
                listOf("Met at the fair"),
                listOf("+33600000000", Phone.TYPE_MOBILE.toString()),
                listOf("+33100000000", Phone.TYPE_OTHER_FAX.toString()),
                listOf("john@scanny.app", Email.TYPE_WORK.toString()),
                listOf(
                    "12 rue de la Paix\nApt 4\nBP 1\n75002 Paris\nIDF\nFrance",
                    "BP 1",
                    "12 rue de la Paix\nApt 4",
                    "Paris",
                    "IDF",
                    "75002",
                    "France",
                    StructuredPostal.TYPE_HOME.toString(),
                ),
                listOf("https://scanny.app", Website.TYPE_OTHER.toString()),
            ),
            actual = rows.map { row -> row.values() },
        )
    }

    @Test
    fun `the contacts app opens with the rows of the contact`() {
        // Given
        val application = RuntimeEnvironment.getApplication()

        // When
        AndroidContactAdder(context = application).addContact(contact = CONTACT)

        // Then
        val intent = shadowOf(application).nextStartedActivity
        assertEquals(
            expected = listOf(Intent.ACTION_INSERT_OR_EDIT, ContactsContract.Contacts.CONTENT_ITEM_TYPE, 10),
            actual = listOf(
                intent.action,
                intent.type,
                intent.getParcelableArrayListExtra(ContactsContract.Intents.Insert.DATA, ContentValues::class.java)?.size,
            ),
        )
    }

    // The values of a row, in the order of its columns
    private fun ContentValues.values(): List<String?> =
        when (getAsString(ContactsContract.Data.MIMETYPE)) {
            StructuredName.CONTENT_ITEM_TYPE -> listOf(
                StructuredName.DISPLAY_NAME,
                StructuredName.PREFIX,
                StructuredName.GIVEN_NAME,
                StructuredName.MIDDLE_NAME,
                StructuredName.FAMILY_NAME,
                StructuredName.SUFFIX,
            )
            Nickname.CONTENT_ITEM_TYPE -> listOf(Nickname.NAME)
            Organization.CONTENT_ITEM_TYPE -> listOf(Organization.COMPANY, Organization.DEPARTMENT, Organization.TITLE)
            Event.CONTENT_ITEM_TYPE -> listOf(Event.START_DATE, Event.TYPE)
            Note.CONTENT_ITEM_TYPE -> listOf(Note.NOTE)
            Phone.CONTENT_ITEM_TYPE -> listOf(Phone.NUMBER, Phone.TYPE)
            Email.CONTENT_ITEM_TYPE -> listOf(Email.ADDRESS, Email.TYPE)
            StructuredPostal.CONTENT_ITEM_TYPE -> listOf(
                StructuredPostal.FORMATTED_ADDRESS,
                StructuredPostal.POBOX,
                StructuredPostal.STREET,
                StructuredPostal.CITY,
                StructuredPostal.REGION,
                StructuredPostal.POSTCODE,
                StructuredPostal.COUNTRY,
                StructuredPostal.TYPE,
            )
            Website.CONTENT_ITEM_TYPE -> listOf(Website.URL, Website.TYPE)
            else -> emptyList()
        }.map { column -> getAsString(column) }

    private companion object {
        val CONTACT = BarcodeContent.Contact(
            namePrefix = "Dr.",
            firstName = "John",
            middleName = "Quincy",
            lastName = "Doe",
            nameSuffix = "Jr.",
            nickname = "Johnny",
            organization = "Scanny",
            department = "Mobile",
            title = "Lead",
            phones = listOf(
                ContactPhone(number = "+33600000000", type = ContactPhoneType.MOBILE),
                ContactPhone(number = "+33100000000", type = ContactPhoneType.FAX),
            ),
            emails = listOf(ContactEmail(address = "john@scanny.app", type = ContactEntryType.WORK)),
            addresses = listOf(
                ContactAddress(
                    poBox = "BP 1",
                    extended = "Apt 4",
                    street = "12 rue de la Paix",
                    city = "Paris",
                    region = "IDF",
                    postalCode = "75002",
                    country = "France",
                    type = ContactEntryType.HOME,
                ),
            ),
            websites = listOf(ContactWebsite(url = "https://scanny.app")),
            birthday = "1990-05-12",
            note = "Met at the fair",
        )
    }
}
