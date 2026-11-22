package fr.skyle.scanny.ui.core.code.mapper

import fr.skyle.scanny.domain.model.BarcodeContent
import fr.skyle.scanny.domain.model.BarcodeFormat
import fr.skyle.scanny.domain.model.OtpAlgorithm
import fr.skyle.scanny.domain.model.OtpType
import fr.skyle.scanny.domain.model.ScannedCode
import fr.skyle.scanny.domain.model.WifiEncryption
import fr.skyle.scanny.platform.link.platformMapsLink
import fr.skyle.scanny.platform.link.platformMmsLink
import fr.skyle.scanny.platform.link.platformSmsLink
import fr.skyle.scanny.ui.core.code.model.CodeAction
import fr.skyle.scanny.ui.core.code.model.CodeField
import fr.skyle.scanny.ui.core.code.model.CodeFieldLabel
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Instant

class CodeMapperTest {

    @Test
    fun `a text is a single field without label`() {
        // Given
        val content = BarcodeContent.Text(text = "Hello Scanny")

        // When
        val fields = content.toCodeFields()

        // Then
        assertEquals(
            expected = listOf(
                CodeField(
                    label = null,
                    value = "Hello Scanny",
                ),
            ),
            actual = fields,
        )
    }

    @Test
    fun `a wifi network shows its network its security and its password`() {
        // Given
        val content = BarcodeContent.Wifi(
            ssid = "Scanny",
            encryption = WifiEncryption.WPA3,
            password = "secret",
        )

        // When
        val fields = content.toCodeFields()

        // Then
        assertEquals(
            expected = listOf(
                field(
                    label = CodeFieldLabel.NETWORK,
                    value = "Scanny",
                ),
                field(
                    label = CodeFieldLabel.SECURITY,
                    value = "WPA3",
                ),
                field(
                    label = CodeFieldLabel.PASSWORD,
                    value = "secret",
                ),
            ),
            actual = fields,
        )
    }

    @Test
    fun `an open wifi network only shows its network`() {
        // Given
        val content = BarcodeContent.Wifi(
            ssid = "Guest",
            encryption = WifiEncryption.NONE,
            password = "unused",
        )

        // When
        val fields = content.toCodeFields()

        // Then
        assertEquals(
            expected = listOf(
                field(
                    label = CodeFieldLabel.NETWORK,
                    value = "Guest",
                ),
            ),
            actual = fields,
        )
    }

    @Test
    fun `an email only shows the parts it has`() {
        // Given
        val content = BarcodeContent.Email(
            to = listOf("john@doe.fr"),
            body = "Hello",
        )

        // When
        val fields = content.toCodeFields()

        // Then
        assertEquals(
            expected = listOf(
                field(
                    label = CodeFieldLabel.RECIPIENT,
                    value = "john@doe.fr",
                ),
                field(
                    label = CodeFieldLabel.MESSAGE,
                    value = "Hello",
                ),
            ),
            actual = fields,
        )
    }

    @Test
    fun `a contact shows its name then each of its phones emails addresses and websites`() {
        // Given
        val content = BarcodeContent.Contact(
            firstName = "John",
            lastName = "Doe",
            organization = "Doe & Co",
            title = "Lead",
            phones = listOf(
                "+33600000000",
                "+33100000000",
            ),
            emails = listOf("john@doe.fr"),
            addresses = listOf("12 rue de la Paix, Paris"),
            urls = listOf("https://doe.fr"),
        )

        // When
        val fields = content.toCodeFields()

        // Then
        assertEquals(
            expected = listOf(
                field(
                    label = CodeFieldLabel.NAME,
                    value = "John Doe",
                ),
                field(
                    label = CodeFieldLabel.ORGANIZATION,
                    value = "Doe & Co",
                ),
                field(
                    label = CodeFieldLabel.JOB_TITLE,
                    value = "Lead",
                ),
                field(
                    label = CodeFieldLabel.PHONE,
                    value = "+33600000000",
                ),
                field(
                    label = CodeFieldLabel.PHONE,
                    value = "+33100000000",
                ),
                field(
                    label = CodeFieldLabel.EMAIL,
                    value = "john@doe.fr",
                ),
                field(
                    label = CodeFieldLabel.ADDRESS,
                    value = "12 rue de la Paix, Paris",
                ),
                field(
                    label = CodeFieldLabel.WEBSITE,
                    value = "https://doe.fr",
                ),
            ),
            actual = fields,
        )
    }

