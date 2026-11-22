package fr.skyle.scanny.ui.screen.createcode.ui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import fr.skyle.scanny.designsystem.core.card.SCCardDefaults
import fr.skyle.scanny.designsystem.core.card.SCExpandableCardStructure
import fr.skyle.scanny.designsystem.core.text.SCText
import fr.skyle.scanny.designsystem.theme.SCTheme
import fr.skyle.scanny.domain.model.CodeErrorCorrection
import fr.skyle.scanny.resources.Res
import fr.skyle.scanny.resources.advanced_options
import fr.skyle.scanny.resources.error_correction
import fr.skyle.scanny.resources.error_correction_explanation
import fr.skyle.scanny.ui.core.preview.AppPreviewWithBackground
import fr.skyle.scanny.ui.screen.createcode.ui.model.ErrorCorrectionChoice
import org.jetbrains.compose.resources.stringResource

/**
 * The options of a QR code few need, folded under their title: its error correction, chosen with its
 * content, since it changes how long a content the code holds.
 */
@Composable
fun AdvancedContentOptions(
    errorCorrectionChoices: List<ErrorCorrectionChoice>,
    selectedErrorCorrection: CodeErrorCorrection,
    onErrorCorrectionSelected: (CodeErrorCorrection) -> Unit,
    modifier: Modifier = Modifier,
) {
    var isExpanded by rememberSaveable { mutableStateOf(false) }

    SCExpandableCardStructure(
        modifier = modifier,
        title = stringResource(Res.string.advanced_options),
        isExpanded = isExpanded,
        onExpandedChange = { expanded -> isExpanded = expanded },
        style = SCCardDefaults.Style.LIGHT,
    ) {
        // The margins of the card, which brings none
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = 16.dp,
                    top = 12.dp,
                    end = 16.dp,
                    bottom = 16.dp,
                ),
        ) {
            SCText(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(Res.string.error_correction),
                style = SCTheme.typography.body1,
                color = SCTheme.colors.textDark,
            )

            Spacer(modifier = Modifier.height(4.dp))

            SCText(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(Res.string.error_correction_explanation),
                style = SCTheme.typography.caption,
                color = SCTheme.colors.text,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Column(modifier = Modifier.selectableGroup()) {
                errorCorrectionChoices.forEach { choice ->
                    ErrorCorrectionRow(
                        modifier = Modifier.fillMaxWidth(),
                        choice = choice,
                        isSelected = choice.errorCorrection == selectedErrorCorrection,
                        onClick = { onErrorCorrectionSelected(choice.errorCorrection) },
                    )
                }
            }
        }
    }
}

@AppPreviewWithBackground
@Composable
private fun PreviewAdvancedContentOptions() {
    SCTheme {
        AdvancedContentOptions(
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = 16.dp),
            errorCorrectionChoices = CodeErrorCorrection.entries.map { errorCorrection ->
                ErrorCorrectionChoice(
                    errorCorrection = errorCorrection,
                    blocker = null,
                )
            },
            selectedErrorCorrection = CodeErrorCorrection.AUTO,
            onErrorCorrectionSelected = {},
        )
    }
}
