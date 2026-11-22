package fr.skyle.scanny.ui.screen.createcode.ui.mapper

import fr.skyle.scanny.domain.model.CodeErrorCorrection
import fr.skyle.scanny.domain.model.CodeStyle
import fr.skyle.scanny.ui.screen.createcode.ui.model.ErrorCorrectionBlocker
import fr.skyle.scanny.ui.screen.createcode.ui.model.LogoBlocker
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ErrorCorrectionMapperTest {

    @Test
    fun `every correction fits a content still incomplete`() {
        // When
        val choices = errorCorrectionChoices(
            rawValue = null,
            style = CodeStyle(),
        )

        // Then
        assertEquals(
            expected = CodeErrorCorrection.entries.associateWith { null },
            actual = choices.associate { choice -> choice.errorCorrection to choice.blocker },
        )
    }

    @Test
    fun `a correction too high for the content is told so`() {
        // When
        // 2000 bytes: held up to the medium correction (2331), not beyond (1663 at the high one)
        val choices = errorCorrectionChoices(
            rawValue = LONG_CONTENT,
            style = CodeStyle(),
        )

        // Then
        assertEquals(
            expected = mapOf(
                CodeErrorCorrection.AUTO to null,
                CodeErrorCorrection.LOW to null,
                CodeErrorCorrection.MEDIUM to null,
                CodeErrorCorrection.QUARTILE to ErrorCorrectionBlocker.TOO_HIGH_FOR_CONTENT,
                CodeErrorCorrection.HIGH to ErrorCorrectionBlocker.TOO_HIGH_FOR_CONTENT,
            ),
            actual = choices.associate { choice -> choice.errorCorrection to choice.blocker },
        )
    }

    @Test
    fun `a logo leaves only the corrections that give it the highest`() {
        // When
        val choices = errorCorrectionChoices(
            rawValue = "https://scanny.app",
            style = CodeStyle(logo = byteArrayOf(1, 2, 3)),
        )

        // Then
        assertEquals(
            expected = mapOf(
                CodeErrorCorrection.AUTO to null,
                CodeErrorCorrection.LOW to ErrorCorrectionBlocker.TOO_LOW_FOR_LOGO,
                CodeErrorCorrection.MEDIUM to ErrorCorrectionBlocker.TOO_LOW_FOR_LOGO,
                CodeErrorCorrection.QUARTILE to ErrorCorrectionBlocker.TOO_LOW_FOR_LOGO,
                CodeErrorCorrection.HIGH to null,
            ),
            actual = choices.associate { choice -> choice.errorCorrection to choice.blocker },
        )
    }

    @Test
    fun `a logo needs the automatic or the highest correction`() {
        // When
        val blockers = CodeErrorCorrection.entries.associateWith { errorCorrection ->
            logoBlocker(
                rawValue = "https://scanny.app",
                style = CodeStyle(errorCorrection = errorCorrection),
            )
        }

        // Then
        assertEquals(
            expected = mapOf(
                CodeErrorCorrection.AUTO to null,
                CodeErrorCorrection.LOW to LogoBlocker.CORRECTION_TOO_LOW,
                CodeErrorCorrection.MEDIUM to LogoBlocker.CORRECTION_TOO_LOW,
                CodeErrorCorrection.QUARTILE to LogoBlocker.CORRECTION_TOO_LOW,
                CodeErrorCorrection.HIGH to null,
            ),
            actual = blockers,
        )
    }

    @Test
    fun `a content too long for the highest correction leaves no room for a logo`() {
        // When
        val blocker = logoBlocker(
            rawValue = LONG_CONTENT,
            style = CodeStyle(),
        )

        // Then
        assertEquals(
            expected = LogoBlocker.CONTENT_TOO_LONG,
            actual = blocker,
        )
    }

    @Test
    fun `a logo already there is changed or removed whatever the rest`() {
        // When
        val blocker = logoBlocker(
            rawValue = LONG_CONTENT,
            style = CodeStyle(
                logo = byteArrayOf(1, 2, 3),
                errorCorrection = CodeErrorCorrection.LOW,
            ),
        )

        // Then
        assertNull(blocker)
    }

    private companion object {
        val LONG_CONTENT = "a".repeat(2000)
    }
}
