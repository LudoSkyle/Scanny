package fr.skyle.scanny.ui.core.code.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import fr.skyle.scanny.designsystem.core.text.SCText
import fr.skyle.scanny.designsystem.theme.SCTheme
import fr.skyle.scanny.resources.Res
import fr.skyle.scanny.resources.content
import fr.skyle.scanny.ui.core.preview.AppPreviewWithBackground
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/** A part of the sheet: what it speaks of, the name of the type or of the format, and its text. */
@Composable
fun CodeInfoSection(
    label: StringResource,
    name: String,
    info: String,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        SCText(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(label),
            style = SCTheme.typography.subtitle2,
            color = SCTheme.colors.text,
        )

        Spacer(modifier = Modifier.height(2.dp))

        SCText(
            modifier = Modifier
                .fillMaxWidth()
                .semantics { heading() },
            text = name,
            style = SCTheme.typography.h3,
            color = SCTheme.colors.textDark,
        )

        Spacer(modifier = Modifier.height(8.dp))

        SCText(
            modifier = Modifier.fillMaxWidth(),
            text = info,
            style = SCTheme.typography.body1,
            color = SCTheme.colors.textDark,
        )
    }
}

@AppPreviewWithBackground
@Composable
private fun PreviewCodeInfoSection() {
    SCTheme {
        CodeInfoSection(
            modifier = Modifier.padding(all = 24.dp),
            label = Res.string.content,
            name = "Wi-Fi",
            info = "The name, the security and the password of a Wi-Fi network, to join it without typing them.",
        )
    }
}
