package fr.skyle.scanny.ui.screen.createcode.ui.mapper

import fr.skyle.scanny.domain.model.BarcodeContent
import fr.skyle.scanny.domain.model.BarcodeFormat
import fr.skyle.scanny.domain.model.CodeStyle
import fr.skyle.scanny.domain.model.ContactAddress
import fr.skyle.scanny.domain.model.ContactEmail
import fr.skyle.scanny.domain.model.ContactEntryType
import fr.skyle.scanny.domain.model.ContactFormat
import fr.skyle.scanny.domain.model.ContactPhone
import fr.skyle.scanny.domain.model.ContactPhoneType
import fr.skyle.scanny.domain.model.ContactWebsite
import fr.skyle.scanny.domain.model.Creation
import fr.skyle.scanny.domain.model.EventClassification
import fr.skyle.scanny.domain.model.EventDateTime
import fr.skyle.scanny.domain.model.EventFrequency
import fr.skyle.scanny.domain.model.EventRecurrence
import fr.skyle.scanny.domain.model.EventStatus
import fr.skyle.scanny.domain.model.EventTransparency
import fr.skyle.scanny.domain.model.OtpAlgorithm
import fr.skyle.scanny.domain.model.OtpType
import fr.skyle.scanny.domain.model.ScannedCode
import fr.skyle.scanny.domain.model.WifiEncryption
import fr.skyle.scanny.domain.model.WifiTransitionMode
import fr.skyle.scanny.domain.payload.toRawValue
import fr.skyle.scanny.ui.screen.createcode.ui.model.ContactPhoneField
import fr.skyle.scanny.ui.screen.createcode.ui.model.CreationForm
import fr.skyle.scanny.ui.screen.createcode.ui.model.CreationKind
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Instant

class CodeFormMapperTest {

    @Test
    fun `a scanned qr code filled in the editor gives back its content`() {
        // Given
        val contents = listOf(
            BarcodeContent.Text(text = "Hello"),
            BarcodeContent.Url(url = "https://scanny.app"),
            BarcodeContent.Email(
                to = listOf("hello@scanny.app"),
                subject = "Hello",
                body = "From Scanny",
            ),
            BarcodeContent.Sms(
                phoneNumber = "+33600000000",
                message = "Hello",
            ),
            BarcodeContent.Wifi(
                ssid = "Scanny",
                encryption = WifiEncryption.WPA,
                password = "secret12",
                isHidden = true,
            ),
            BarcodeContent.Wifi(
                ssid = "Modern",
                encryption = WifiEncryption.WPA,
                password = "short",
                transitionDisable = setOf(WifiTransitionMode.WPA3_PERSONAL, WifiTransitionMode.SAE_PK),
                passwordIdentifier = "john",
                publicKey = "MDkwEwYH",
            ),
            BarcodeContent.Wifi(
                ssid = "Corp",
                encryption = WifiEncryption.WPA_ENTERPRISE,
                password = "secret",
                transitionDisable = setOf(WifiTransitionMode.WPA3_ENTERPRISE),
                eapMethod = "TTLS",
                identity = "john@corp",
                anonymousIdentity = "anon",
                phase2Method = "PAP",
            ),
            BarcodeContent.Wifi(
                ssid = "Cafe",
                encryption = WifiEncryption.NONE,
                transitionDisable = setOf(WifiTransitionMode.ENHANCED_OPEN),
            ),
            BarcodeContent.Contact(
                formattedName = "Dr. John Q. Doe",
                namePrefix = "Dr.",
                firstName = "John",
                middleName = "Q.",
                lastName = "Doe",
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
                        street = "1 rue de Scanny",
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
            ),
            // A name shown other than the one the parts make
            BarcodeContent.Contact(
                formattedName = "Johnny",
                firstName = "John",
                lastName = "Doe",
            ),
            BarcodeContent.Phone(phoneNumber = "+33 6 00 00 00 00"),
            BarcodeContent.Event(
                summary = "Scanny launch",
                start = EventDateTime(date = LocalDate(2026, 11, 22), time = LocalTime(15, 0), timeZone = "Europe/Paris"),
                end = EventDateTime(date = LocalDate(2026, 11, 22), time = LocalTime(16, 30), timeZone = "Europe/Paris"),
                location = "Opéra",
                latitude = 48.8719,
                longitude = 2.3316,
                description = "Come along",
                url = "https://scanny.app",
                organizer = "john@scanny.app",
                attendees = listOf("jane@scanny.app", "jim@scanny.app"),
                recurrence = EventRecurrence(frequency = EventFrequency.WEEKLY, interval = 2, count = 10, days = setOf(DayOfWeek.MONDAY)),
                reminderMinutes = 15,
                status = EventStatus.CONFIRMED,
                transparency = EventTransparency.TRANSPARENT,
                classification = EventClassification.PRIVATE,
                priority = 1,
                categories = listOf("Work", "Launch"),
            ),
            BarcodeContent.Event(
                summary = "Holidays",
                start = EventDateTime(date = LocalDate(2026, 12, 24)),
                end = EventDateTime(date = LocalDate(2026, 12, 26)),
                recurrence = EventRecurrence(frequency = EventFrequency.YEARLY, until = LocalDate(2030, 12, 24)),
            ),
            BarcodeContent.Event(
                summary = "Floating",
                start = EventDateTime(date = LocalDate(2026, 1, 5), time = LocalTime(9, 0)),
            ),
            BarcodeContent.Mms(
                phoneNumber = "+33600000000",
                subject = "Hello",
                message = "See you",
            ),
            BarcodeContent.Contact(
                firstName = "John",
                lastName = "Doe",
                phones = listOf(ContactPhone(number = "+33600000000")),
                websites = listOf(ContactWebsite(url = "https://scanny.app")),
                format = ContactFormat.MECARD,
            ),
            BarcodeContent.Location(
                latitude = 0.00001,
                longitude = -122.4194155,
                altitude = -12.5,
                uncertainty = 35.0,
                label = "Scanny",
            ),
            BarcodeContent.Email(
                to = listOf("a@scanny.app", "b@scanny.app"),
                cc = listOf("c@scanny.app"),
                bcc = listOf("d@scanny.app"),
                subject = "Hello",
            ),
            BarcodeContent.Bookmark(
                url = "https://scanny.app",
                title = "Scanny",
            ),
            BarcodeContent.Otp(
                secret = "JBSWY3DPEHPK3PXP",
                type = OtpType.HOTP,
                issuer = "Scanny",
                account = "john@scanny.app",
                algorithm = OtpAlgorithm.SHA512,
                digits = 8,
                counter = 7,
            ),
            BarcodeContent.Otp(
                secret = "JBSWY3DPEHPK3PXP",
                account = "john@scanny.app",
                period = 60,
            ),
            BarcodeContent.Bitcoin(
                address = "1BoatSLRHtKNngkdXEeobR76b53LETtpyT",
                amount = "0.0015",
                label = "Scanny",
                message = "Order 42",
            ),
        )

        // When
        val contentsBack = contents.map { content ->
            scannedCode(content = content).toCreationForm().toBarcodeContent()
        }

        // Then
        assertEquals(
            expected = contents,
            actual = contentsBack,
        )
    }

