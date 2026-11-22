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

    @Test
    fun `a phone link is a percent encoded tel link`() {
        // Given
        val phone = BarcodeContent.Phone(phoneNumber = "+33 6 00 00 00 00")

        // When
        val link = phone.toTelLink()

        // Then
        assertEquals(
            expected = "tel:+33%206%2000%2000%2000%2000",
            actual = link,
        )
    }

    @Test
    fun `a geo link pins the place named or not`() {
        // Given
        val locations = listOf(
            BarcodeContent.Location(
                latitude = 48.8584,
                longitude = 2.2945,
                label = "Tour Eiffel",
            ),
            BarcodeContent.Location(
                latitude = 48.8584,
                longitude = 2.2945,
            ),
        )

        // When
        val links = locations.map { location -> location.toGeoLink() }

        // Then
        assertEquals(
            expected = listOf(
                "geo:48.8584,2.2945?q=48.8584,2.2945(Tour%20Eiffel)",
                "geo:48.8584,2.2945?q=48.8584,2.2945",
            ),
            actual = links,
        )
    }

    @Test
    fun `an apple maps link pins the place under its name or its coordinates`() {
        // Given
        val locations = listOf(
            BarcodeContent.Location(
                latitude = 48.8584,
                longitude = 2.2945,
                label = "Tour Eiffel & co",
            ),
            BarcodeContent.Location(
                latitude = -33.8568,
                longitude = 151.2153,
            ),
        )

        // When
        val links = locations.map { location -> location.toAppleMapsLink() }

        // Then
        assertEquals(
            expected = listOf(
                "https://maps.apple.com/?ll=48.8584,2.2945&q=Tour%20Eiffel%20%26%20co",
                "https://maps.apple.com/?ll=-33.8568,151.2153&q=-33.8568,151.2153",
            ),
            actual = links,
        )
    }
}
