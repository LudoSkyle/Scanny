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
import fr.skyle.scanny.domain.model.BarcodeFormat
import fr.skyle.scanny.ui.core.code.model.CodeType
import fr.skyle.scanny.ui.core.date.model.CalendarDay
import fr.skyle.scanny.ui.core.preview.AppPreviewWithBackground
import fr.skyle.scanny.ui.screen.history.ui.model.HistoryHeader
import fr.skyle.scanny.ui.screen.history.ui.model.ScanHistoryItem
import fr.skyle.scanny.ui.screen.history.ui.model.ScanHistorySection

/** The favorite captures, then the others by day, from the newest, or what shows before the first scan. */
@Composable
fun ScanHistoryList(
    sections: List<ScanHistorySection>?,
    contentPadding: PaddingValues,
    onScanClicked: (ScanHistoryItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    when {
        // Read in a moment: an empty screen rather than a message that would flash
        sections == null -> Unit

        sections.isEmpty() -> ScanHistoryEmpty(
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
                    key = section.header.toString(),
                    contentType = "header",
                ) {
                    HistorySectionHeader(
                        modifier = Modifier
                            .fillMaxWidth()
                            .animateItem(),
                        header = section.header,
                    )
                }

                items(
                    items = section.items,
                    key = { item -> item.scanId },
                    contentType = { "scan" },
                ) { item ->
                    // Moved between the favorites and its day without a blink
                    ScanHistoryRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .animateItem(),
                        item = item,
                        onClick = { onScanClicked(item) },
                    )
                }
            }
        }
    }
}

@AppPreviewWithBackground
@Composable
private fun PreviewScanHistoryList() {
    SCTheme {
        ScanHistoryList(
            modifier = Modifier.fillMaxSize(),
            sections = listOf(
                ScanHistorySection(
                    header = HistoryHeader.Day(day = CalendarDay.Today),
                    items = listOf(
                        ScanHistoryItem(
                            scanId = 1,
                            firstCodeId = 1,
                            type = CodeType.LINK,
                            format = BarcodeFormat.QR_CODE,
                            summary = "https://scanny.app",
                            time = "21:48",
                            codeCount = 1,
                            codeTypes = listOf(CodeType.LINK),
                        ),
                    ),
                ),
            ),
            contentPadding = PaddingValues(),
            onScanClicked = {},
        )
    }
}
