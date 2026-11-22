package fr.skyle.scanny.ui.screen.feedback.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import fr.skyle.scanny.designsystem.core.snackbar.rememberSCSnackbarState
import fr.skyle.scanny.designsystem.theme.SCTheme
import fr.skyle.scanny.resources.Res
import fr.skyle.scanny.resources.feedback_about_scan
import fr.skyle.scanny.resources.send_email
import fr.skyle.scanny.resources.subject
import fr.skyle.scanny.ui.screen.feedback.ui.model.FeedbackSubject
import fr.skyle.scanny.ui.test.ComposeTest
import org.jetbrains.compose.resources.getString
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
internal class FeedbackScreenTest : ComposeTest() {

    @Test
    fun `the e-mail is sent once it has a subject and a message`() = runComposeUiTest {
        // Given
        var subject by mutableStateOf<FeedbackSubject?>(null)
        var message by mutableStateOf("")
        setContent {
            feedbackScreen(
                subject = subject,
                message = message,
                onSubjectSelected = { selected -> subject = selected },
            )
        }
        onNodeWithText(getString(Res.string.send_email)).assertIsNotEnabled()

        // When
        // The subject is chosen in a menu that unfolds under its field
        onNodeWithText(getString(Res.string.subject)).performClick()
        onNodeWithText(getString(Res.string.feedback_about_scan)).performClick()

        // Then
        onNodeWithText(getString(Res.string.send_email)).assertIsNotEnabled()

        // When
        message = "The scan is slow on a curved bottle"

        // Then
        onNodeWithText(getString(Res.string.send_email)).assertIsEnabled()
    }

    @Composable
    private fun feedbackScreen(
        subject: FeedbackSubject?,
        message: String,
        onSubjectSelected: (FeedbackSubject) -> Unit,
    ) {
        SCTheme {
            FeedbackScreen(
                subject = subject,
                message = message,
                snackbarState = rememberSCSnackbarState(),
                onBackClicked = {},
                onSubjectSelected = onSubjectSelected,
                onMessageChanged = {},
                onSendClicked = {},
            )
        }
    }
}
