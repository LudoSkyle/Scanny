package fr.skyle.scanny.platform.link

import kotlin.test.Test
import kotlin.test.assertEquals

class SmsLinkTest {

    @Test
    fun `android opens the sms link as written`() {
        // When
        val link = platformSmsLink(smsLink = "sms:+33600000000?body=Hello")

        // Then
        assertEquals(
            expected = "sms:+33600000000?body=Hello",
            actual = link,
        )
    }
}