package fr.skyle.scanny.ui.screen.feedback.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import fr.skyle.scanny.designsystem.core.button.SCButton
import fr.skyle.scanny.designsystem.core.button.SCButtonDefaults
import fr.skyle.scanny.designsystem.core.card.SCCardDefaults
import fr.skyle.scanny.designsystem.core.card.SCCardStructure
import fr.skyle.scanny.designsystem.core.screenstructure.SCScreenStructure
import fr.skyle.scanny.designsystem.core.screenstructure.SCScreenStructureDefaults
import fr.skyle.scanny.designsystem.core.snackbar.SCSnackbarState
import fr.skyle.scanny.designsystem.core.snackbar.rememberSCSnackbarState
import fr.skyle.scanny.designsystem.core.text.SCText
import fr.skyle.scanny.designsystem.core.textfield.SCTextField
import fr.skyle.scanny.designsystem.core.textfield.SCTextFieldDefaults
import fr.skyle.scanny.designsystem.core.topappbar.SCTopAppBar
import fr.skyle.scanny.designsystem.core.topappbar.SCTopAppBarDefaults
import fr.skyle.scanny.designsystem.theme.SCTheme
import fr.skyle.scanny.resources.Res
import fr.skyle.scanny.resources.feedback
import fr.skyle.scanny.resources.feedback_subject_question
import fr.skyle.scanny.resources.mail_app_opens_with_message
import fr.skyle.scanny.resources.message
import fr.skyle.scanny.resources.send_email
import fr.skyle.scanny.ui.core.preview.AppScreenPreview
import fr.skyle.scanny.ui.screen.feedback.ui.component.FeedbackSubjectRow
import fr.skyle.scanny.ui.screen.feedback.ui.model.FeedbackSubject
import org.jetbrains.compose.resources.stringResource

@Composable
fun FeedbackScreen(
    subject: FeedbackSubject?,
    message: String,
    snackbarState: SCSnackbarState,
    onBackClicked: () -> Unit,
    onSubjectSelected: (FeedbackSubject) -> Unit,
    onMessageChanged: (String) -> Unit,
    onSendClicked: () -> Unit,
) {
    SCScreenStructure(
        modifier = Modifier.fillMaxSize(),
        style = SCScreenStructureDefaults.Style.BACKGROUND,
        topBar = {
            SCTopAppBar(
                modifier = Modifier.fillMaxWidth(),
                style = SCTopAppBarDefaults.Style.BACKGROUND,
                title = stringResource(Res.string.feedback),
                onBackClicked = onBackClicked,
            )
        },
        snackbarState = snackbarState,
    ) { contentPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(state = rememberScrollState())
                .padding(contentPadding)
                .padding(
                    horizontal = 16.dp,
                    vertical = 8.dp,
                ),
        ) {
            SCCardStructure(
                modifier = Modifier.fillMaxWidth(),
                title = stringResource(Res.string.feedback_subject_question),
                style = SCCardDefaults.Style.LIGHT,
            ) {
                FeedbackSubject.entries.forEach { feedbackSubject ->
                    FeedbackSubjectRow(
                        modifier = Modifier.fillMaxWidth(),
                        subject = feedbackSubject,
                        isSelected = feedbackSubject == subject,
                        onClick = { onSubjectSelected(feedbackSubject) },
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            SCTextField(
                modifier = Modifier.fillMaxWidth(),
                value = message,
                onValueChange = onMessageChanged,
                label = stringResource(Res.string.message),
                style = SCTextFieldDefaults.Style.LIGHT,
                minLines = 5,
            )

            Spacer(modifier = Modifier.height(24.dp))

            SCButton(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(Res.string.send_email),
                onClick = onSendClicked,
                style = SCButtonDefaults.Style.PRIMARY,
                size = SCButtonDefaults.Size.MEDIUM,
                isEnabled = subject != null && message.isNotBlank(),
            )

            Spacer(modifier = Modifier.height(12.dp))

            SCText(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(Res.string.mail_app_opens_with_message),
                style = SCTheme.typography.caption,
                color = SCTheme.colors.text,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@AppScreenPreview
@Composable
private fun PreviewFeedbackScreen() {
    SCTheme {
        FeedbackScreen(
            subject = FeedbackSubject.SCAN,
            message = "The scan of a barcode on a curved bottle is slow.",
            snackbarState = rememberSCSnackbarState(),
            onBackClicked = {},
            onSubjectSelected = {},
            onMessageChanged = {},
            onSendClicked = {},
        )
    }
}