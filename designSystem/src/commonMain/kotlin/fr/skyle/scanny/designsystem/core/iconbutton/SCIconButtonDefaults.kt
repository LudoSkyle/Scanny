package fr.skyle.scanny.designsystem.core.iconbutton

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import fr.skyle.scanny.designsystem.theme.SCTheme

object SCIconButtonDefaults {

    enum class Style {
        DARK,
        LIGHT;

        val contentColor: Color
            @Composable
            get() = when (this) {
                DARK -> SCTheme.colors.textDark
                LIGHT -> SCTheme.colors.textLight
            }
    }

    enum class Size {
        MEDIUM;

        val iconSize: Dp
            get() = when (this) {
                MEDIUM -> 24.dp
            }
    }
}