package fr.skyle.scanny.ui.screen.feedback.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalUriHandler
import fr.skyle.scanny.designsystem.core.snackbar.rememberSCSnackbarState
import fr.skyle.scanny.resources.Res
import fr.skyle.scanny.resources.no_app_to_open
import fr.skyle.scanny.ui.core.link.openLink
import fr.skyle.scanny.ui.screen.feedback.ui.mapper.toFeedbackMailtoLink
import fr.skyle.scanny.ui.screen.feedback.ui.model.FeedbackSubject
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString

// No ViewModel: the feedback is written here and sent by the mail app of the device
@Composable
fun FeedbackRoute(
    navigateBack: () -> Unit,
) {
    val uriHandler = LocalUriHandler.current
    val coroutineScope = rememberCoroutineScope()

    val snackbarState = rememberSCSnackbarState()

    var subject by rememberSaveable { mutableStateOf<FeedbackSubject?>(null) }
    var message by rememberSaveable { mutableStateOf("") }

    FeedbackScreen(
        subject = subject,
        message = message,
        snackbarState = snackbarState,
        onBackClicked = navigateBack,
        onSubjectSelected = { selectedSubject ->
            subject = selectedSubject
        },
        onMessageChanged = { writtenMessage ->
            message = writtenMessage
        },
        onSendClicked = {
            val link = subject?.toFeedbackMailtoLink(message = message) ?: return@FeedbackScreen
            if (!uriHandler.openLink(link = link)) {
                coroutineScope.launch {
                    snackbarState.show(message = getString(Res.string.no_app_to_open))
                }
            }
        },
    )
}