package fr.skyle.scanny.designsystem.core.iconbadge

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import fr.skyle.scanny.designsystem.theme.SCTheme

object SCIconBadgeDefaults {

    enum class Style {
        PRIMARY,
        // Stands out among PRIMARY ones, a group among single codes for instance
        FILLED;

        val containerColor: Color
            @Composable
            get() = when (this) {
                PRIMARY -> SCTheme.colors.backgroundIcon
                FILLED -> SCTheme.colors.primary
            }

        val contentColor: Color
            @Composable
            get() = when (this) {
                PRIMARY -> SCTheme.colors.primary
                FILLED -> SCTheme.colors.textLight
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