    @Test
    fun `a location shows its place and its coordinates and a bookmark its title and its link`() {
        // Given
        val contents = listOf(
            BarcodeContent.Location(
                latitude = 48.8584,
                longitude = 2.2945,
                label = "Tour Eiffel",
            ),
            BarcodeContent.Location(
                latitude = -33.8568,
                longitude = 151.2153,
            ),
            BarcodeContent.Bookmark(
                url = "https://scanny.app",
                title = "Scanny",
            ),
            BarcodeContent.Phone(phoneNumber = "+33600000000"),
        )

        // When
        val fields = contents.map { content -> content.toCodeFields() }

        // Then
        assertEquals(
            expected = listOf(
                listOf(
                    field(
                        label = CodeFieldLabel.PLACE,
                        value = "Tour Eiffel",
                    ),
                    field(
                        label = CodeFieldLabel.COORDINATES,
                        value = "48.8584, 2.2945",
                    ),
                ),
                listOf(
                    field(
                        label = CodeFieldLabel.COORDINATES,
                        value = "-33.8568, 151.2153",
                    ),
                ),
                listOf(
                    field(
                        label = CodeFieldLabel.TITLE,
                        value = "Scanny",
                    ),
                    field(
                        label = CodeFieldLabel.LINK,
                        value = "https://scanny.app",
                    ),
                ),
                listOf(
                    field(
                        label = CodeFieldLabel.PHONE_NUMBER,
                        value = "+33600000000",
                    ),
                ),
            ),
            actual = fields,
        )
    }

    @Test
    fun `an otp key shows its issuer its account and its secret and a bitcoin payment its parts`() {
        // Given
        val contents = listOf(
            BarcodeContent.Otp(
                secret = "JBSWY3DPEHPK3PXP",
                issuer = "Scanny",
                account = "john@doe.fr",
                digits = 8,
            ),
            BarcodeContent.Bitcoin(
                address = "1BoatSLRHtKNngkdXEeobR76b53LETtpyT",
                amount = "0.0015",
                label = "Scanny shop",
                message = "Order 42",
            ),
        )

        // When
        val fields = contents.map { content -> content.toCodeFields() }

        // Then
        assertEquals(
            expected = listOf(
                listOf(
                    field(
                        label = CodeFieldLabel.ISSUER,
                        value = "Scanny",
                    ),
                    field(
                        label = CodeFieldLabel.ACCOUNT,
                        value = "john@doe.fr",
                    ),
                    field(
                        label = CodeFieldLabel.SECRET_KEY,
                        value = "JBSWY3DPEHPK3PXP",
                    ),
                    field(
                        label = CodeFieldLabel.OTP_TYPE,
                        value = "TOTP",
                    ),
                    field(
                        label = CodeFieldLabel.ALGORITHM,
                        value = "SHA1",
                    ),
                    field(
                        label = CodeFieldLabel.DIGITS,
                        value = "8",
                    ),
                    field(
                        label = CodeFieldLabel.PERIOD,
                        value = "30 s",
                    ),
                ),
                listOf(
                    field(
                        label = CodeFieldLabel.ADDRESS,
                        value = "1BoatSLRHtKNngkdXEeobR76b53LETtpyT",
                    ),
                    field(
                        label = CodeFieldLabel.AMOUNT,
                        value = "0.0015 BTC",
                    ),
                    field(
                        label = CodeFieldLabel.RECIPIENT,
                        value = "Scanny shop",
                    ),
                    field(
                        label = CodeFieldLabel.MESSAGE,
                        value = "Order 42",
                    ),
                ),
            ),
            actual = fields,
        )
    }

