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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import fr.skyle.scanny.designsystem.theme.SCTheme
import fr.skyle.scanny.ui.core.preview.AppPreviewWithBackground

// Every hue around the disc: any color can be picked
private val HUES = listOf(0f, 60f, 120f, 180f, 240f, 300f, 0f).map { hue -> Color.hsv(hue = hue, saturation = 1f, value = 1f) }

/**
 * The swatch that opens the choice of any color: all the hues when no other color is chosen, the color
 * chosen at their center otherwise, circled as the chosen one.
 */
@Composable
fun CustomColorSwatch(
    color: Color?,
    name: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(48.dp)
            .clip(CircleShape)
            .selectable(
                selected = color != null,
                role = Role.RadioButton,
                onClick = onClick,
            )
            .semantics { contentDescription = name }
            .border(
                width = 2.dp,
                color = if (color != null) SCTheme.colors.primary else Color.Transparent,
                shape = CircleShape,
            )
            .padding(all = 6.dp)
            .background(
                brush = Brush.sweepGradient(HUES),
                shape = CircleShape,
            )
            .padding(all = 8.dp)
            .background(
                color = color ?: Color.Transparent,
                shape = CircleShape,
            ),
    )
}

@AppPreviewWithBackground
@Composable
private fun PreviewCustomColorSwatch() {
    SCTheme {
        Row {
            CustomColorSwatch(
                color = null,
                name = "Other color",
                onClick = {},
            )
            CustomColorSwatch(
                color = Color(0xFF00897B),
                name = "Other color",
                onClick = {},
            )
        }
    }
}
