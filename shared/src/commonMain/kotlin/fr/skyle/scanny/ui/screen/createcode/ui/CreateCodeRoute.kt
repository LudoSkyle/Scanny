package fr.skyle.scanny.ui.screen.createcode.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import fr.skyle.scanny.designsystem.core.snackbar.rememberSCSnackbarState
import fr.skyle.scanny.platform.gallery.rememberLogoPicker
import fr.skyle.scanny.resources.Res
import fr.skyle.scanny.resources.creation_not_saved
import fr.skyle.scanny.resources.logo_not_read
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun CreateCodeRoute(
    navigateToCreationDetail: (creationId: Long) -> Unit,
    navigateBack: () -> Unit,
    viewModel: CreateCodeViewModel = koinViewModel(),
) {
    val lifecycleOwner = LocalLifecycleOwner.current

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val snackbarState = rememberSCSnackbarState()

    val pickLogo = rememberLogoPicker(onResult = viewModel::onLogoPicked)

    LaunchedEffect(Unit) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.event.collect { event ->
                when (event) {
                    is CreateCodeViewModel.CreateCodeEvent.OpenCreationDetail -> {
                        navigateToCreationDetail(event.creationId)
                    }

                    CreateCodeViewModel.CreateCodeEvent.CreationNotSaved -> launch {
                        snackbarState.show(message = getString(Res.string.creation_not_saved))
                    }

                    CreateCodeViewModel.CreateCodeEvent.LogoNotRead -> launch {
                        snackbarState.show(message = getString(Res.string.logo_not_read))
                    }
                }
            }
        }
    }

    CreateCodeScreen(
        form = uiState.form,
        preview = uiState.preview,
        style = uiState.style,
        isCreating = uiState.isCreating,
        snackbarState = snackbarState,
        onBackClicked = navigateBack,
        onFormChanged = viewModel::onFormChanged,
        onStyleChanged = viewModel::onStyleChanged,
        onAddLogoClicked = pickLogo,
        onCreateClicked = viewModel::onCreateClicked,
    )
}