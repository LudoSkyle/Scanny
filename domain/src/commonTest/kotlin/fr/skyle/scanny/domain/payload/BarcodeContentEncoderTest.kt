package fr.skyle.scanny.domain.payload

import fr.skyle.scanny.domain.model.BarcodeContent
import fr.skyle.scanny.domain.model.WifiEncryption
import kotlin.test.Test
import kotlin.test.assertEquals

class BarcodeContentEncoderTest {

    @Test
    fun `a text is written as is`() {
        // Given
        val content = BarcodeContent.Text(text = "Hello Scanny")

        // When
        val payload = encode(content = content)

        // Then
        assertEquals(
            expected = "Hello Scanny",
            actual = payload,
        )
    }

    @Test
    fun `an url is written as is`() {
        // Given
        val content = BarcodeContent.Url(url = "https://scanny.app")

        // When
        val payload = encode(content = content)

        // Then
        assertEquals(
            expected = "https://scanny.app",
            actual = payload,
        )
    }

    @Test
    fun `an email is written as a percent encoded mailto link`() {
        // Given
        val content = BarcodeContent.Email(
            address = "john@doe.fr",
            subject = "Hello Scanny",
            body = "Line 1\nLine 2",
        )

        // When
        val payload = encode(content = content)

        // Then
        assertEquals(
            expected = "mailto:john@doe.fr?subject=Hello%20Scanny&body=Line%201%0ALine%202",
            actual = payload,
        )
    }

    @Test
    fun `an email without subject and body has no query`() {
        // Given
        val content = BarcodeContent.Email(address = "john@doe.fr")

        // When
        val payload = encode(content = content)

        // Then
        assertEquals(
            expected = "mailto:john@doe.fr",
            actual = payload,
        )
    }

    @Test
    fun `an sms is written as an smsto payload`() {
        // Given
        val content = BarcodeContent.Sms(
            phoneNumber = "+33600000000",
            message = "Hello",
        )

        // When
        val payload = encode(content = content)

        // Then
        assertEquals(
            expected = "SMSTO:+33600000000:Hello",
            actual = payload,
        )
    }

    @Test
    fun `a wifi network is written with its escaped fields`() {
        // Given
        val content = BarcodeContent.Wifi(
            ssid = "My;Net",
            encryption = WifiEncryption.WPA,
            password = "pa:ss",
        )

        // When
        val payload = encode(content = content)

        // Then
        assertEquals(
            expected = """WIFI:T:WPA;S:My\;Net;P:pa\:ss;;""",
            actual = payload,
        )
    }

    @Test
    fun `an open wifi network is written without password`() {
        // Given
        val content = BarcodeContent.Wifi(
            ssid = "Guest",
            encryption = WifiEncryption.NONE,
            password = "unused",
        )

        // When
        val payload = encode(content = content)

        // Then
        assertEquals(
            expected = "WIFI:T:nopass;S:Guest;;",
            actual = payload,
        )
    }

    @Test
    fun `a hidden wpa3 network is written as a hidden sae network`() {
        // Given
        val content = BarcodeContent.Wifi(
            ssid = "Modern",
            encryption = WifiEncryption.WPA3,
            password = "secret",
            isHidden = true,
        )

        // When
        val payload = encode(content = content)

        // Then
        assertEquals(
            expected = "WIFI:T:SAE;S:Modern;P:secret;H:true;;",
            actual = payload,
        )
    }

    @Test
    fun `a contact is written as a vcard 3`() {
        // Given
        val content = BarcodeContent.Contact(
            formattedName = "John Doe",
            firstName = "John",
            lastName = "Doe",
            organization = "Doe, Smith & Co",
            phones = listOf("+33600000000"),
            emails = listOf("john@doe.fr"),
            addresses = listOf("12 rue de la Paix, Paris"),
            urls = listOf("https://doe.fr"),
        )

        // When
        val payload = encode(content = content)

        // Then
        assertEquals(
            expected = listOf(
                "BEGIN:VCARD",
                "VERSION:3.0",
                "N:Doe;John;;;",
                "FN:John Doe",
                "ORG:Doe\\, Smith & Co",
                "TEL:+33600000000",
                "EMAIL:john@doe.fr",
                "ADR:;;12 rue de la Paix\\, Paris;;;;",
                "URL:https://doe.fr",
                "END:VCARD",
            ).joinToString(separator = "\n"),
            actual = payload,
        )
    }

    @Test
    fun `every written content is read back unchanged`() {
        // Given
        val contents = listOf(
            BarcodeContent.Text(text = "Hello: Scanny; with, special \\ characters\n"),
            BarcodeContent.Url(url = "https://scanny.app/path?query=1&other=été"),
            BarcodeContent.Email(
                address = "john+scan@doe.fr",
                subject = "100% sure & certain = yes?",
                body = "Été\nà bientôt",
            ),
            BarcodeContent.Email(body = "Only a body"),
            BarcodeContent.Sms(
                phoneNumber = "+33600000000",
                message = "Meet at 10:30; okay?",
            ),
            BarcodeContent.Sms(message = "Only a message"),
            BarcodeContent.Wifi(
                ssid = """My;Net,with:all\special"chars""",
                encryption = WifiEncryption.WPA,
                password = """pa;ss,wo:rd\with"quotes""",
            ),
            BarcodeContent.Wifi(
                ssid = "\"Quoted\"",
                encryption = WifiEncryption.WEP,
                password = "\"12345\"",
                isHidden = true,
            ),
            BarcodeContent.Wifi(
                ssid = "Guest",
                encryption = WifiEncryption.NONE,
            ),
            BarcodeContent.Wifi(
                ssid = "Modern",
                encryption = WifiEncryption.WPA3,
                password = "secret",
            ),
            BarcodeContent.Contact(
                formattedName = "John; Doe, Jr\nthe second",
                firstName = "John",
                lastName = "Doe; Smith",
                organization = "Doe, Smith & Co",
                title = "Lead\\developer",
                phones = listOf(
                    "+33600000000",
                    "+33100000000",
                ),
                emails = listOf("john@doe.fr"),
                addresses = listOf("12 rue de la Paix, 75002 Paris; France"),
                urls = listOf("https://doe.fr"),
            ),
            BarcodeContent.Contact(firstName = "John"),
        )

        // When
        val readContents = contents.map { content ->
            BarcodeContentParser.parse(rawValue = encode(content = content))
        }

        // Then
        assertEquals(
            expected = contents,
            actual = readContents,
        )
    }

    private fun encode(content: BarcodeContent): String =
        BarcodeContentEncoder.encode(content = content)
}