package fr.skyle.scanny.ui.screen.scangroup.ui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import fr.skyle.scanny.designsystem.core.iconbadge.SCIconBadge
import fr.skyle.scanny.designsystem.core.iconbadge.SCIconBadgeDefaults
import fr.skyle.scanny.designsystem.core.text.SCText
import fr.skyle.scanny.designsystem.theme.SCTheme
import fr.skyle.scanny.resources.Res
import fr.skyle.scanny.resources.at_time
import fr.skyle.scanny.resources.code_count
import fr.skyle.scanny.resources.group
import fr.skyle.scanny.resources.ic_stacks
import fr.skyle.scanny.ui.core.date.component.calendarDayText
import fr.skyle.scanny.ui.core.date.model.CalendarDay
import fr.skyle.scanny.ui.core.preview.AppPreviewWithBackground
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/** How many codes a group holds, and when they were scanned. */
@Composable
fun ScanGroupHeader(
    codeCount: Int,
    scannedDay: CalendarDay,
    scannedTime: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SCIconBadge(
            modifier = Modifier.wrapContentSize(),
            icon = painterResource(Res.drawable.ic_stacks),
            contentDescription = stringResource(Res.string.group),
            style = SCIconBadgeDefaults.Style.PRIMARY,
            size = SCIconBadgeDefaults.Size.LARGE,
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            SCText(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(Res.string.code_count, codeCount),
                style = SCTheme.typography.h3,
                color = SCTheme.colors.textDark,
            )

            Spacer(modifier = Modifier.height(2.dp))

            SCText(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(Res.string.at_time, calendarDayText(day = scannedDay), scannedTime),
                style = SCTheme.typography.subtitle2,
                color = SCTheme.colors.text,
            )
        }
    }
}

@AppPreviewWithBackground
@Composable
private fun PreviewScanGroupHeader() {
    SCTheme {
        ScanGroupHeader(
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = 16.dp),
            codeCount = 3,
            scannedDay = CalendarDay.Today,
            scannedTime = "21:48",
        )
    }
}