    @Test
    fun `a barcode the editor draws stays a barcode of its value`() {
        // When
        val form = scannedCode(
            content = BarcodeContent.Text(text = "4006381333931"),
            format = BarcodeFormat.EAN_13,
        ).toCreationForm()

        // Then
        assertEquals(
            expected = CreationForm(
                kind = CreationKind.BARCODE,
                barcodeFormat = BarcodeFormat.EAN_13,
                barcodeValue = "4006381333931",
            ),
            actual = form,
        )
    }

    @Test
    fun `a code the editor does not draw becomes a qr code of its content`() {
        // When
        val forms = listOf(BarcodeFormat.DATA_MATRIX, null).map { format ->
            scannedCode(
                content = BarcodeContent.Url(url = "https://scanny.app"),
                format = format,
            ).toCreationForm()
        }

        // Then
        assertEquals(
            expected = List(2) {
                CreationForm(
                    kind = CreationKind.LINK,
                    url = "https://scanny.app",
                )
            },
            actual = forms,
        )
    }

    @Test
    fun `a contact keeps every one of its entries and an empty row for those it has none of`() {
        // When
        val form = scannedCode(
            content = BarcodeContent.Contact(
                formattedName = "John Doe",
                firstName = "John",
                lastName = "Doe",
                phones = listOf(
                    ContactPhone(number = "+33600000000"),
                    ContactPhone(number = "+33700000000", type = ContactPhoneType.WORK),
                ),
            ),
        ).toCreationForm()

        // Then
        assertEquals(
            expected = CreationForm(
                kind = CreationKind.CONTACT,
                contactFirstName = "John",
                contactLastName = "Doe",
                contactPhones = listOf(
                    ContactPhoneField(number = "+33600000000", type = ContactPhoneType.OTHER),
                    ContactPhoneField(number = "+33700000000", type = ContactPhoneType.WORK),
                ),
            ),
            actual = form,
        )
    }

    @Test
    fun `a mecard named in one piece shows it as its last name`() {
        // When
        val form = scannedCode(
            content = BarcodeContent.Contact(
                formattedName = "John Doe",
                format = ContactFormat.MECARD,
            ),
        ).toCreationForm()

        // Then
        assertEquals(
            expected = CreationForm(
                kind = CreationKind.CONTACT,
                contactLastName = "John Doe",
                contactFormat = ContactFormat.MECARD,
            ),
            actual = form,
        )
    }

    @Test
    fun `a created code fills the form as a scanned one would`() {
        // Given
        val content = BarcodeContent.Url(url = "https://scanny.app")

        // When
        val form = Creation(
            id = 1,
            content = content,
            rawValue = content.toRawValue(),
            format = BarcodeFormat.QR_CODE,
            style = CodeStyle(darkColor = 0xFF3B43BE),
            createdAt = Instant.fromEpochMilliseconds(0),
        ).toCreationForm()

        // Then
        assertEquals(
            expected = scannedCode(content = content).toCreationForm(),
            actual = form,
        )
    }

    private fun scannedCode(
        content: BarcodeContent,
        format: BarcodeFormat? = BarcodeFormat.QR_CODE,
    ): ScannedCode =
        ScannedCode(
            id = 1,
            scanId = 1,
            rawValue = content.toRawValue(),
            content = content,
            format = format,
            scannedAt = Instant.fromEpochMilliseconds(0),
        )
}
