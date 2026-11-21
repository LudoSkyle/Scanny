package fr.skyle.scanny.ui.screen.createcode.ui.mapper

import fr.skyle.scanny.domain.model.BarcodeContent
import fr.skyle.scanny.domain.model.BarcodeFormat
import fr.skyle.scanny.domain.model.WifiEncryption
import fr.skyle.scanny.ui.screen.createcode.ui.model.CreationForm
import fr.skyle.scanny.ui.screen.createcode.ui.model.CreationKind
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class CreationFormMapperTest {

    @Test
    fun `a blank text is no content yet`() {
        // When
        val content = CreationForm(text = "   ").toBarcodeContent()

        // Then
        assertNull(content)
    }

    @Test
    fun `a link typed without its scheme gets https and one with http keeps it`() {
        // When
        val contents = listOf(" scanny.app ", "http://scanny.app").map { url ->
            CreationForm(
                kind = CreationKind.LINK,
                url = url,
            ).toBarcodeContent()
        }

        // Then
        assertEquals(
            expected = listOf(
                BarcodeContent.Url(url = "https://scanny.app"),
                BarcodeContent.Url(url = "http://scanny.app"),
            ),
            actual = contents,
        )
    }

    @Test
    fun `an e-mail needs its recipient and leaves out a blank subject and body`() {
        // When
        val withoutRecipient = CreationForm(
            kind = CreationKind.EMAIL,
            emailSubject = "Hello",
        ).toBarcodeContent()
        val withRecipient = CreationForm(
            kind = CreationKind.EMAIL,
            emailAddress = " hello@scanny.app ",
            emailSubject = " ",
        ).toBarcodeContent()

        // Then
        assertNull(withoutRecipient)
        assertEquals(
            expected = BarcodeContent.Email(address = "hello@scanny.app"),
            actual = withRecipient,
        )
    }

    @Test
    fun `an sms needs its phone number`() {
        // When
        val contents = listOf("", "+33600000000").map { phoneNumber ->
            CreationForm(
                kind = CreationKind.SMS,
                smsPhoneNumber = phoneNumber,
                smsMessage = "On my way",
            ).toBarcodeContent()
        }

        // Then
        assertEquals(
            expected = listOf(
                null,
                BarcodeContent.Sms(
                    phoneNumber = "+33600000000",
                    message = "On my way",
                ),
            ),
            actual = contents,
        )
    }

    @Test
    fun `a wpa network needs a name and a password of 8 to 63 characters`() {
        // When
        val contents = listOf(
            "Scanny" to "short",
            "" to "long enough",
            "Scanny" to "long enough",
        ).map { (ssid, password) ->
            CreationForm(
                kind = CreationKind.WIFI,
                wifiSsid = ssid,
                wifiEncryption = WifiEncryption.WPA,
                wifiPassword = password,
            ).toBarcodeContent()
        }

        // Then
        assertEquals(
            expected = listOf(
                null,
                null,
                BarcodeContent.Wifi(
                    ssid = "Scanny",
                    encryption = WifiEncryption.WPA,
                    password = "long enough",
                ),
            ),
            actual = contents,
        )
    }

    @Test
    fun `an open network keeps no password`() {
        // When
        val content = CreationForm(
            kind = CreationKind.WIFI,
            wifiSsid = "Scanny",
            wifiEncryption = WifiEncryption.NONE,
            wifiPassword = "typed before",
            isWifiHidden = true,
        ).toBarcodeContent()

        // Then
        assertEquals(
            expected = BarcodeContent.Wifi(
                ssid = "Scanny",
                encryption = WifiEncryption.NONE,
                isHidden = true,
            ),
            actual = content,
        )
    }

    @Test
    fun `a contact needs a name and keeps only the fields filled in`() {
        // When
        val withoutName = CreationForm(
            kind = CreationKind.CONTACT,
            contactPhone = "+33600000000",
        ).toBarcodeContent()
        val withName = CreationForm(
            kind = CreationKind.CONTACT,
            contactName = " John Doe ",
            contactPhone = "+33600000000",
            contactEmail = " ",
        ).toBarcodeContent()

        // Then
        assertNull(withoutName)
        assertEquals(
            expected = BarcodeContent.Contact(
                formattedName = "John Doe",
                phones = listOf("+33600000000"),
            ),
            actual = withName,
        )
    }

    @Test
    fun `a barcode holds its value as a text in the format chosen and the others are qr codes`() {
        // Given
        val barcode = CreationForm(
            kind = CreationKind.BARCODE,
            barcodeFormat = BarcodeFormat.CODE_128,
            barcodeValue = " SCANNY-128 ",
        )

        // Then
        assertEquals(
            expected = Triple(BarcodeContent.Text(text = "SCANNY-128"), BarcodeFormat.CODE_128, BarcodeFormat.QR_CODE),
            actual = Triple(barcode.toBarcodeContent(), barcode.format, CreationForm(kind = CreationKind.WIFI).format),
        )
    }
}