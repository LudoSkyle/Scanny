package fr.skyle.scanny.ui.screen.createcode.ui.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import fr.skyle.scanny.designsystem.core.text.SCText
import fr.skyle.scanny.designsystem.theme.SCTheme
import fr.skyle.scanny.resources.Res
import fr.skyle.scanny.resources.code_inverted
import fr.skyle.scanny.resources.code_low_contrast
import fr.skyle.scanny.ui.core.code.model.CodeReadability
import fr.skyle.scanny.ui.core.preview.AppPreviewWithBackground
import org.jetbrains.compose.resources.stringResource

/** Why scanners may read the code poorly, in its colors: the user is told, not stopped. Nothing when it reads well. */
@Composable
fun CodeReadabilityWarning(
    readability: CodeReadability,
    modifier: Modifier = Modifier,
) {
    val text = when (readability) {
        CodeReadability.GOOD -> return
        CodeReadability.LOW_CONTRAST -> stringResource(Res.string.code_low_contrast)
        CodeReadability.INVERTED -> stringResource(Res.string.code_inverted)
    }

    SCText(
        modifier = modifier,
        text = text,
        style = SCTheme.typography.body2,
        color = SCTheme.colors.error,
    )
}

@AppPreviewWithBackground
@Composable
private fun PreviewCodeReadabilityWarning() {
    SCTheme {
        CodeReadabilityWarning(
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = 16.dp),
            readability = CodeReadability.INVERTED,
        )
    }
}
