package fr.skyle.scanny.ui.screen.feedback.ui.component

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
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
import fr.skyle.scanny.ui.core.preview.AppPreviewWithBackground
import fr.skyle.scanny.ui.screen.feedback.ui.mapper.toText
import fr.skyle.scanny.ui.screen.feedback.ui.model.FeedbackSubject
import org.jetbrains.compose.resources.stringResource

/** A subject of feedback, chosen by a touch anywhere on its row. */
@Composable
fun FeedbackSubjectRow(
    subject: FeedbackSubject,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .selectable(
                selected = isSelected,
                role = Role.RadioButton,
                onClick = onClick,
            )
            .padding(
                horizontal = 8.dp,
                vertical = 4.dp,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SCRadioButton(
            modifier = Modifier.wrapContentSize(),
            isSelected = isSelected,
            onClick = null,
            style = SCRadioButtonDefaults.Style.PRIMARY,
        )

        Spacer(modifier = Modifier.width(8.dp))

        SCText(
            modifier = Modifier.weight(1f),
            text = stringResource(subject.toText()),
            style = SCTheme.typography.body1,
            color = SCTheme.colors.textDark,
        )
    }
}

@AppPreviewWithBackground
@Composable
private fun PreviewFeedbackSubjectRow() {
    SCTheme {
        FeedbackSubjectRow(
            modifier = Modifier.fillMaxWidth(),
            subject = FeedbackSubject.CODE_DETECTION,
            isSelected = true,
            onClick = {},
        )
    }
}