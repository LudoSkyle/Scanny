package fr.skyle.scanny.designsystem.core.snackbar

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember

/** The short messages of a screen, shown at its bottom by SCScreenStructure. */
@Stable
class SCSnackbarState internal constructor(
    internal val hostState: SnackbarHostState,
) {

    suspend fun show(message: String) {
        hostState.showSnackbar(message = message)
    }
}

@Composable
fun rememberSCSnackbarState(): SCSnackbarState =
    remember {
        SCSnackbarState(hostState = SnackbarHostState())
    }