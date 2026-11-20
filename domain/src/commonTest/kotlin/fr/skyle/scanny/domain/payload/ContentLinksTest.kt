package fr.skyle.scanny.domain.payload

import fr.skyle.scanny.domain.model.BarcodeContent
import kotlin.test.Test
import kotlin.test.assertEquals

class ContentLinksTest {

    @Test
    fun `an email link is a percent encoded mailto link`() {
        // Given
        val email = BarcodeContent.Email(
            address = "john@doe.fr",
            subject = "Hello Scanny",
            body = "Line 1\nLine 2",
        )

        // When
        val link = email.toMailtoLink()

        // Then
        assertEquals(
            expected = "mailto:john@doe.fr?subject=Hello%20Scanny&body=Line%201%0ALine%202",
            actual = link,
        )
    }

    @Test
    fun `an sms link carries the percent encoded message in its body`() {
        // Given
        val sms = BarcodeContent.Sms(
            phoneNumber = "+33600000000",
            message = "Meet at 10:30 & bring été",
        )

        // When
        val link = sms.toSmsLink()

        // Then
        assertEquals(
            expected = "sms:+33600000000?body=Meet%20at%2010%3A30%20%26%20bring%20%C3%A9t%C3%A9",
            actual = link,
        )
    }

    @Test
    fun `an sms link without message has no body`() {
        // Given
        val sms = BarcodeContent.Sms(phoneNumber = "+33 6 00 00 00 00")

        // When
        val link = sms.toSmsLink()

        // Then
        assertEquals(
            expected = "sms:+33%206%2000%2000%2000%2000",
            actual = link,
        )
    }

    @Test
    fun `an sms link without number only carries the message`() {
        // Given
        val sms = BarcodeContent.Sms(message = "Hello")

        // When
        val link = sms.toSmsLink()

        // Then
        assertEquals(
            expected = "sms:?body=Hello",
            actual = link,
        )
    }
}