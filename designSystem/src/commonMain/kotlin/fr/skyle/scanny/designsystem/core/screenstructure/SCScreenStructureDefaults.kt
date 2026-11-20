package fr.skyle.scanny.designsystem.core.screenstructure

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import fr.skyle.scanny.designsystem.theme.SCTheme

object SCScreenStructureDefaults {

    enum class Style {
        BACKGROUND,
        BLACK;

        val containerColor: Color
            @Composable
            get() = when (this) {
                BACKGROUND -> SCTheme.colors.background
                BLACK -> SCTheme.colors.backgroundBlack
            }

        val contentColor: Color
            @Composable
            get() = when (this) {
                BACKGROUND -> SCTheme.colors.textDark
                BLACK -> SCTheme.colors.textLight
            }
    }
}