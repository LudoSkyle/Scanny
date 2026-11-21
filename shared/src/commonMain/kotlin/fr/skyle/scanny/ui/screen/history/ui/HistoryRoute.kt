package fr.skyle.scanny.ui.screen.history.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import fr.skyle.scanny.designsystem.core.snackbar.rememberSCSnackbarState
import fr.skyle.scanny.resources.Res
import fr.skyle.scanny.resources.history_not_cleared
import fr.skyle.scanny.ui.screen.history.ui.component.ClearHistoryDialog
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun HistoryRoute(
    navigateToScanDetail: (codeId: Long) -> Unit,
    navigateToScanGroup: (scanId: Long) -> Unit,
    navigateBack: () -> Unit,
    viewModel: HistoryViewModel = koinViewModel(),
) {
    val lifecycleOwner = LocalLifecycleOwner.current

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val snackbarState = rememberSCSnackbarState()

    var isClearDialogVisible by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.event.collect { event ->
                when (event) {
                    HistoryViewModel.HistoryEvent.ClearFailed -> launch {
                        snackbarState.show(message = getString(Res.string.history_not_cleared))
                    }

                    is HistoryViewModel.HistoryEvent.OpenScanDetail -> {
                        navigateToScanDetail(event.codeId)
                    }

                    is HistoryViewModel.HistoryEvent.OpenScanGroup -> {
                        navigateToScanGroup(event.scanId)
                    }
                }
            }
        }
    }

    HistoryScreen(
        sections = uiState.sections,
        snackbarState = snackbarState,
        onBackClicked = navigateBack,
        onClearClicked = {
            isClearDialogVisible = true
        },
        onScanClicked = viewModel::onScanClicked,
    )

    if (isClearDialogVisible) {
        ClearHistoryDialog(
            onConfirmClicked = {
                isClearDialogVisible = false
                viewModel.onClearConfirmed()
            },
            onDismissRequest = {
                isClearDialogVisible = false
            },
        )
    }
}