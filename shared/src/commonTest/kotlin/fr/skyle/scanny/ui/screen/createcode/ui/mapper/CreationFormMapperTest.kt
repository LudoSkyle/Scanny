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
    fun `a phone needs its number`() {
        // When
        val contents = listOf("  ", " +33 6 00 00 00 00 ").map { phoneNumber ->
            CreationForm(
                kind = CreationKind.PHONE,
                phoneNumber = phoneNumber,
            ).toBarcodeContent()
        }

        // Then
        assertEquals(
            expected = listOf(
                null,
                BarcodeContent.Phone(phoneNumber = "+33 6 00 00 00 00"),
            ),
            actual = contents,
        )
    }

    @Test
    fun `a location takes a decimal comma or point and keeps its place if named`() {
        // When
        val contents = listOf(
            CreationForm(
                kind = CreationKind.LOCATION,
                locationLatitude = " 48,8584 ",
                locationLongitude = "2.2945",
                locationLabel = " Tour Eiffel ",
            ),
            CreationForm(
                kind = CreationKind.LOCATION,
                locationLatitude = "-33.8568",
                locationLongitude = "151,2153",
                locationLabel = " ",
            ),
        ).map { form -> form.toBarcodeContent() }

        // Then
        assertEquals(
            expected = listOf(
                BarcodeContent.Location(
                    latitude = 48.8584,
                    longitude = 2.2945,
                    label = "Tour Eiffel",
                ),
                BarcodeContent.Location(
                    latitude = -33.8568,
                    longitude = 151.2153,
                ),
            ),
            actual = contents,
        )
    }

    @Test
    fun `a location needs both coordinates on the map`() {
        // When
        val contents = listOf(
            "48.8584" to "",
            "91" to "2.2945",
            "48.8584" to "-181",
            "48,85,84" to "2.2945",
            "NaN" to "2.2945",
        ).map { (latitude, longitude) ->
            CreationForm(
                kind = CreationKind.LOCATION,
                locationLatitude = latitude,
                locationLongitude = longitude,
            ).toBarcodeContent()
        }

        // Then
        assertEquals(
            expected = listOf(null, null, null, null, null),
            actual = contents,
        )
    }

    @Test
    fun `a bookmark needs its link which gets https without its scheme`() {
        // When
        val contents = listOf(
            CreationForm(
                kind = CreationKind.BOOKMARK,
                bookmarkTitle = "Scanny",
            ),
            CreationForm(
                kind = CreationKind.BOOKMARK,
                bookmarkTitle = " Scanny ",
                bookmarkUrl = " scanny.app ",
            ),
        ).map { form -> form.toBarcodeContent() }

        // Then
        assertEquals(
            expected = listOf(
                null,
                BarcodeContent.Bookmark(
                    url = "https://scanny.app",
                    title = "Scanny",
                ),
            ),
            actual = contents,
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