package fr.skyle.scanny.ui.screen.createcode.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import fr.skyle.scanny.designsystem.core.snackbar.rememberSCSnackbarState
import fr.skyle.scanny.platform.gallery.rememberLogoPicker
import fr.skyle.scanny.resources.Res
import fr.skyle.scanny.resources.creation_not_saved
import fr.skyle.scanny.resources.logo_not_read
import fr.skyle.scanny.ui.screen.createcode.ui.model.CreationSource
import fr.skyle.scanny.ui.screen.createcode.ui.model.CreationStep
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun CreateCodeRoute(
    source: CreationSource,
    navigateToCreationDetail: (creationId: Long) -> Unit,
    navigateBack: () -> Unit,
    viewModel: CreateCodeViewModel = koinViewModel(parameters = { parametersOf(source) }),
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

    // Back from the customization returns to the content, the editor kept: the system back too
    NavigationBackHandler(
        state = rememberNavigationEventState(currentInfo = NavigationEventInfo.None),
        isBackEnabled = uiState.step == CreationStep.STYLE,
        onBackCompleted = viewModel::onBackToContentClicked,
    )

    CreateCodeScreen(
        form = uiState.form,
        preview = uiState.preview,
        contentSize = uiState.contentSize,
        errorCorrectionChoices = uiState.errorCorrectionChoices,
        logoBlocker = uiState.logoBlocker,
        style = uiState.style,
        step = uiState.step,
        isCreating = uiState.isCreating,
        snackbarState = snackbarState,
        onBackClicked = {
            when (uiState.step) {
                CreationStep.CONTENT -> navigateBack()
                CreationStep.STYLE -> viewModel.onBackToContentClicked()
            }
        },
        onFormChanged = viewModel::onFormChanged,
        onStyleChanged = viewModel::onStyleChanged,
        onAddLogoClicked = pickLogo,
        onNextClicked = viewModel::onNextClicked,
        onCreateClicked = viewModel::onCreateClicked,
    )
}