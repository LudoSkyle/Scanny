package fr.skyle.scanny.ui.core.code.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import fr.skyle.scanny.designsystem.core.text.SCText
import fr.skyle.scanny.designsystem.core.text.SCTextWithCode
import fr.skyle.scanny.designsystem.theme.SCTheme
import fr.skyle.scanny.ui.core.preview.AppPreviewWithBackground

/** A part of the sheet of a format: what it speaks of, then its text. */
@Composable
fun CodeInfoSection(
    label: String,
    text: String,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        SCText(
            modifier = Modifier.fillMaxWidth(),
            text = label,
            style = SCTheme.typography.subtitle1,
            color = SCTheme.colors.text,
        )

        Spacer(modifier = Modifier.height(4.dp))

        // A value in it, between backticks, as code
        SCTextWithCode(
            modifier = Modifier.fillMaxWidth(),
            text = text,
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
            label = "Example",
            text = "4006381333931: 400 for Germany, 6381 for the company, 33393 for the product, 1 for the check.",
        )
    }
}
