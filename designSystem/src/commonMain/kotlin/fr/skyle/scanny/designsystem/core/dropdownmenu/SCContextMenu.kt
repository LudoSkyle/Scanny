package fr.skyle.scanny.designsystem.core.dropdownmenu

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import fr.skyle.scanny.designsystem.core.text.SCText
import fr.skyle.scanny.designsystem.theme.SCTheme

// Close to the edges of the window, still clear of them
private val WINDOW_MARGIN = 8.dp

/**
 * A menu of [options] that unfolds from the point it is placed at, a finger holding an element:
 * under it if it has the room, else over it, ending there. Unlike [SCDropdownMenu], which opens over
 * an element of the lower half of the screen and keeps far from its edges, it stays at the finger. It
 * closes once an option is chosen.
 */
@Composable
fun <T> SCContextMenu(
    isExpanded: Boolean,
    onDismissRequest: () -> Unit,
    options: List<T>,
    optionText: @Composable (T) -> String,
    onOptionSelected: (T) -> Unit,
    style: SCDropdownMenuDefaults.Style,
    modifier: Modifier = Modifier,
) {
    if (!isExpanded) {
        return
    }

    val margin = with(LocalDensity.current) { WINDOW_MARGIN.roundToPx() }
    val positionProvider = remember(margin) { AtPointPositionProvider(margin = margin) }

    Popup(
        popupPositionProvider = positionProvider,
        onDismissRequest = onDismissRequest,
        properties = PopupProperties(focusable = true),
    ) {
        Surface(
            modifier = modifier,
            shape = SCTheme.shapes.medium,
            color = style.containerColor,
            shadowElevation = 3.dp,
        ) {
            Column(
                modifier = Modifier
                    .width(IntrinsicSize.Max)
                    .padding(vertical = 8.dp),
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        modifier = Modifier.fillMaxWidth(),
                        text = {
                            SCText(
                                modifier = Modifier.wrapContentSize(),
                                text = optionText(option),
                                style = SCTheme.typography.body1,
                                color = style.textColor,
                            )
                        },
                        onClick = {
                            onDismissRequest()
                            onOptionSelected(option)
                        },
                    )
                }
            }
        }
    }
}

// The menu under the point it hangs from, or over it, ending there, kept within the window
private class AtPointPositionProvider(
    private val margin: Int,
) : PopupPositionProvider {

    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize,
    ): IntOffset {
        val maxX = (windowSize.width - popupContentSize.width - margin).coerceAtLeast(margin)
        val x = anchorBounds.left.coerceIn(margin, maxX)

        val under = anchorBounds.top
        val over = anchorBounds.top - popupContentSize.height
        val y = when {
            under + popupContentSize.height <= windowSize.height - margin -> under
            over >= margin -> over
            else -> (windowSize.height - popupContentSize.height - margin).coerceAtLeast(margin)
        }
        return IntOffset(x = x, y = y)
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewSCContextMenu() {
    SCTheme {
        Box(modifier = Modifier.padding(all = 16.dp)) {
            SCContextMenu(
                isExpanded = true,
                onDismissRequest = {},
                options = listOf(
                    "Add to favorites",
                    "Delete",
                ),
                optionText = { option -> option },
                onOptionSelected = {},
                style = SCDropdownMenuDefaults.Style.LIGHT,
            )
        }
    }
}
