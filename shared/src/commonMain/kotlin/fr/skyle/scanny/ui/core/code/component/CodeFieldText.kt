package fr.skyle.scanny.ui.core.code.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import fr.skyle.scanny.designsystem.core.text.SCText
import fr.skyle.scanny.designsystem.theme.SCTheme
import fr.skyle.scanny.resources.Res
import fr.skyle.scanny.resources.message
import fr.skyle.scanny.ui.core.preview.AppPreviewWithBackground
import org.jetbrains.compose.resources.stringResource

/** A piece of the content, named unless it is the content itself, cut after [maxLines] lines. */
@Composable
fun CodeFieldText(
    label: String?,
    value: String,
    modifier: Modifier = Modifier,
    maxLines: Int = Int.MAX_VALUE,
) {
    Column(modifier = modifier) {
        if (label != null) {
            SCText(
                modifier = Modifier.fillMaxWidth(),
                text = label,
                style = SCTheme.typography.caption,
                color = SCTheme.colors.text,
            )

            Spacer(modifier = Modifier.height(2.dp))
        }

        SCText(
            modifier = Modifier.fillMaxWidth(),
            text = value,
            style = SCTheme.typography.body1,
            color = SCTheme.colors.textDark,
            maxLines = maxLines,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@AppPreviewWithBackground
@Composable
private fun PreviewCodeFieldText() {
    SCTheme {
        CodeFieldText(
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = 16.dp),
            label = stringResource(Res.string.message),
            value = "See you at eight in front of the cinema",
        )
    }
}
