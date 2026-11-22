package fr.skyle.scanny.ui.screen.createcode.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import fr.skyle.scanny.designsystem.core.card.SCCard
import fr.skyle.scanny.designsystem.core.card.SCCardDefaults
import fr.skyle.scanny.designsystem.core.text.SCText
import fr.skyle.scanny.designsystem.theme.SCTheme
import fr.skyle.scanny.domain.model.BarcodeFormat
import fr.skyle.scanny.domain.model.CodeShape
import fr.skyle.scanny.domain.model.CodeStyle
import fr.skyle.scanny.resources.Res
import fr.skyle.scanny.resources.fill_content_to_see_code
import fr.skyle.scanny.resources.the_code
import fr.skyle.scanny.ui.core.code.component.CodeImage
import fr.skyle.scanny.ui.core.preview.AppPreviewWithBackground
import fr.skyle.scanny.ui.screen.createcode.ui.mapper.toText
import fr.skyle.scanny.ui.screen.createcode.ui.model.CodePreview
import org.jetbrains.compose.resources.stringResource

// Large enough to be scanned from the screen, not so large it hides the fields
private const val CODE_WIDTH_FRACTION = 0.7f

/** The code as it will be created, in its [style], or what is missing for it. */
@Composable
fun CodePreviewCard(
    preview: CodePreview,
    style: CodeStyle,
    modifier: Modifier = Modifier,
) {
    SCCard(
        modifier = modifier,
        style = SCCardDefaults.Style.LIGHT,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = 16.dp),
            contentAlignment = Alignment.Center,
        ) {
            when (preview) {
                is CodePreview.Code -> CodeImage(
                    modifier = Modifier.fillMaxWidth(fraction = CODE_WIDTH_FRACTION),
                    rawValue = preview.rawValue,
                    format = preview.format,
                    contentDescription = stringResource(Res.string.the_code),
                    style = style,
                )

                CodePreview.Incomplete -> SCText(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(Res.string.fill_content_to_see_code),
                    style = SCTheme.typography.body2,
                    color = SCTheme.colors.text,
                    textAlign = TextAlign.Center,
                )

                is CodePreview.Refused -> SCText(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(preview.toText()),
                    style = SCTheme.typography.body2,
                    color = SCTheme.colors.text,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@AppPreviewWithBackground
@Composable
private fun PreviewCodePreviewCard() {
    SCTheme {
        CodePreviewCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = 16.dp),
            preview = CodePreview.Code(
                rawValue = "https://scanny.app",
                format = BarcodeFormat.QR_CODE,
            ),
            style = CodeStyle(
                darkColor = 0xFF3B43BE,
                dotShape = CodeShape.CIRCLE,
            ),
        )
    }
}