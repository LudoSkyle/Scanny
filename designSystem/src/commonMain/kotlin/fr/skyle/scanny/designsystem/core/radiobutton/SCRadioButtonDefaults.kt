package fr.skyle.scanny.designsystem.core.radiobutton

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import fr.skyle.scanny.designsystem.theme.SCTheme

object SCRadioButtonDefaults {

    enum class Style {
        PRIMARY;

        val selectedColor: Color
            @Composable
            get() = when (this) {
                PRIMARY -> SCTheme.colors.primary
            }

        val unselectedColor: Color
            @Composable
            get() = when (this) {
                PRIMARY -> SCTheme.colors.text
            }

        val disabledColor: Color
            @Composable
            get() = when (this) {
                PRIMARY -> SCTheme.colors.textDisabled
            }
    }
}
