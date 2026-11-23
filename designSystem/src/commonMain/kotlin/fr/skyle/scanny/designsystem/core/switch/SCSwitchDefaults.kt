package fr.skyle.scanny.designsystem.core.switch

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import fr.skyle.scanny.designsystem.theme.SCTheme

object SCSwitchDefaults {

    enum class Style {
        PRIMARY;

        val checkedThumbColor: Color
            @Composable
            get() = when (this) {
                PRIMARY -> SCTheme.colors.textLight
            }

        val checkedTrackColor: Color
            @Composable
            get() = when (this) {
                PRIMARY -> SCTheme.colors.primary
            }

        val uncheckedThumbColor: Color
            @Composable
            get() = when (this) {
                PRIMARY -> SCTheme.colors.text
            }

        val uncheckedTrackColor: Color
            @Composable
            get() = when (this) {
                PRIMARY -> SCTheme.colors.backgroundIcon
            }
    }
}