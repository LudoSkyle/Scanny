package fr.skyle.scanny.ui.core.code.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import fr.skyle.scanny.designsystem.core.button.SCButton
import fr.skyle.scanny.designsystem.core.button.SCButtonDefaults
import fr.skyle.scanny.designsystem.theme.SCTheme
import fr.skyle.scanny.ui.core.code.mapper.toText
import fr.skyle.scanny.ui.core.code.model.CodeAction
import fr.skyle.scanny.ui.core.preview.AppPreview
import org.jetbrains.compose.resources.stringResource

/** The main button of a code, named after what it does. */
@Composable
fun CodeActionButton(
    action: CodeAction,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    SCButton(
        modifier = modifier,
        text = stringResource(action.toText()),
        onClick = onClick,
        style = SCButtonDefaults.Style.PRIMARY,
        size = SCButtonDefaults.Size.MEDIUM,
    )
}

@AppPreview
@Composable
private fun PreviewCodeActionButton() {
    SCTheme {
        CodeActionButton(
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = 16.dp),
            action = CodeAction.OpenLink(link = "https://scanny.app"),
            onClick = {},
        )
    }
}