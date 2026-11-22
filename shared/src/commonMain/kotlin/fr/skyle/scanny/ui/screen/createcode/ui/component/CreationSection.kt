package fr.skyle.scanny.ui.screen.createcode.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import fr.skyle.scanny.designsystem.core.card.SCCardDefaults
import fr.skyle.scanny.designsystem.core.card.SCCardStructure
import fr.skyle.scanny.designsystem.core.expandablecontent.SCExpandableContent
import fr.skyle.scanny.designsystem.theme.SCTheme
import fr.skyle.scanny.resources.Res
import fr.skyle.scanny.resources.link
import fr.skyle.scanny.resources.more_options
import fr.skyle.scanny.resources.title
import fr.skyle.scanny.ui.core.preview.AppPreviewWithBackground
import org.jetbrains.compose.resources.stringResource

/**
 * A group of the fields of the editor, in a card under its [title]. The fields few need go in
 * [moreOptions], folded under "More options": still there, but out of the way. They unfold when
 * [hasMoreOptionsSet], a value in one of them: one read from a scanned code is not hidden. They then
 * stay unfolded, even once emptied, for the field being typed in not to vanish.
 */
@Composable
fun CreationSection(
    title: String,
    modifier: Modifier = Modifier,
    moreOptions: (@Composable ColumnScope.() -> Unit)? = null,
    hasMoreOptionsSet: Boolean = false,
    content: @Composable ColumnScope.() -> Unit,
) {
    var isExpanded by rememberSaveable { mutableStateOf(false) }
    // The form is filled once composed, from a scanned or a created code
    LaunchedEffect(hasMoreOptionsSet) {
        if (hasMoreOptionsSet) {
            isExpanded = true
        }
    }

    SCCardStructure(
        modifier = modifier,
        title = title,
        style = SCCardDefaults.Style.LIGHT,
    ) {
        // The margins of the card, which brings none
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = 16.dp,
                    top = 16.dp,
                    end = 16.dp,
                    bottom = if (moreOptions != null) 8.dp else 16.dp,
                ),
            verticalArrangement = Arrangement.spacedBy(space = 16.dp),
        ) {
            content()

            if (moreOptions != null) {
                SCExpandableContent(
                    modifier = Modifier.fillMaxWidth(),
                    label = stringResource(Res.string.more_options),
                    isExpanded = isExpanded,
                    onExpandedChange = { expanded -> isExpanded = expanded },
                    content = moreOptions,
                )
            }
        }
    }
}

@AppPreviewWithBackground
@Composable
private fun PreviewCreationSection() {
    SCTheme {
        CreationSection(
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = 16.dp),
            title = stringResource(Res.string.link),
            moreOptions = {
                CreationTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = "",
                    onValueChange = {},
                    label = stringResource(Res.string.title),
                )
            },
        ) {
            CreationTextField(
                modifier = Modifier.fillMaxWidth(),
                value = "scanny.app",
                onValueChange = {},
                label = stringResource(Res.string.link),
                isRequired = true,
            )
        }
    }
}
