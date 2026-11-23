package fr.skyle.scanny.designsystem.core.button

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import fr.skyle.scanny.designsystem.theme.SCTheme

object SCButtonDefaults {

    enum class Style {
        PRIMARY,
        SECONDARY;

        val containerColor: Color
            @Composable
            get() = when (this) {
                PRIMARY -> SCTheme.colors.primary
                SECONDARY -> SCTheme.colors.backgroundIcon
            }

        val contentColor: Color
            @Composable
            get() = when (this) {
                PRIMARY -> SCTheme.colors.textLight
                SECONDARY -> SCTheme.colors.textPrimary
            }
    }

    enum class Size {
        MEDIUM;

        val height: Dp
            get() = when (this) {
                MEDIUM -> 48.dp
            }
    }
}