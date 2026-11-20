package fr.skyle.scanny.designsystem.core.textfield

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import fr.skyle.scanny.designsystem.theme.SCTheme

object SCTextFieldDefaults {

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

        val labelColor: Color
            @Composable
            get() = when (this) {
                LIGHT -> SCTheme.colors.text
            }

        // The border and the label of the field being written, and its cursor
        val focusedColor: Color
            @Composable
            get() = when (this) {
                LIGHT -> SCTheme.colors.primary
            }

        val unfocusedBorderColor: Color
            @Composable
            get() = when (this) {
                LIGHT -> SCTheme.colors.backgroundDisabled
            }
    }
}