    @Test
    fun `a hotp key shows its counter instead of a period`() {
        // Given
        val content = BarcodeContent.Otp(
            secret = "JBSWY3DPEHPK3PXP",
            type = OtpType.HOTP,
            account = "john",
            algorithm = OtpAlgorithm.SHA512,
            counter = 42,
        )

        // When
        val fields = content.toCodeFields()

        // Then
        assertEquals(
            expected = listOf(
                field(
                    label = CodeFieldLabel.ACCOUNT,
                    value = "john",
                ),
                field(
                    label = CodeFieldLabel.SECRET_KEY,
                    value = "JBSWY3DPEHPK3PXP",
                ),
                field(
                    label = CodeFieldLabel.OTP_TYPE,
                    value = "HOTP",
                ),
                field(
                    label = CodeFieldLabel.ALGORITHM,
                    value = "SHA512",
                ),
                field(
                    label = CodeFieldLabel.DIGITS,
                    value = "6",
                ),
                field(
                    label = CodeFieldLabel.COUNTER,
                    value = "42",
                ),
            ),
            actual = fields,
        )
    }

    @Test
    fun `an mms shows its number its subject and its message`() {
        // Given
        val content = BarcodeContent.Mms(
            phoneNumber = "+33600000000",
            subject = "Hello",
            message = "See you",
        )

        // When
        val fields = content.toCodeFields()

        // Then
        assertEquals(
            expected = listOf(
                field(
                    label = CodeFieldLabel.PHONE_NUMBER,
                    value = "+33600000000",
                ),
                field(
                    label = CodeFieldLabel.SUBJECT,
                    value = "Hello",
                ),
                field(
                    label = CodeFieldLabel.MESSAGE,
                    value = "See you",
                ),
            ),
            actual = fields,
        )
    }

    @Test
    fun `the summary of a code is its most telling part`() {
        // Given
        val codes = listOf(
            scannedCode(
                content = BarcodeContent.Wifi(
                    ssid = "Scanny",
                    encryption = WifiEncryption.WPA,
                    password = "secret",
                ),
            ),
            scannedCode(content = BarcodeContent.Email(subject = "Hello")),
            scannedCode(content = BarcodeContent.Sms(phoneNumber = "+33600000000")),
            scannedCode(content = BarcodeContent.Contact(formattedName = "John Doe")),
            scannedCode(content = BarcodeContent.Contact(organization = "Doe & Co")),
            scannedCode(content = BarcodeContent.Phone(phoneNumber = "+33100000000")),
            scannedCode(
                content = BarcodeContent.Location(
                    latitude = 48.8584,
                    longitude = 2.2945,
                ),
            ),
            scannedCode(content = BarcodeContent.Bookmark(url = "https://scanny.app")),
            scannedCode(content = BarcodeContent.Mms(subject = "Hello MMS")),
            scannedCode(
                content = BarcodeContent.Otp(
                    secret = "JBSWY3DPEHPK3PXP",
                    issuer = "Scanny",
                    account = "john@doe.fr",
                ),
            ),
            scannedCode(
                content = BarcodeContent.Otp(secret = "JBSWY3DPEHPK3PXP"),
                rawValue = "otpauth://totp/?secret=JBSWY3DPEHPK3PXP",
            ),
            scannedCode(content = BarcodeContent.Bitcoin(address = "1BoatSLRHtKNngkdXEeobR76b53LETtpyT")),
        )

        // When
        val summaries = codes.map { code ->
            code.toCodeSummary()
        }

        // Then
        assertEquals(
            expected = listOf(
                "Scanny",
                "Hello",
                "+33600000000",
                "John Doe",
                "Doe & Co",
                "+33100000000",
                "48.8584, 2.2945",
                "https://scanny.app",
                "Hello MMS",
                "Scanny: john@doe.fr",
                "otpauth://totp/?secret=JBSWY3DPEHPK3PXP",
                "1BoatSLRHtKNngkdXEeobR76b53LETtpyT",
            ),
            actual = summaries,
        )
    }

    @Test
    fun `a contact with nothing to show is summed up by its raw value`() {
        // Given
        val code = scannedCode(
            content = BarcodeContent.Contact(addresses = listOf("12 rue de la Paix")),
            rawValue = "BEGIN:VCARD",
        )

        // When
        val summary = code.toCodeSummary()

        // Then
        assertEquals(
            expected = "BEGIN:VCARD",
            actual = summary,
        )
    }

