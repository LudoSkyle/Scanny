package fr.skyle.scanny.ui.screen.createcode.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import fr.skyle.scanny.domain.model.BarcodeFormat
import fr.skyle.scanny.domain.model.CodeStyle
import fr.skyle.scanny.domain.payload.toRawValue
import fr.skyle.scanny.domain.usecase.creation.RecordCreationUseCase
import fr.skyle.scanny.domain.usecase.scan.WatchScannedCodeUseCase
import fr.skyle.scanny.platform.gallery.LogoPickResult
import fr.skyle.scanny.ui.core.code.mapper.canHold
import fr.skyle.scanny.ui.screen.createcode.ui.mapper.format
import fr.skyle.scanny.ui.screen.createcode.ui.mapper.toBarcodeContent
import fr.skyle.scanny.ui.screen.createcode.ui.mapper.toCreationForm
import fr.skyle.scanny.ui.screen.createcode.ui.model.CodePreview
import fr.skyle.scanny.ui.screen.createcode.ui.model.CreationForm
import fr.skyle.scanny.ui.screen.createcode.ui.model.CreationStep
import io.github.alexzhirkevich.qrose.matrix.qr.QrErrorCorrection
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CreateCodeViewModel(
    scannedCodeId: Long?,
    watchScannedCodeUseCase: WatchScannedCodeUseCase,
    private val recordCreationUseCase: RecordCreationUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateCodeUIState())
    val uiState: StateFlow<CreateCodeUIState> = _uiState.asStateFlow()

    private val _event = Channel<CreateCodeEvent>(Channel.BUFFERED)
    val event = _event.receiveAsFlow()

    init {
        // Filled once with the scanned code it opens on, then the user's
        if (scannedCodeId != null) {
            viewModelScope.launch {
                try {
                    watchScannedCodeUseCase(codeId = scannedCodeId).first()?.let { scannedCode ->
                        onFormChanged(form = scannedCode.toCreationForm())
                    }
                } catch (exception: CancellationException) {
                    throw exception
                } catch (exception: Exception) {
                    Logger.e(throwable = exception) { "Failed to read the scanned code $scannedCodeId" }
                }
            }
        }
    }

    fun onFormChanged(form: CreationForm) {
        _uiState.update { uiState ->
            uiState.copy(
                form = form,
                preview = form.toCodePreview(),
            )
        }
    }

    // A QR code only, once its content holds: a barcode is created from the content
    fun onNextClicked() {
        val uiState = _uiState.value
        if (uiState.preview is CodePreview.Code && uiState.form.format == BarcodeFormat.QR_CODE) {
            _uiState.update { state ->
                state.copy(step = CreationStep.STYLE)
            }
        }
    }

    // The customization is kept until the editor is left: back on it, it is as it was
    fun onBackToContentClicked() {
        _uiState.update { uiState ->
            uiState.copy(step = CreationStep.CONTENT)
        }
    }

    fun onStyleChanged(style: CodeStyle) {
        _uiState.update { uiState ->
            uiState.copy(style = style)
        }
    }

    fun onLogoPicked(result: LogoPickResult) {
        when (result) {
            is LogoPickResult.Picked -> _uiState.update { uiState ->
                uiState.copy(style = uiState.style.copy(logo = result.png))
            }

            LogoPickResult.Unreadable -> _event.trySend(CreateCodeEvent.LogoNotRead)
        }
    }

    fun onCreateClicked() {
        val uiState = _uiState.value
        val content = uiState.form.toBarcodeContent()
        if (uiState.isCreating || content == null || uiState.preview !is CodePreview.Code) {
            return
        }

        _uiState.update { state ->
            state.copy(isCreating = true)
        }
        viewModelScope.launch {
            try {
                val creation = recordCreationUseCase(
                    content = content,
                    format = uiState.form.format,
                    // The style chosen for a QR code is kept when switching to a barcode, not given to it
                    style = if (uiState.form.format == BarcodeFormat.QR_CODE) uiState.style else CodeStyle(),
                )

                _event.send(CreateCodeEvent.OpenCreationDetail(creationId = creation.id))
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                Logger.e(throwable = exception) { "Failed to record a creation in ${uiState.form.format}" }

                _event.send(CreateCodeEvent.CreationNotSaved)
            } finally {
                _uiState.update { state ->
                    state.copy(isCreating = false)
                }
            }
        }
    }

    // A QR code is tried with the highest correction, the one a logo can ask for
    private fun CreationForm.toCodePreview(): CodePreview {
        val rawValue = toBarcodeContent()?.toRawValue() ?: return CodePreview.Incomplete
        val isHeld = format.canHold(
            rawValue = rawValue,
            errorCorrection = QrErrorCorrection.H,
        )
        return if (isHeld) {
            CodePreview.Code(
                rawValue = rawValue,
                format = format,
            )
        } else {
            CodePreview.Refused(format = format)
        }
    }

    data class CreateCodeUIState(
        val form: CreationForm = CreationForm(),
        val preview: CodePreview = CodePreview.Incomplete,
        val style: CodeStyle = CodeStyle(),
        val step: CreationStep = CreationStep.CONTENT,
        val isCreating: Boolean = false,
    )

    sealed interface CreateCodeEvent {
        data class OpenCreationDetail(val creationId: Long) : CreateCodeEvent
        data object CreationNotSaved : CreateCodeEvent
        data object LogoNotRead : CreateCodeEvent
    }
}