package fr.skyle.scanny.designsystem.core.dropdownmenu

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import fr.skyle.scanny.designsystem.theme.SCTheme

object SCDropdownMenuDefaults {

    enum class Style {
        LIGHT;

        val containerColor: Color
            @Composable
            get() = when (this) {
                LIGHT -> SCTheme.colors.backgroundLight
            }

        val textColor: Color
            @Composable
            get() = when (this) {
                LIGHT -> SCTheme.colors.textDark
            }

        // The option chosen so far
        val selectedTextColor: Color
            @Composable
            get() = when (this) {
                LIGHT -> SCTheme.colors.primary
            }
    }
}