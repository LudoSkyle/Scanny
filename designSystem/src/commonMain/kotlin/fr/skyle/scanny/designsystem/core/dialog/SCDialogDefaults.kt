package fr.skyle.scanny.designsystem.core.dialog

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import fr.skyle.scanny.designsystem.theme.SCTheme

object SCDialogDefaults {

    enum class Style {
        // Confirms an action that cannot be undone, such as a deletion
        DESTRUCTIVE;

        val confirmColor: Color
            @Composable
            get() = when (this) {
                DESTRUCTIVE -> SCTheme.colors.error
            }
    }
}