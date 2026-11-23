package fr.skyle.scanny.ui.screen.createcode.ui.model

import fr.skyle.scanny.domain.model.CodeStyle
import fr.skyle.scanny.ui.core.code.mapper.readability
import fr.skyle.scanny.ui.core.code.model.CodeReadability
import kotlin.test.Test
import kotlin.test.assertEquals

class CodeColorTest {

    @Test
    fun `every dot color reads well on every background color`() {
        // When
        val poorPairs = CodeDotColor.entries.flatMap { dotColor ->
            CodeBackgroundColor.entries
                .filter { backgroundColor ->
                    CodeStyle(
                        darkColor = dotColor.argb,
                        lightColor = backgroundColor.argb,
                    ).readability() != CodeReadability.GOOD
                }
                .map { backgroundColor -> dotColor to backgroundColor }
        }

        // Then
        assertEquals(
            expected = emptyList(),
            actual = poorPairs,
        )
    }
}
