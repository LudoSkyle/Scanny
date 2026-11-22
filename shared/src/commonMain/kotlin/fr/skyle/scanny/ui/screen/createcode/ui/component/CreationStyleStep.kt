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
import fr.skyle.scanny.designsystem.core.button.SCButton
import fr.skyle.scanny.designsystem.core.button.SCButtonDefaults
import fr.skyle.scanny.designsystem.theme.SCTheme
import fr.skyle.scanny.domain.model.BarcodeFormat
import fr.skyle.scanny.domain.model.CodeShape
import fr.skyle.scanny.domain.model.CodeStyle
import fr.skyle.scanny.resources.Res
import fr.skyle.scanny.resources.create
import fr.skyle.scanny.ui.core.preview.AppPreviewWithBackground
import fr.skyle.scanny.ui.screen.createcode.ui.model.CodePreview
import fr.skyle.scanny.ui.screen.createcode.ui.model.LogoBlocker
import org.jetbrains.compose.resources.stringResource

/** The second step of the editor of a QR code: how it looks, seen on the code as it is chosen. */
@Composable
fun CreationStyleStep(
    preview: CodePreview,
    style: CodeStyle,
    logoBlocker: LogoBlocker?,
    isCreating: Boolean,
    contentPadding: PaddingValues,
    onStyleChanged: (CodeStyle) -> Unit,
    onAddLogoClicked: () -> Unit,
    onCreateClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(state = rememberScrollState())
            .padding(contentPadding)
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

        Spacer(modifier = Modifier.height(24.dp))

        SCButton(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(Res.string.create),
            onClick = onCreateClicked,
            style = SCButtonDefaults.Style.PRIMARY,
            size = SCButtonDefaults.Size.MEDIUM,
            isEnabled = preview is CodePreview.Code && !isCreating,
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
            isCreating = false,
            contentPadding = PaddingValues(),
            onStyleChanged = {},
            onAddLogoClicked = {},
            onCreateClicked = {},
        )
    }
}
