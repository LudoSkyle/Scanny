package fr.skyle.scanny.platform.link

import kotlin.test.Test
import kotlin.test.assertEquals

class SmsLinkTest {

    @Test
    fun `ios separates the message with an ampersand`() {
        // When
        val link = platformSmsLink(smsLink = "sms:+33600000000?body=Hello%3F")

        // Then
        assertEquals(
            expected = "sms:+33600000000&body=Hello%3F",
            actual = link,
        )
    }

    @Test
    fun `ios keeps a link without message as written`() {
        // When
        val link = platformSmsLink(smsLink = "sms:+33600000000")

        // Then
        assertEquals(
            expected = "sms:+33600000000",
            actual = link,
        )
    }
}