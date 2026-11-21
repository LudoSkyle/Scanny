package fr.skyle.scanny.ui.screen.history.ui.component

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import fr.skyle.scanny.designsystem.theme.SCTheme
import fr.skyle.scanny.ui.core.code.model.CodeType
import fr.skyle.scanny.ui.core.date.model.CalendarDay
import fr.skyle.scanny.ui.core.preview.AppPreviewWithBackground
import fr.skyle.scanny.ui.screen.history.ui.model.CreationHistoryItem
import fr.skyle.scanny.ui.screen.history.ui.model.CreationHistorySection

/** The codes created by day, from the newest, or what shows before the first one. */
@Composable
fun CreationHistoryList(
    sections: List<CreationHistorySection>?,
    contentPadding: PaddingValues,
    onCreationClicked: (CreationHistoryItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    when {
        // Read in a moment: an empty screen rather than a message that would flash
        sections == null -> Unit

        sections.isEmpty() -> CreationHistoryEmpty(
            modifier = modifier
                .padding(contentPadding)
                .wrapContentSize()
                .padding(horizontal = 32.dp),
        )

        else -> LazyColumn(
            modifier = modifier,
            contentPadding = contentPadding,
        ) {
            sections.forEach { section ->
                item(
                    key = section.day.toString(),
                    contentType = "day",
                ) {
                    HistoryDayHeader(
                        modifier = Modifier.fillMaxWidth(),
                        day = section.day,
                    )
                }

                items(
                    items = section.items,
                    key = { item -> item.creationId },
                    contentType = { "creation" },
                ) { item ->
                    CreationHistoryRow(
                        modifier = Modifier.fillMaxWidth(),
                        item = item,
                        onClick = { onCreationClicked(item) },
                    )
                }
            }
        }
    }
}

@AppPreviewWithBackground
@Composable
private fun PreviewCreationHistoryList() {
    SCTheme {
        CreationHistoryList(
            modifier = Modifier.fillMaxSize(),
            sections = listOf(
                CreationHistorySection(
                    day = CalendarDay.Today,
                    items = listOf(
                        CreationHistoryItem(
                            creationId = 1,
                            type = CodeType.WIFI,
                            summary = "Scanny",
                            time = "21:48",
                        ),
                    ),
                ),
            ),
            contentPadding = PaddingValues(),
            onCreationClicked = {},
        )
    }
}
