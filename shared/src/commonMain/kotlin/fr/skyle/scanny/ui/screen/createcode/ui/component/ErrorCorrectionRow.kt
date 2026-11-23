package fr.skyle.scanny.ui.screen.createcode.ui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.selection.selectable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import fr.skyle.scanny.designsystem.core.radiobutton.SCRadioButton
import fr.skyle.scanny.designsystem.core.radiobutton.SCRadioButtonDefaults
import fr.skyle.scanny.designsystem.core.text.SCText
import fr.skyle.scanny.designsystem.theme.SCTheme
import fr.skyle.scanny.domain.model.CodeErrorCorrection
import fr.skyle.scanny.ui.core.preview.AppPreviewWithBackground
import fr.skyle.scanny.ui.screen.createcode.ui.mapper.toDescription
import fr.skyle.scanny.ui.screen.createcode.ui.mapper.toText
import fr.skyle.scanny.ui.screen.createcode.ui.model.ErrorCorrectionBlocker
import fr.skyle.scanny.ui.screen.createcode.ui.model.ErrorCorrectionChoice
import org.jetbrains.compose.resources.stringResource

/** A correction offered for the code: its name, how much of the code it makes up for, and what keeps it from being chosen. */
@Composable
fun ErrorCorrectionRow(
    choice: ErrorCorrectionChoice,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isEnabled = choice.blocker == null

    Row(
        modifier = modifier
            .selectable(
                selected = isSelected,
                enabled = isEnabled,
                role = Role.RadioButton,
                onClick = onClick,
            )
            .heightIn(min = 48.dp)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SCRadioButton(
            modifier = Modifier.wrapContentSize(),
            isSelected = isSelected,
            onClick = null,
            style = SCRadioButtonDefaults.Style.PRIMARY,
            isEnabled = isEnabled,
        )

        Spacer(modifier = Modifier.width(8.dp))

        Column(modifier = Modifier.weight(1f)) {
            SCText(
                text = stringResource(choice.errorCorrection.toText()),
                style = SCTheme.typography.body2,
                color = if (isEnabled) SCTheme.colors.textDark else SCTheme.colors.textDisabled,
            )

            SCText(
                text = stringResource(choice.blocker?.toText() ?: choice.errorCorrection.toDescription()),
                style = SCTheme.typography.caption,
                color = if (isEnabled) SCTheme.colors.text else SCTheme.colors.textDisabled,
            )
        }
    }
}

@AppPreviewWithBackground
@Composable
private fun PreviewErrorCorrectionRow() {
    SCTheme {
        Column {
            ErrorCorrectionRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                choice = ErrorCorrectionChoice(
                    errorCorrection = CodeErrorCorrection.AUTO,
                    blocker = null,
                ),
                isSelected = true,
                onClick = {},
            )

            ErrorCorrectionRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                choice = ErrorCorrectionChoice(
                    errorCorrection = CodeErrorCorrection.HIGH,
                    blocker = ErrorCorrectionBlocker.TOO_HIGH_FOR_CONTENT,
                ),
                isSelected = false,
                onClick = {},
            )
        }
    }
}
