package fr.skyle.scanny.ui.screen.settings.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.dp
import fr.skyle.scanny.designsystem.core.dropdownmenu.SCDropdownMenu
import fr.skyle.scanny.designsystem.core.dropdownmenu.SCDropdownMenuDefaults
import fr.skyle.scanny.designsystem.core.iconbadge.SCIconBadge
import fr.skyle.scanny.designsystem.core.iconbadge.SCIconBadgeDefaults
import fr.skyle.scanny.designsystem.core.text.SCText
import fr.skyle.scanny.designsystem.theme.SCTheme
import fr.skyle.scanny.domain.model.HistoryRetention
import fr.skyle.scanny.resources.Res
import fr.skyle.scanny.resources.ic_auto_delete
import fr.skyle.scanny.resources.keep_history
import fr.skyle.scanny.ui.core.preview.AppPreviewWithBackground
import fr.skyle.scanny.ui.screen.settings.ui.mapper.toText
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/** A setting chosen among [options]: the row shows the [selectedOption], and a touch on it unfolds them. */
@Composable
fun <T> SettingsChoiceRow(
    icon: Painter,
    title: String,
    options: List<T>,
    selectedOption: T,
    optionText: @Composable (T) -> String,
    onOptionSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
) {
    var isExpanded by rememberSaveable { mutableStateOf(false) }

    Row(
        modifier = modifier
            .clickable {
                isExpanded = true
            }
            .heightIn(min = 56.dp)
            .padding(
                horizontal = 16.dp,
                vertical = 8.dp,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SCIconBadge(
            modifier = Modifier.wrapContentSize(),
            icon = icon,
            contentDescription = null,
            style = SCIconBadgeDefaults.Style.PRIMARY,
            size = SCIconBadgeDefaults.Size.MEDIUM,
        )

        Spacer(modifier = Modifier.width(16.dp))

        SCText(
            modifier = Modifier.weight(1f),
            text = title,
            style = SCTheme.typography.body1,
            color = SCTheme.colors.textDark,
        )

        Spacer(modifier = Modifier.width(16.dp))

        // The menu unfolds under the value it changes
        Box(modifier = Modifier.wrapContentSize()) {
            SCText(
                modifier = Modifier.wrapContentSize(),
                text = optionText(selectedOption),
                style = SCTheme.typography.body2,
                color = SCTheme.colors.primary,
            )

            SCDropdownMenu(
                isExpanded = isExpanded,
                onDismissRequest = {
                    isExpanded = false
                },
                options = options,
                selectedOption = selectedOption,
                optionText = optionText,
                onOptionSelected = onOptionSelected,
                style = SCDropdownMenuDefaults.Style.LIGHT,
            )
        }
    }
}

@AppPreviewWithBackground
@Composable
private fun PreviewSettingsChoiceRow() {
    SCTheme {
        SettingsChoiceRow(
            modifier = Modifier.fillMaxWidth(),
            icon = painterResource(Res.drawable.ic_auto_delete),
            title = stringResource(Res.string.keep_history),
            options = HistoryRetention.entries,
            selectedOption = HistoryRetention.ONE_MONTH,
            optionText = { retention -> stringResource(retention.toText()) },
            onOptionSelected = {},
        )
    }
}