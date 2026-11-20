package fr.skyle.scanny.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class BarcodeContentTest {

    @Test
    fun `a contact is shown by its formatted name first`() {
        // Given
        val contact = BarcodeContent.Contact(
            formattedName = "Dr John Doe",
            firstName = "John",
            lastName = "Doe",
        )

        // When
        val displayName = contact.displayName

        // Then
        assertEquals(
            expected = "Dr John Doe",
            actual = displayName,
        )
    }

    @Test
    fun `a contact without formatted name is shown by its first and last names`() {
        // Given
        val contacts = listOf(
            BarcodeContent.Contact(
                firstName = "John",
                lastName = "Doe",
            ),
            BarcodeContent.Contact(lastName = "Doe"),
        )

        // When
        val displayNames = contacts.map { contact ->
            contact.displayName
        }

        // Then
        assertEquals(
            expected = listOf(
                "John Doe",
                "Doe",
            ),
            actual = displayNames,
        )
    }

    @Test
    fun `a contact without any name has no name to show`() {
        // Given
        val contact = BarcodeContent.Contact(organization = "Doe & Co")

        // When
        val displayName = contact.displayName

        // Then
        assertNull(displayName)
    }
}