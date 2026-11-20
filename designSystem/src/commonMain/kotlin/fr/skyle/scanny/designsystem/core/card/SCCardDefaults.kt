package fr.skyle.scanny.designsystem.core.card

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import fr.skyle.scanny.designsystem.theme.SCTheme

object SCCardDefaults {

    enum class Style {
        LIGHT;

        val containerColor: Color
            @Composable
            get() = when (this) {
                LIGHT -> SCTheme.colors.backgroundLight
            }

        val contentColor: Color
            @Composable
            get() = when (this) {
                LIGHT -> SCTheme.colors.textDark
            }
    }
}