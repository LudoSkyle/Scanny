package fr.skyle.scanny.designsystem.core.iconbadge

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import fr.skyle.scanny.designsystem.theme.SCTheme

object SCIconBadgeDefaults {

    enum class Style {
        PRIMARY;

        val containerColor: Color
            @Composable
            get() = when (this) {
                PRIMARY -> SCTheme.colors.backgroundIcon
            }

        val contentColor: Color
            @Composable
            get() = when (this) {
                PRIMARY -> SCTheme.colors.primary
            }
    }

    enum class Size {
        MEDIUM,
        LARGE;

        val badgeSize: Dp
            get() = when (this) {
                MEDIUM -> 40.dp
                LARGE -> 48.dp
            }

        val iconSize: Dp
            get() = when (this) {
                MEDIUM -> 20.dp
                LARGE -> 24.dp
            }
    }
}