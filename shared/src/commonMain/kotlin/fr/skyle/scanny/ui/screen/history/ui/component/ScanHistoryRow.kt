package fr.skyle.scanny.ui.screen.history.ui.component

import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import fr.skyle.scanny.designsystem.core.iconbadge.SCIconBadge
import fr.skyle.scanny.designsystem.core.iconbadge.SCIconBadgeDefaults
import fr.skyle.scanny.designsystem.core.text.SCText
import fr.skyle.scanny.designsystem.theme.SCTheme
import fr.skyle.scanny.resources.Res
import fr.skyle.scanny.resources.code_count
import fr.skyle.scanny.resources.description_and_time
import fr.skyle.scanny.resources.group
import fr.skyle.scanny.resources.ic_stacks
import fr.skyle.scanny.ui.core.code.mapper.toIcon
import fr.skyle.scanny.ui.core.code.mapper.toText
import fr.skyle.scanny.ui.core.code.model.CodeType
import fr.skyle.scanny.ui.core.preview.AppPreviewWithBackground
import fr.skyle.scanny.ui.screen.history.ui.model.ScanHistoryItem
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/** A capture of the history: what it holds, its type or its number of codes, and its time. */
@Composable
fun ScanHistoryRow(
    item: ScanHistoryItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // A group is drawn as such, not as its first code
    val isGroup = item.codeCount > 1
    val description = if (isGroup) {
        stringResource(Res.string.code_count, item.codeCount)
    } else {
        stringResource(item.type.toText())
    }

    Row(
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(
                horizontal = 20.dp,
                vertical = 12.dp,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SCIconBadge(
            modifier = Modifier.wrapContentSize(),
            icon = painterResource(if (isGroup) Res.drawable.ic_stacks else item.type.toIcon()),
            contentDescription = stringResource(if (isGroup) Res.string.group else item.type.toText()),
            style = SCIconBadgeDefaults.Style.PRIMARY,
            size = SCIconBadgeDefaults.Size.LARGE,
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            SCText(
                modifier = Modifier.fillMaxWidth(),
                text = item.summary,
                style = SCTheme.typography.body1,
                color = SCTheme.colors.textDark,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            Spacer(modifier = Modifier.height(2.dp))

            SCText(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(Res.string.description_and_time, description, item.time),
                style = SCTheme.typography.subtitle2,
                color = SCTheme.colors.text,
                maxLines = 1,
            )
        }
    }
}

@AppPreviewWithBackground
@Composable
private fun PreviewScanHistoryRow() {
    SCTheme {
        ScanHistoryRow(
            modifier = Modifier.fillMaxWidth(),
            item = ScanHistoryItem(
                scanId = 1,
                firstCodeId = 1,
                type = CodeType.LINK,
                summary = "https://scanny.app/a/link/long/enough/to/be/cut/at/the/end/of/the/row",
                time = "21:48",
                codeCount = 1,
            ),
            onClick = {},
        )
    }
}

@AppPreviewWithBackground
@Composable
private fun PreviewScanHistoryRow_Group() {
    SCTheme {
        ScanHistoryRow(
            modifier = Modifier.fillMaxWidth(),
            item = ScanHistoryItem(
                scanId = 2,
                firstCodeId = 2,
                type = CodeType.TEXT,
                summary = "4006381333931",
                time = "09:05",
                codeCount = 3,
            ),
            onClick = {},
        )
    }
}