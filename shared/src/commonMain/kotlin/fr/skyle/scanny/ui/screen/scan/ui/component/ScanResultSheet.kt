package fr.skyle.scanny.ui.screen.scan.ui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import fr.skyle.scanny.designsystem.core.bottomsheet.SCBottomSheet
import fr.skyle.scanny.designsystem.core.button.SCButton
import fr.skyle.scanny.designsystem.core.button.SCButtonDefaults
import fr.skyle.scanny.designsystem.core.snackbar.SCSnackbarState
import fr.skyle.scanny.designsystem.theme.SCTheme
import fr.skyle.scanny.domain.model.BarcodeFormat
import fr.skyle.scanny.resources.Res
import fr.skyle.scanny.resources.copied
import fr.skyle.scanny.resources.copy
import fr.skyle.scanny.resources.see_detail
import fr.skyle.scanny.ui.core.code.component.CodeActionButton
import fr.skyle.scanny.ui.core.code.component.CodeFieldText
import fr.skyle.scanny.ui.core.code.component.CodeHeader
import fr.skyle.scanny.ui.core.code.component.codeFieldValueText
import fr.skyle.scanny.ui.core.code.mapper.toText
import fr.skyle.scanny.ui.core.code.model.CodeAction
import fr.skyle.scanny.ui.core.code.model.CodeField
import fr.skyle.scanny.ui.core.code.model.CodeFieldLabel
import fr.skyle.scanny.ui.core.code.model.CodeType
import fr.skyle.scanny.ui.core.preview.AppScreenPreview
import fr.skyle.scanny.ui.screen.scan.ui.model.ScanResultItem
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Duration.Companion.seconds

private val COPIED_DURATION = 2.seconds

private const val FIELD_MAX_LINES = 4

/** The code just scanned: what it is, what it holds, and what to do with it. */
@Composable
fun ScanResultSheet(
    result: ScanResultItem,
    onActionClicked: (CodeAction) -> Unit,
    onCopyClicked: () -> Unit,
    onSeeDetailClicked: () -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    // What came of its action, a network joined or a link no app opens: a message of the screen would show under it
    snackbarState: SCSnackbarState? = null,
) {
    // Told on the button itself: a message of the screen would show under the sheet
    var isCopied by remember(result.codeId) { mutableStateOf(false) }

    LaunchedEffect(isCopied) {
        if (isCopied) {
            delay(COPIED_DURATION)
            isCopied = false
        }
    }

    SCBottomSheet(
        modifier = modifier,
        onDismissRequest = onDismissRequest,
        snackbarState = snackbarState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
        ) {
            CodeHeader(
                modifier = Modifier.fillMaxWidth(),
                type = result.type,
                format = result.format,
            )

            Spacer(modifier = Modifier.height(16.dp))

            // The whole content, a long text cut: the detail shows it whole
            result.fields.forEachIndexed { index, field ->
                if (index > 0) {
                    Spacer(modifier = Modifier.height(12.dp))
                }

                CodeFieldText(
                    modifier = Modifier.fillMaxWidth(),
                    label = field.label?.let { label -> stringResource(label.toText()) },
                    value = codeFieldValueText(field = field),
                    maxLines = FIELD_MAX_LINES,
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            result.action?.let { action ->
                CodeActionButton(
                    modifier = Modifier.fillMaxWidth(),
                    action = action,
                    onClick = { onActionClicked(action) },
                )

                Spacer(modifier = Modifier.height(12.dp))
            }

            Row(modifier = Modifier.fillMaxWidth()) {
                SCButton(
                    modifier = Modifier.weight(1f),
                    text = stringResource(if (isCopied) Res.string.copied else Res.string.copy),
                    onClick = {
                        onCopyClicked()
                        isCopied = true
                    },
                    style = SCButtonDefaults.Style.SECONDARY,
                    size = SCButtonDefaults.Size.MEDIUM,
                )

                Spacer(modifier = Modifier.width(12.dp))

                SCButton(
                    modifier = Modifier.weight(1f),
                    text = stringResource(Res.string.see_detail),
                    onClick = onSeeDetailClicked,
                    style = SCButtonDefaults.Style.SECONDARY,
                    size = SCButtonDefaults.Size.MEDIUM,
                )
            }
        }
    }
}

@AppScreenPreview
@Composable
private fun PreviewScanResultSheet() {
    SCTheme {
        ScanResultSheet(
            modifier = Modifier.fillMaxWidth(),
            result = ScanResultItem(
                codeId = 1,
                type = CodeType.LINK,
                format = BarcodeFormat.QR_CODE,
                fields = listOf(
                    CodeField(
                        label = null,
                        value = "https://scanny.app",
                    ),
                ),
                rawValue = "https://scanny.app",
                action = CodeAction.OpenLink(link = "https://scanny.app"),
            ),
            onActionClicked = {},
            onCopyClicked = {},
            onSeeDetailClicked = {},
            onDismissRequest = {},
        )
    }
}

@AppScreenPreview
@Composable
private fun PreviewScanResultSheet_WithoutAction() {
    SCTheme {
        ScanResultSheet(
            modifier = Modifier.fillMaxWidth(),
            result = ScanResultItem(
                codeId = 1,
                type = CodeType.SMS,
                format = BarcodeFormat.QR_CODE,
                fields = listOf(
                    CodeField(
                        label = CodeFieldLabel.PHONE_NUMBER,
                        value = "+33600000000",
                    ),
                    CodeField(
                        label = CodeFieldLabel.MESSAGE,
                        value = "See you at eight in front of the cinema",
                    ),
                ),
                rawValue = "SMSTO:+33600000000:See you at eight in front of the cinema",
                action = null,
            ),
            onActionClicked = {},
            onCopyClicked = {},
            onSeeDetailClicked = {},
            onDismissRequest = {},
        )
    }
}