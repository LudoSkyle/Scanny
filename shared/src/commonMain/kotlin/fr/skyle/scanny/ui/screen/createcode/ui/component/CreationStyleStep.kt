package fr.skyle.scanny.ui.screen.createcode.ui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import fr.skyle.scanny.designsystem.theme.SCTheme
import fr.skyle.scanny.domain.model.BarcodeFormat
import fr.skyle.scanny.domain.model.CodeShape
import fr.skyle.scanny.domain.model.CodeStyle
import fr.skyle.scanny.ui.core.preview.AppPreviewWithBackground
import fr.skyle.scanny.ui.screen.createcode.ui.model.CodePreview
import fr.skyle.scanny.ui.screen.createcode.ui.model.LogoBlocker

/**
 * The second step of the editor of a QR code: how it looks, seen on the code as it is chosen, large at
 * the top. The button that creates it is in the bar fixed under it.
 */
@Composable
fun CreationStyleStep(
    preview: CodePreview,
    style: CodeStyle,
    logoBlocker: LogoBlocker?,
    contentPadding: PaddingValues,
    onStyleChanged: (CodeStyle) -> Unit,
    onAddLogoClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            // Out of the scroll: what is scrolled to stays above the bar under it
            .padding(contentPadding)
            .verticalScroll(state = rememberScrollState())
            .padding(
                horizontal = 16.dp,
                vertical = 8.dp,
            ),
    ) {
        CodePreviewCard(
            modifier = Modifier.fillMaxWidth(),
            preview = preview,
            style = style,
        )

        Spacer(modifier = Modifier.height(24.dp))

        CodeStyleFields(
            modifier = Modifier.fillMaxWidth(),
            style = style,
            logoBlocker = logoBlocker,
            onStyleChanged = onStyleChanged,
            onAddLogoClicked = onAddLogoClicked,
        )
    }
}

@AppPreviewWithBackground
@Composable
private fun PreviewCreationStyleStep() {
    SCTheme {
        CreationStyleStep(
            preview = CodePreview.Code(
                rawValue = "https://scanny.app",
                format = BarcodeFormat.QR_CODE,
            ),
            style = CodeStyle(
                darkColor = 0xFF3B43BE,
                dotShape = CodeShape.ROUNDED,
            ),
            logoBlocker = null,
            contentPadding = PaddingValues(),
            onStyleChanged = {},
            onAddLogoClicked = {},
        )
    }
}
