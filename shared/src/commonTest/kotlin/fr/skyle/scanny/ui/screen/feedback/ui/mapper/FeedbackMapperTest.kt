package fr.skyle.scanny.ui.screen.feedback.ui.mapper

import fr.skyle.scanny.ui.screen.feedback.ui.model.FeedbackSubject
import kotlin.test.Test
import kotlin.test.assertEquals

class FeedbackMapperTest {

    @Test
    fun `a feedback is an e-mail to scanny tagged with its subject`() {
        // When
        val link = FeedbackSubject.CODE_DETECTION.toFeedbackMailtoLink(message = "A vCard read as a text")

        // Then
        assertEquals(
            expected = "mailto:scanny.contact@gmail.com?subject=%5BCODE_DETECTION%5D&body=A%20vCard%20read%20as%20a%20text",
            actual = link,
        )
    }
}