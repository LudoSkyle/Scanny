package fr.skyle.scanny.ui.screen.createcode.ui.mapper

import fr.skyle.scanny.domain.model.BarcodeContent
import fr.skyle.scanny.domain.model.BarcodeFormat
import fr.skyle.scanny.domain.model.CodeStyle
import fr.skyle.scanny.domain.model.ContactFormat
import fr.skyle.scanny.domain.model.Creation
import fr.skyle.scanny.domain.model.OtpType
import fr.skyle.scanny.domain.model.ScannedCode
import fr.skyle.scanny.domain.model.WifiEncryption
import fr.skyle.scanny.domain.payload.toRawValue
import fr.skyle.scanny.ui.screen.createcode.ui.model.CreationForm
import fr.skyle.scanny.ui.screen.createcode.ui.model.CreationKind
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
                address = "hello@scanny.app",
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
            BarcodeContent.Contact(
                formattedName = "John Doe",
                organization = "Scanny",
                phones = listOf("+33600000000"),
                emails = listOf("john@scanny.app"),
                addresses = listOf("1 rue de Scanny"),
                urls = listOf("https://scanny.app"),
            ),
            BarcodeContent.Phone(phoneNumber = "+33 6 00 00 00 00"),
            BarcodeContent.Mms(
                phoneNumber = "+33600000000",
                subject = "Hello",
                message = "See you",
            ),
            BarcodeContent.Contact(
                formattedName = "John Doe",
                phones = listOf("+33600000000"),
                format = ContactFormat.MECARD,
            ),
            BarcodeContent.Location(
                latitude = 0.00001,
                longitude = -122.4194155,
                label = "Scanny",
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
                algorithm = "SHA512",
                digits = 8,
                period = 60,
                counter = 7,
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
    fun `a contact keeps the first of its phones e-mails addresses and websites`() {
        // When
        val form = scannedCode(
            content = BarcodeContent.Contact(
                firstName = "John",
                lastName = "Doe",
                phones = listOf("+33600000000", "+33700000000"),
                emails = listOf("john@scanny.app", "doe@scanny.app"),
                addresses = listOf("1 rue de Scanny", "2 rue de Scanny"),
                urls = listOf("https://scanny.app", "https://example.org"),
            ),
        ).toCreationForm()

        // Then
        assertEquals(
            expected = CreationForm(
                kind = CreationKind.CONTACT,
                contactName = "John Doe",
                contactPhone = "+33600000000",
                contactEmail = "john@scanny.app",
                contactAddress = "1 rue de Scanny",
                contactWebsite = "https://scanny.app",
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
