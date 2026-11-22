package fr.skyle.scanny.designsystem.core.snackbar

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember

/** The short messages of a screen or of a sheet, shown at its bottom by SCSnackbarHost. */
@Stable
class SCSnackbarState internal constructor(
    internal val hostState: SnackbarHostState,
) {

    /**
     * Shows [message], with an action named [actionLabel] if any, and tells whether the action was
     * touched before the message left. A message with an action stays longer, for it to be reached.
     */
    suspend fun show(
        message: String,
        actionLabel: String? = null,
    ): Boolean =
        hostState.showSnackbar(
            message = message,
            actionLabel = actionLabel,
            duration = if (actionLabel != null) SnackbarDuration.Long else SnackbarDuration.Short,
        ) == SnackbarResult.ActionPerformed

    /** Hides the message shown, if any: a sheet that closes takes its host, and the message would wait for it. */
    fun dismiss() {
        hostState.currentSnackbarData?.dismiss()
    }
}

@Composable
fun rememberSCSnackbarState(): SCSnackbarState =
    remember {
        SCSnackbarState(hostState = SnackbarHostState())
    }
