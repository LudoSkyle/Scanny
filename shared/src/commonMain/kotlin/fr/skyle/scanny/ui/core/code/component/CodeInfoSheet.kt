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
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import fr.skyle.scanny.designsystem.core.bottomsheet.SCBottomSheet
import fr.skyle.scanny.designsystem.core.text.SCText
import fr.skyle.scanny.designsystem.theme.SCTheme
import fr.skyle.scanny.domain.model.BarcodeFormat
import fr.skyle.scanny.resources.Res
import fr.skyle.scanny.resources.format_example
import fr.skyle.scanny.resources.format_structure
import fr.skyle.scanny.resources.format_use
import fr.skyle.scanny.ui.core.code.mapper.toExample
import fr.skyle.scanny.ui.core.code.mapper.toStructure
import fr.skyle.scanny.ui.core.code.mapper.toText
import fr.skyle.scanny.ui.core.code.mapper.toUse
import fr.skyle.scanny.ui.core.preview.AppScreenPreview
import org.jetbrains.compose.resources.stringResource

/** What a format of code is: where it is found, how it is made, and a code of it taken apart. */
@Composable
fun CodeInfoSheet(
    format: BarcodeFormat,
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
            SCText(
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { heading() },
                text = stringResource(format.toText()),
                style = SCTheme.typography.h3,
                color = SCTheme.colors.textDark,
            )

            Spacer(modifier = Modifier.height(16.dp))

            CodeInfoSection(
                label = stringResource(Res.string.format_use),
                text = stringResource(format.toUse()),
            )

            Spacer(modifier = Modifier.height(16.dp))

            CodeInfoSection(
                label = stringResource(Res.string.format_structure),
                text = stringResource(format.toStructure()),
            )

            Spacer(modifier = Modifier.height(16.dp))

            CodeInfoSection(
                label = stringResource(Res.string.format_example),
                text = stringResource(format.toExample()),
            )
        }
    }
}

@AppScreenPreview
@Composable
private fun PreviewCodeInfoSheet() {
    SCTheme {
        CodeInfoSheet(
            format = BarcodeFormat.EAN_13,
            onDismissRequest = {},
        )
    }
}
