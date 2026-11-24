package fr.skyle.scanny.designsystem.core.segmentedcontrol

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import fr.skyle.scanny.designsystem.core.text.SCText
import fr.skyle.scanny.designsystem.theme.SCTheme
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * A choice among a few [options], side by side in a pill, each as wide as the others: the chosen one
 * filled, the filling sliding from one to the other. Each option reads as a tab.
 *
 * [selectionPosition] follows a gesture that moves between the options, a pager swiped: the index
 * of the option the filling is on, between two while it moves. Without it, the filling slides to
 * the option chosen.
 */
@Composable
fun <T> SCSegmentedControl(
    options: List<T>,
    selectedOption: T,
    optionText: @Composable (T) -> String,
    onOptionSelected: (T) -> Unit,
    style: SCSegmentedControlDefaults.Style,
    size: SCSegmentedControlDefaults.Size,
    modifier: Modifier = Modifier,
    selectionPosition: (() -> Float)? = null,
) {
    val animatedPosition by animateFloatAsState(options.indexOf(selectedOption).toFloat())
    val position = selectionPosition ?: { animatedPosition }

    BoxWithConstraints(
        modifier = modifier
            .height(size.height)
            .clip(CircleShape)
            .background(color = style.containerColor)
            .padding(all = size.padding)
            .selectableGroup(),
    ) {
        val optionWidth = maxWidth / options.size

        // Under the texts, read at the drawing only: a swipe moves it without composing again
        Box(
            modifier = Modifier
                .offset { IntOffset(x = (optionWidth.toPx() * position()).roundToInt(), y = 0) }
                .width(optionWidth)
                .fillMaxHeight()
                .clip(CircleShape)
                .background(color = style.selectedColor),
        )

        Row(modifier = Modifier.fillMaxSize()) {
            options.forEachIndexed { index, option ->
                // Light over the filling, dark beside it, in between while it moves
                val filling = (1f - abs(position() - index)).coerceIn(0f, 1f)

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(CircleShape)
                        .selectable(
                            selected = option == selectedOption,
                            role = Role.Tab,
                            onClick = { onOptionSelected(option) },
                        )
                        .padding(horizontal = 12.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    SCText(
                        text = optionText(option),
                        style = size.textStyle,
                        color = lerp(start = style.textColor, stop = style.selectedTextColor, fraction = filling),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

private class SCSegmentedControlVariantProvider :
    PreviewParameterProvider<Pair<SCSegmentedControlDefaults.Style, SCSegmentedControlDefaults.Size>> {
    override val values =
        SCSegmentedControlDefaults.Style.entries.flatMap { style ->
            SCSegmentedControlDefaults.Size.entries.map { size ->
                style to size
            }
        }.asSequence()
}

@Preview(showBackground = true)
@Composable
private fun PreviewSCSegmentedControl(
    @PreviewParameter(SCSegmentedControlVariantProvider::class)
    variant: Pair<SCSegmentedControlDefaults.Style, SCSegmentedControlDefaults.Size>,
) {
    SCTheme {
        SCSegmentedControl(
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = 16.dp),
            options = listOf("Scans", "Creations"),
            selectedOption = "Scans",
            optionText = { option -> option },
            onOptionSelected = {},
            style = variant.first,
            size = variant.second,
        )
    }
}
