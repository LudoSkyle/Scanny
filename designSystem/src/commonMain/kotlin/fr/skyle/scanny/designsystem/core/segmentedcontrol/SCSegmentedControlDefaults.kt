package fr.skyle.scanny.designsystem.core.segmentedcontrol

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import fr.skyle.scanny.designsystem.theme.SCTheme

object SCSegmentedControlDefaults {

    enum class Style {
        PRIMARY;

        val containerColor: Color
            @Composable
            get() = when (this) {
                PRIMARY -> SCTheme.colors.backgroundIcon
            }

        val selectedColor: Color
            @Composable
            get() = when (this) {
                PRIMARY -> SCTheme.colors.primary
            }

        val selectedTextColor: Color
            @Composable
            get() = when (this) {
                PRIMARY -> SCTheme.colors.textLight
            }

        val textColor: Color
            @Composable
            get() = when (this) {
                PRIMARY -> SCTheme.colors.textPrimary
            }
    }

    enum class Size {
        MEDIUM;

        val height: Dp
            get() = when (this) {
                MEDIUM -> 44.dp
            }

        // Between the pill and the option chosen
        val padding: Dp
            get() = when (this) {
                MEDIUM -> 4.dp
            }

        val textStyle: TextStyle
            @Composable
            get() = when (this) {
                MEDIUM -> SCTheme.typography.button
            }
    }
}
