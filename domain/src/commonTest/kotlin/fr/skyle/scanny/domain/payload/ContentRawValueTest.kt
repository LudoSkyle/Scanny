package fr.skyle.scanny.domain.payload

import fr.skyle.scanny.domain.model.BarcodeContent
import kotlin.test.Test
import kotlin.test.assertEquals

class ContentRawValueTest {

    @Test
    fun `the text of a content is the one the parser reads back`() {
        // Given
        val email = BarcodeContent.Email(
            address = "hello@scanny.app",
            subject = "Hi there",
        )

        // When
        val rawValue = email.toRawValue()

        // Then
        assertEquals(
            expected = "mailto:hello@scanny.app?subject=Hi%20there" to email,
            actual = rawValue to BarcodeContentParser.parse(rawValue = rawValue),
        )
    }
}