    @Test
    fun `a link an email and an sms each open their app`() {
        // Given
        val contents = listOf(
            BarcodeContent.Url(url = "https://scanny.app"),
            BarcodeContent.Email(to = listOf("john@doe.fr")),
            BarcodeContent.Sms(
                phoneNumber = "+33600000000",
                message = "Hello",
            ),
        )

        // When
        val actions = contents.map { content ->
            content.toCodeAction(canJoinWifi = { false })
        }

        // Then
        assertEquals(
            expected = listOf(
                CodeAction.OpenLink(link = "https://scanny.app"),
                CodeAction.WriteEmail(link = "mailto:john@doe.fr"),
                CodeAction.WriteSms(link = platformSmsLink(smsLink = "sms:+33600000000?body=Hello")),
            ),
            actual = actions,
        )
    }

    @Test
    fun `a phone is called a place shown on the map and a bookmark opened`() {
        // Given
        val location = BarcodeContent.Location(
            latitude = 48.8584,
            longitude = 2.2945,
        )
        val contents = listOf(
            BarcodeContent.Phone(phoneNumber = "+33600000000"),
            location,
            BarcodeContent.Bookmark(
                url = "https://scanny.app",
                title = "Scanny",
            ),
        )

        // When
        val actions = contents.map { content ->
            content.toCodeAction(canJoinWifi = { false })
        }

        // Then
        assertEquals(
            expected = listOf(
                CodeAction.Call(link = "tel:+33600000000"),
                CodeAction.ShowOnMap(link = platformMapsLink(location = location)),
                CodeAction.OpenLink(link = "https://scanny.app"),
            ),
            actual = actions,
        )
    }

    @Test
    fun `an otp key is added to an authenticator and a payment opened in a wallet`() {
        // Given
        val contents = listOf(
            BarcodeContent.Otp(
                secret = "JBSWY3DPEHPK3PXP",
                account = "john",
            ),
            BarcodeContent.Bitcoin(
                address = "1BoatSLRHtKNngkdXEeobR76b53LETtpyT",
                amount = "0.5",
            ),
        )

        // When
        val actions = contents.map { content ->
            content.toCodeAction(canJoinWifi = { false })
        }

        // Then
        assertEquals(
            expected = listOf(
                CodeAction.AddToAuthenticator(link = "otpauth://totp/john?secret=JBSWY3DPEHPK3PXP"),
                CodeAction.OpenWallet(link = "bitcoin:1BoatSLRHtKNngkdXEeobR76b53LETtpyT?amount=0.5"),
            ),
            actual = actions,
        )
    }

    @Test
    fun `an mms is written in the messages app`() {
        // Given
        val mms = BarcodeContent.Mms(
            phoneNumber = "+33600000000",
            subject = "Hello",
        )

        // When
        val action = mms.toCodeAction(canJoinWifi = { false })

        // Then
        assertEquals(
            expected = CodeAction.WriteMms(link = platformMmsLink(mms = mms)),
            actual = action,
        )
    }

    @Test
    fun `a text has no action`() {
        // When
        val action = BarcodeContent.Text(text = "Hello").toCodeAction(canJoinWifi = { true })

        // Then
        assertNull(action)
    }

    @Test
    fun `a contact is added to the contacts`() {
        // Given
        val contact = BarcodeContent.Contact(formattedName = "John Doe")

        // When
        val action = contact.toCodeAction(canJoinWifi = { false })

        // Then
        assertEquals(
            expected = CodeAction.AddContact(contact = contact),
            actual = action,
        )
    }

    @Test
    fun `a network is joined only when the device can`() {
        // Given
        val wifi = BarcodeContent.Wifi(
            ssid = "Scanny",
            encryption = WifiEncryption.WPA,
            password = "secret12",
        )

        // When
        val actions = listOf(true, false).map { canJoin ->
            wifi.toCodeAction(canJoinWifi = { canJoin })
        }

        // Then
        assertEquals(
            expected = listOf(
                CodeAction.JoinWifi(wifi = wifi),
                null,
            ),
            actual = actions,
        )
    }

    private fun field(
        label: CodeFieldLabel,
        value: String,
    ): CodeField =
        CodeField(
            label = label,
            value = value,
        )

    private fun scannedCode(
        content: BarcodeContent,
        rawValue: String = "raw",
    ): ScannedCode =
        ScannedCode(
            id = 1,
            scanId = 1,
            rawValue = rawValue,
            content = content,
            format = BarcodeFormat.QR_CODE,
            scannedAt = Instant.fromEpochMilliseconds(0),
        )
}