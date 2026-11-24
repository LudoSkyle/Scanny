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
import fr.skyle.scanny.designsystem.core.tag.SCTag
import fr.skyle.scanny.designsystem.core.tag.SCTagDefaults
import fr.skyle.scanny.designsystem.core.text.SCText
import fr.skyle.scanny.designsystem.theme.SCTheme
import fr.skyle.scanny.domain.model.BarcodeFormat
import fr.skyle.scanny.ui.core.code.mapper.toIcon
import fr.skyle.scanny.ui.core.code.mapper.toText
import fr.skyle.scanny.ui.core.code.model.CodeType
import fr.skyle.scanny.ui.core.preview.AppPreviewWithBackground
import fr.skyle.scanny.ui.screen.history.ui.model.CreationHistoryItem
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/** A code created: what it holds, its type drawn, its format in a pill, and its time. */
@Composable
fun CreationHistoryRow(
    item: CreationHistoryItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
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
            icon = painterResource(item.type.toIcon()),
            contentDescription = stringResource(item.type.toText()),
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

            Spacer(modifier = Modifier.height(4.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                SCTag(
                    modifier = Modifier.weight(weight = 1f, fill = false),
                    // Its format, its type already drawn; one this version does not know, its type
                    text = stringResource(item.format?.toText() ?: item.type.toText()),
                    style = SCTagDefaults.Style.LIGHT,
                    size = SCTagDefaults.Size.MEDIUM,
                )

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
private fun PreviewCreationHistoryRow() {
    SCTheme {
        CreationHistoryRow(
            modifier = Modifier.fillMaxWidth(),
            item = CreationHistoryItem(
                creationId = 1,
                type = CodeType.WIFI,
                format = BarcodeFormat.QR_CODE,
                summary = "Scanny",
                time = "21:48",
            ),
            onClick = {},
        )
    }
}
