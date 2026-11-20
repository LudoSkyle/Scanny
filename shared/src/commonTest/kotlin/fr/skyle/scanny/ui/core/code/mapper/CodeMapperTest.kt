package fr.skyle.scanny.ui.core.code.mapper

import fr.skyle.scanny.domain.model.BarcodeContent
import fr.skyle.scanny.domain.model.BarcodeFormat
import fr.skyle.scanny.domain.model.ScannedCode
import fr.skyle.scanny.domain.model.WifiEncryption
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
            address = "john@doe.fr",
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
            BarcodeContent.Email(address = "john@doe.fr"),
            BarcodeContent.Sms(
                phoneNumber = "+33600000000",
                message = "Hello",
            ),
        )

        // When
        val actions = contents.map { content ->
            content.toCodeAction()
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
    fun `a text has no action`() {
        // When
        val action = BarcodeContent.Text(text = "Hello").toCodeAction()

        // Then
        assertNull(action)
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