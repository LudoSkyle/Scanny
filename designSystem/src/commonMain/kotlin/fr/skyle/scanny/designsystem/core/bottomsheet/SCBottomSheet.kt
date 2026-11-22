package fr.skyle.scanny.designsystem.core.bottomsheet

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import fr.skyle.scanny.designsystem.core.snackbar.SCSnackbarHost
import fr.skyle.scanny.designsystem.core.snackbar.SCSnackbarState
import fr.skyle.scanny.designsystem.core.text.SCText
import fr.skyle.scanny.designsystem.theme.SCTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SCBottomSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    // The messages of the sheet: a message of the screen would show under it
    snackbarState: SCSnackbarState? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    ModalBottomSheet(
        modifier = modifier,
        onDismissRequest = onDismissRequest,
        // Opened in full or closed: the content of a sheet is short
        sheetState = rememberBottomSheetState(
            initialValue = SheetValue.Hidden,
            enabledValues = setOf(
                SheetValue.Hidden,
                SheetValue.Expanded,
            ),
        ),
        shape = SCTheme.shapes.sheet,
        containerColor = SCTheme.colors.background,
        contentColor = SCTheme.colors.textDark,
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                content = content,
            )

            if (snackbarState != null) {
                SCSnackbarHost(
                    modifier = Modifier.align(Alignment.BottomCenter),
                    state = snackbarState,
                )
            }
        }
    }
}

@Preview
@Composable
private fun PreviewSCBottomSheet() {
    SCTheme {
        SCBottomSheet(
            modifier = Modifier.fillMaxWidth(),
            onDismissRequest = {},
        ) {
            SCText(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(all = 24.dp),
                text = "Content",
                style = SCTheme.typography.body1,
            )
        }
    }
}