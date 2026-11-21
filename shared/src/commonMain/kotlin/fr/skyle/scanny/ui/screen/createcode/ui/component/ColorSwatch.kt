package fr.skyle.scanny.ui.screen.createcode.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import fr.skyle.scanny.designsystem.theme.SCTheme
import fr.skyle.scanny.ui.core.preview.AppPreviewWithBackground

/**
 * A color to choose, as a disc. The chosen one is circled, whatever its color: a ring tells it, not a
 * shade.
 */
@Composable
fun ColorSwatch(
    color: Color,
    name: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(48.dp)
            .clip(CircleShape)
            .selectable(
                selected = isSelected,
                role = Role.RadioButton,
                onClick = onClick,
            )
            .semantics { contentDescription = name }
            .border(
                width = 2.dp,
                color = if (isSelected) SCTheme.colors.primary else Color.Transparent,
                shape = CircleShape,
            )
            .padding(all = 6.dp)
            // Outlined, for a white disc to show on a light screen
            .border(
                width = 1.dp,
                color = SCTheme.colors.backgroundDisabled,
                shape = CircleShape,
            )
            .background(
                color = color,
                shape = CircleShape,
            ),
    )
}

@AppPreviewWithBackground
@Composable
private fun PreviewColorSwatch() {
    SCTheme {
        Row {
            ColorSwatch(
                color = Color(0xFF3B43BE),
                name = "Indigo",
                isSelected = true,
                onClick = {},
            )
            ColorSwatch(
                color = Color.White,
                name = "White",
                isSelected = false,
                onClick = {},
            )
        }
    }
}
