package fr.skyle.scanny.ui.screen.history.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import fr.skyle.scanny.designsystem.core.tag.SCTag
import fr.skyle.scanny.designsystem.core.tag.SCTagDefaults
import fr.skyle.scanny.designsystem.core.text.SCText
import fr.skyle.scanny.designsystem.theme.SCTheme
import fr.skyle.scanny.domain.model.BarcodeFormat
import fr.skyle.scanny.resources.Res
import fr.skyle.scanny.resources.group
import fr.skyle.scanny.resources.group_of_n_codes
import fr.skyle.scanny.resources.ic_stacks
import fr.skyle.scanny.resources.more_count
import fr.skyle.scanny.ui.core.code.mapper.toIcon
import fr.skyle.scanny.ui.core.code.mapper.toText
import fr.skyle.scanny.ui.core.code.model.CodeType
import fr.skyle.scanny.ui.core.preview.AppPreviewWithBackground
import fr.skyle.scanny.ui.screen.history.ui.model.ScanHistoryItem
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

// The types of a group shown in pills, the others counted in one last pill
private const val MAX_GROUP_TYPES = 3

/**
 * A capture of the history and its time: a single code by what it holds, its type drawn and its
 * format in a pill, a group by its number of codes and the types of its codes, each once. A group stands out among the
 * single codes, filled.
 */
@Composable
fun ScanHistoryRow(
    item: ScanHistoryItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // A group is drawn as such, not as its first code
    val isGroup = item.codeCount > 1

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
            style = if (isGroup) SCIconBadgeDefaults.Style.FILLED else SCIconBadgeDefaults.Style.PRIMARY,
            size = SCIconBadgeDefaults.Size.LARGE,
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            SCText(
                modifier = Modifier.fillMaxWidth(),
                text = if (isGroup) stringResource(Res.string.group_of_n_codes, item.codeCount) else item.summary,
                style = SCTheme.typography.body1,
                color = SCTheme.colors.textDark,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Row(
                    modifier = Modifier.weight(weight = 1f, fill = false),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    // A single code shows its format, its type already drawn; one this version does not
                    // know, its type
                    val texts = if (isGroup) {
                        item.codeTypes.map { type -> stringResource(type.toText()) }
                    } else {
                        listOf(stringResource(item.format?.toText() ?: item.type.toText()))
                    }
                    texts.take(MAX_GROUP_TYPES).forEach { text ->
                        SCTag(
                            text = text,
                            style = SCTagDefaults.Style.LIGHT,
                            size = SCTagDefaults.Size.MEDIUM,
                        )
                    }

                    if (texts.size > MAX_GROUP_TYPES) {
                        SCTag(
                            text = stringResource(Res.string.more_count, texts.size - MAX_GROUP_TYPES),
                            style = SCTagDefaults.Style.LIGHT,
                            size = SCTagDefaults.Size.MEDIUM,
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                SCText(
                    text = item.time,
                    style = SCTheme.typography.subtitle2,
                    color = SCTheme.colors.text,
                    maxLines = 1,
                )
            }
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
                format = BarcodeFormat.QR_CODE,
                summary = "https://scanny.app/a/link/long/enough/to/be/cut/at/the/end/of/the/row",
                time = "21:48",
                codeCount = 1,
                codeTypes = listOf(CodeType.LINK),
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
                format = BarcodeFormat.QR_CODE,
                summary = "4006381333931",
                time = "09:05",
                codeCount = 6,
                codeTypes = listOf(CodeType.TEXT, CodeType.WIFI, CodeType.LINK, CodeType.CONTACT, CodeType.SMS),
            ),
            onClick = {},
        )
    }
}
