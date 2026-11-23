package fr.skyle.scanny.designsystem.core.divider

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import fr.skyle.scanny.designsystem.theme.SCTheme

object SCDividerDefaults {

    enum class Style {
        // Between the rows of a card, the color of the screen behind it
        LIGHT;

        val color: Color
            @Composable
            get() = when (this) {
                LIGHT -> SCTheme.colors.background
            }
    }
}