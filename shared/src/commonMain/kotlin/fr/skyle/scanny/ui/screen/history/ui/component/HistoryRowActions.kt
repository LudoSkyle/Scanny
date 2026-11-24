package fr.skyle.scanny.ui.screen.history.ui.component

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.DpOffset
import fr.skyle.scanny.designsystem.core.dropdownmenu.SCDropdownMenu
import fr.skyle.scanny.designsystem.core.dropdownmenu.SCDropdownMenuDefaults
import fr.skyle.scanny.resources.Res
import fr.skyle.scanny.resources.add_to_favorites
import fr.skyle.scanny.resources.delete
import fr.skyle.scanny.resources.more_actions
import fr.skyle.scanny.resources.remove_from_favorites
import org.jetbrains.compose.resources.stringResource

private enum class HistoryRowAction {
    FAVORITE,
    DELETE,
}

/**
 * A row of the history, touched to open it, held to show its actions where the finger holds it: put
 * it among the favorites or take it out, delete it. A hold rather than a swipe: a swipe goes from a view of the
 * history to the other.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HistoryRowActions(
    isFavorite: Boolean,
    onClick: () -> Unit,
    onFavoriteClicked: () -> Unit,
    onDeleteClicked: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    var isMenuExpanded by rememberSaveable { mutableStateOf(false) }
    val hapticFeedback = LocalHapticFeedback.current
    val density = LocalDensity.current
    // Where the finger last touched the row, and its height: the menu unfolds under the row, moved up
    // to the finger
    var touchPosition by remember { mutableStateOf(Offset.Zero) }
    var rowHeight by remember { mutableIntStateOf(0) }

    Box(
        modifier = modifier
            .onSizeChanged { size -> rowHeight = size.height }
            // Seen, not taken: the touch stays for the clicks
            .pointerInput(Unit) {
                awaitEachGesture {
                    touchPosition = awaitFirstDown(requireUnconsumed = false).position
                }
            }
            .combinedClickable(
                onClick = onClick,
                onLongClickLabel = stringResource(Res.string.more_actions),
                onLongClick = {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    isMenuExpanded = true
                },
            ),
    ) {
        content()

        SCDropdownMenu(
            isExpanded = isMenuExpanded,
            onDismissRequest = { isMenuExpanded = false },
            options = HistoryRowAction.entries,
            selectedOption = null,
            optionText = { action ->
                stringResource(
                    when (action) {
                        HistoryRowAction.FAVORITE -> if (isFavorite) Res.string.remove_from_favorites else Res.string.add_to_favorites
                        HistoryRowAction.DELETE -> Res.string.delete
                    },
                )
            },
            onOptionSelected = { action ->
                when (action) {
                    HistoryRowAction.FAVORITE -> onFavoriteClicked()
                    HistoryRowAction.DELETE -> onDeleteClicked()
                }
            },
            style = SCDropdownMenuDefaults.Style.LIGHT,
            offset = with(density) {
                DpOffset(
                    x = touchPosition.x.toDp(),
                    y = (touchPosition.y - rowHeight).toDp(),
                )
            },
        )
    }
}
