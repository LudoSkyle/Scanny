package fr.skyle.scanny.designsystem.core.topappbar

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import fr.skyle.scanny.designsystem.core.iconbutton.SCIconButtonDefaults
import fr.skyle.scanny.designsystem.theme.SCTheme

object SCTopAppBarDefaults {

    enum class Style {
        BACKGROUND,
        TRANSPARENT;

        val containerColor: Color
            @Composable
            get() = when (this) {
                BACKGROUND -> SCTheme.colors.background
                TRANSPARENT -> SCTheme.colors.transparent
            }

        val contentColor: Color
            @Composable
            get() = when (this) {
                BACKGROUND -> SCTheme.colors.textDark
                TRANSPARENT -> SCTheme.colors.textLight
            }

        val subtitleColor: Color
            @Composable
            get() = when (this) {
                BACKGROUND -> SCTheme.colors.text
                TRANSPARENT -> SCTheme.colors.textLight
            }

        internal val iconButtonStyle: SCIconButtonDefaults.Style
            get() = when (this) {
                BACKGROUND -> SCIconButtonDefaults.Style.DARK
                TRANSPARENT -> SCIconButtonDefaults.Style.LIGHT
            }
    }
}