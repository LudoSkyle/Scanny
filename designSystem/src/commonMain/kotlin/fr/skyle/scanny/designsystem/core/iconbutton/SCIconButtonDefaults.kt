package fr.skyle.scanny.designsystem.core.iconbutton

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import fr.skyle.scanny.designsystem.theme.SCTheme

object SCIconButtonDefaults {

    enum class Style {
        DARK,
        LIGHT,
        OVERLAY,
        // An overlay button switched on, such as a mode in use
        PRIMARY;

        val containerColor: Color
            @Composable
            get() = when (this) {
                DARK, LIGHT -> SCTheme.colors.transparent
                // Readable over any picture, such as the preview of the camera
                OVERLAY -> SCTheme.colors.backgroundBlack.copy(alpha = 0.45f)
                PRIMARY -> SCTheme.colors.primary
            }

        val contentColor: Color
            @Composable
            get() = when (this) {
                DARK -> SCTheme.colors.textDark
                LIGHT, OVERLAY, PRIMARY -> SCTheme.colors.textLight
            }
    }

    enum class Size {
        MEDIUM,
        LARGE;

        val buttonSize: Dp
            get() = when (this) {
                MEDIUM -> 48.dp
                LARGE -> 56.dp
            }

        val iconSize: Dp
            get() = when (this) {
                MEDIUM -> 24.dp
                LARGE -> 28.dp
            }
    }
}