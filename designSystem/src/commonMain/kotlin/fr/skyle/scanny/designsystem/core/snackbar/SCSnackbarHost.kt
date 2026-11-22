package fr.skyle.scanny.designsystem.core.snackbar

import androidx.compose.material3.SnackbarHost
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/** Where the messages of [state] show: at the bottom of a screen, or of a sheet that hides it. */
@Composable
fun SCSnackbarHost(
    state: SCSnackbarState,
    modifier: Modifier = Modifier,
) {
    SnackbarHost(
        modifier = modifier,
        hostState = state.hostState,
    ) { snackbarData ->
        SCSnackbar(snackbarData = snackbarData)
    }
}
