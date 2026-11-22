package fr.skyle.scanny.ui.screen.createcode.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import fr.skyle.scanny.designsystem.core.text.SCText
import fr.skyle.scanny.designsystem.theme.SCTheme
import fr.skyle.scanny.resources.Res
import fr.skyle.scanny.resources.content_size
import fr.skyle.scanny.resources.content_size_count
import fr.skyle.scanny.resources.content_size_wide_characters
import fr.skyle.scanny.ui.core.preview.AppPreviewWithBackground
import fr.skyle.scanny.ui.screen.createcode.ui.model.ContentSize
import org.jetbrains.compose.resources.stringResource

/**
 * How much of the QR code its whole content takes, as the counter of a comment field: red once over the
 * most the code holds.
 */
@Composable
fun ContentSizeCounter(
    size: ContentSize,
    modifier: Modifier = Modifier,
) {
    // Read as one: the name of the counter, then its count
    Column(modifier = modifier.semantics(mergeDescendants = true) {}) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            SCText(
                text = stringResource(Res.string.content_size),
                style = SCTheme.typography.caption,
                color = SCTheme.colors.text,
            )

            SCText(
                text = stringResource(Res.string.content_size_count, size.used, size.max),
                style = SCTheme.typography.caption,
                color = if (size.isOverLimit) SCTheme.colors.error else SCTheme.colors.text,
            )
        }

        if (size.hasWideCharacters) {
            Spacer(modifier = Modifier.height(4.dp))

            SCText(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(Res.string.content_size_wide_characters),
                style = SCTheme.typography.caption,
                color = SCTheme.colors.text,
            )
        }
    }
}

@AppPreviewWithBackground
@Composable
private fun PreviewContentSizeCounter() {
    SCTheme {
        ContentSizeCounter(
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = 16.dp),
            size = ContentSize(
                used = 1280,
                max = 1273,
                hasWideCharacters = true,
            ),
        )
    }
}
