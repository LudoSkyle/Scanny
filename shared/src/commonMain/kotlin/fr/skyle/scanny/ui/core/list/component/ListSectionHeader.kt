package fr.skyle.scanny.ui.core.list.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import fr.skyle.scanny.designsystem.core.text.SCText
import fr.skyle.scanny.designsystem.theme.SCTheme
import fr.skyle.scanny.resources.Res
import fr.skyle.scanny.resources.codes_of_group
import fr.skyle.scanny.ui.core.preview.AppPreviewWithBackground
import org.jetbrains.compose.resources.stringResource

/** The title of the codes that follow it in a list: the codes of a day, the favorites, those of a group. */
@Composable
fun ListSectionHeader(
    text: String,
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
        text = text,
        style = SCTheme.typography.subtitle1,
        color = SCTheme.colors.text,
    )
}

@AppPreviewWithBackground
@Composable
private fun PreviewListSectionHeader() {
    SCTheme {
        ListSectionHeader(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(Res.string.codes_of_group),
        )
    }
}
