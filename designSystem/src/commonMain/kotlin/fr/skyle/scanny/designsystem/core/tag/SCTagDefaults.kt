package fr.skyle.scanny.designsystem.core.tag

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import fr.skyle.scanny.designsystem.theme.SCTheme

object SCTagDefaults {

    enum class Style {
        LIGHT,
        // Stands out among LIGHT ones
        FILLED;

        val containerColor: Color
            @Composable
            get() = when (this) {
                LIGHT -> SCTheme.colors.backgroundIcon
                FILLED -> SCTheme.colors.primary
            }

        val contentColor: Color
            @Composable
            get() = when (this) {
                LIGHT -> SCTheme.colors.textPrimary
                FILLED -> SCTheme.colors.textLight
            }
    }

    enum class Size {
        MEDIUM;

        val horizontalPadding: Dp
            get() = when (this) {
                MEDIUM -> 10.dp
            }

        val verticalPadding: Dp
            get() = when (this) {
                MEDIUM -> 3.dp
            }

        val textStyle: TextStyle
            @Composable
            get() = when (this) {
                MEDIUM -> SCTheme.typography.subtitle2
            }
    }
}
