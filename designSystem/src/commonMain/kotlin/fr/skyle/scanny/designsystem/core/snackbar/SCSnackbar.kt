package fr.skyle.scanny.designsystem.core.snackbar

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarVisuals
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import fr.skyle.scanny.designsystem.theme.SCTheme

/** A message of SCSnackbarHost, its action lighter than its text. */
@Composable
internal fun SCSnackbar(
    snackbarData: SnackbarData,
    modifier: Modifier = Modifier,
) {
    Snackbar(
        modifier = modifier,
        snackbarData = snackbarData,
        shape = SCTheme.shapes.medium,
        containerColor = SCTheme.colors.textDark,
        contentColor = SCTheme.colors.textLight,
        actionColor = SCTheme.colors.backgroundIcon,
    )
}

@Preview
@Composable
private fun PreviewSCSnackbar() {
    SCTheme {
        SCSnackbar(
            modifier = Modifier.padding(all = 16.dp),
            snackbarData = object : SnackbarData {
                override val visuals = object : SnackbarVisuals {
                    override val message = "Image saved"
                    override val actionLabel = "See"
                    override val withDismissAction = false
                    override val duration = SnackbarDuration.Long
                }

                override fun performAction() = Unit

                override fun dismiss() = Unit
            },
        )
    }
}
