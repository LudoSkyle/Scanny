package fr.skyle.scanny.ui.screen.history.ui.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import fr.skyle.scanny.designsystem.core.text.SCText
import fr.skyle.scanny.designsystem.theme.SCTheme
import fr.skyle.scanny.ui.core.date.component.calendarDayText
import fr.skyle.scanny.ui.core.date.model.CalendarDay
import fr.skyle.scanny.ui.core.preview.AppPreviewWithBackground
import kotlinx.datetime.LocalDate

/** The day of the captures that follow it in the history. */
@Composable
fun HistoryDayHeader(
    day: CalendarDay,
    modifier: Modifier = Modifier,
) {
    SCText(
        modifier = modifier
            .semantics { heading() }
            .padding(
                start = 20.dp,
                top = 24.dp,
                end = 20.dp,
                bottom = 4.dp,
            ),
        text = calendarDayText(day = day),
        style = SCTheme.typography.subtitle1,
        color = SCTheme.colors.text,
    )
}

@AppPreviewWithBackground
@Composable
private fun PreviewHistoryDayHeader() {
    SCTheme {
        HistoryDayHeader(
            modifier = Modifier.fillMaxWidth(),
            day = CalendarDay.Date(
                date = LocalDate(
                    year = 2026,
                    month = 11,
                    day = 18,
                ),
            ),
        )
    }
}