package fr.skyle.scanny.ui.core.code.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import fr.skyle.scanny.designsystem.core.bottomsheet.SCBottomSheet
import fr.skyle.scanny.designsystem.theme.SCTheme
import fr.skyle.scanny.domain.model.BarcodeFormat
import fr.skyle.scanny.resources.Res
import fr.skyle.scanny.resources.content
import fr.skyle.scanny.resources.format
import fr.skyle.scanny.ui.core.code.mapper.toInfo
import fr.skyle.scanny.ui.core.code.mapper.toText
import fr.skyle.scanny.ui.core.code.model.CodeType
import fr.skyle.scanny.ui.core.preview.AppScreenPreview
import org.jetbrains.compose.resources.stringResource

/**
 * What a code is: its type, what it holds and what Scanny does with it, then its format, what it looks
 * like and where it is found. A format unknown, from a code of an earlier version, is left out.
 */
@Composable
fun CodeInfoSheet(
    type: CodeType,
    format: BarcodeFormat?,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    SCBottomSheet(
        modifier = modifier,
        onDismissRequest = onDismissRequest,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(state = rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
        ) {
            CodeInfoSection(
                label = Res.string.content,
                name = stringResource(type.toText()),
                info = stringResource(type.toInfo()),
            )

            if (format != null) {
                Spacer(modifier = Modifier.height(24.dp))

                CodeInfoSection(
                    label = Res.string.format,
                    name = stringResource(format.toText()),
                    info = stringResource(format.toInfo()),
                )
            }
        }
    }
}

@AppScreenPreview
@Composable
private fun PreviewCodeInfoSheet() {
    SCTheme {
        CodeInfoSheet(
            type = CodeType.WIFI,
            format = BarcodeFormat.QR_CODE,
            onDismissRequest = {},
        )
    }
}
