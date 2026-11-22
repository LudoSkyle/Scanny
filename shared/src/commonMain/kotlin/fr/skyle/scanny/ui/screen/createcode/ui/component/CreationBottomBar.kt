package fr.skyle.scanny.ui.screen.createcode.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import fr.skyle.scanny.designsystem.core.button.SCButton
import fr.skyle.scanny.designsystem.core.button.SCButtonDefaults
import fr.skyle.scanny.designsystem.core.divider.SCDivider
import fr.skyle.scanny.designsystem.core.divider.SCDividerDefaults
import fr.skyle.scanny.designsystem.core.text.SCText
import fr.skyle.scanny.designsystem.theme.SCTheme
import fr.skyle.scanny.domain.model.BarcodeFormat
import fr.skyle.scanny.domain.model.CodeStyle
import fr.skyle.scanny.resources.Res
import fr.skyle.scanny.resources.enlarge_code
import fr.skyle.scanny.resources.fill_content_to_see_code
import fr.skyle.scanny.resources.ic_qr_code
import fr.skyle.scanny.resources.next
import fr.skyle.scanny.resources.the_code
import fr.skyle.scanny.ui.core.code.component.CodeImage
import fr.skyle.scanny.ui.core.code.mapper.toText
import fr.skyle.scanny.ui.core.preview.AppPreviewWithBackground
import fr.skyle.scanny.ui.screen.createcode.ui.mapper.toText
import fr.skyle.scanny.ui.screen.createcode.ui.model.CodePreview
import fr.skyle.scanny.ui.screen.createcode.ui.model.ContentSize
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

// Large enough to see the code change while typing, small enough to leave room to the fields
private val THUMBNAIL_SIZE = 64.dp

/**
 * The bar fixed under the editor, above the keyboard: the code as it will be created, small, touched
 * to see it large; how much of it the content takes, or what keeps it from being made; and the button
 * that goes on.
 */
@Composable
fun CreationBottomBar(
    preview: CodePreview,
    style: CodeStyle,
    contentSize: ContentSize?,
    buttonText: String,
    isButtonEnabled: Boolean,
    onButtonClicked: () -> Unit,
    onCodeClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .background(color = SCTheme.colors.backgroundLight)
            .windowInsetsPadding(insets = WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom)),
    ) {
        SCDivider(
            modifier = Modifier.fillMaxWidth(),
            style = SCDividerDefaults.Style.LIGHT,
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 16.dp,
                    vertical = 12.dp,
                ),
            horizontalArrangement = Arrangement.spacedBy(space = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CodeThumbnail(
                preview = preview,
                style = style,
                onCodeClicked = onCodeClicked,
            )

            Column(modifier = Modifier.weight(1f)) {
                CreationStatus(
                    modifier = Modifier.fillMaxWidth(),
                    preview = preview,
                    contentSize = contentSize,
                )

                Spacer(modifier = Modifier.height(8.dp))

                SCButton(
                    modifier = Modifier.fillMaxWidth(),
                    text = buttonText,
                    onClick = onButtonClicked,
                    style = SCButtonDefaults.Style.PRIMARY,
                    size = SCButtonDefaults.Size.MEDIUM,
                    isEnabled = isButtonEnabled,
                )
            }
        }
    }
}

// The code once its content holds, dark on light as it will be scanned; until then, a sign of a code
@Composable
private fun CodeThumbnail(
    preview: CodePreview,
    style: CodeStyle,
    onCodeClicked: () -> Unit,
) {
    val isCode = preview is CodePreview.Code
    Box(
        modifier = Modifier
            .size(size = THUMBNAIL_SIZE)
            .clip(shape = SCTheme.shapes.medium)
            .background(color = if (isCode) Color.White else SCTheme.colors.backgroundIcon)
            .then(
                if (isCode) {
                    Modifier.clickable(
                        role = Role.Button,
                        onClickLabel = stringResource(Res.string.enlarge_code),
                        onClick = onCodeClicked,
                    )
                } else {
                    Modifier
                },
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (preview is CodePreview.Code) {
            CodeImage(
                modifier = Modifier.fillMaxWidth(),
                rawValue = preview.rawValue,
                format = preview.format,
                contentDescription = stringResource(Res.string.the_code),
                style = style,
                quietZone = 4.dp,
            )
        } else {
            Image(
                modifier = Modifier.size(32.dp),
                painter = painterResource(Res.drawable.ic_qr_code),
                contentDescription = null,
                colorFilter = ColorFilter.tint(color = SCTheme.colors.textDisabled),
            )
        }
    }
}

// What keeps the code from being made, else how much of a QR code the content takes, else the format
@Composable
private fun CreationStatus(
    preview: CodePreview,
    contentSize: ContentSize?,
    modifier: Modifier = Modifier,
) {
    if (preview is CodePreview.Code && contentSize != null) {
        ContentSizeCounter(
            modifier = modifier,
            size = contentSize,
        )
        return
    }

    SCText(
        modifier = modifier,
        text = when (preview) {
            CodePreview.Incomplete -> stringResource(Res.string.fill_content_to_see_code)
            is CodePreview.Refused -> stringResource(preview.toText())
            is CodePreview.Code -> stringResource(preview.format.toText())
        },
        style = SCTheme.typography.caption,
        color = if (preview is CodePreview.Refused) SCTheme.colors.error else SCTheme.colors.text,
    )
}

@AppPreviewWithBackground
@Composable
private fun PreviewCreationBottomBar() {
    SCTheme {
        CreationBottomBar(
            modifier = Modifier.fillMaxWidth(),
            preview = CodePreview.Code(
                rawValue = "https://scanny.app",
                format = BarcodeFormat.QR_CODE,
            ),
            style = CodeStyle(),
            contentSize = ContentSize(
                used = 18,
                max = 2953,
                hasWideCharacters = false,
            ),
            buttonText = stringResource(Res.string.next),
            isButtonEnabled = true,
            onButtonClicked = {},
            onCodeClicked = {},
        )
    }
}
