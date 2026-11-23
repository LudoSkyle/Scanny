package fr.skyle.scanny.ui.screen.createcode.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import fr.skyle.scanny.domain.model.BarcodeFormat
import fr.skyle.scanny.domain.model.CodeStyle
import fr.skyle.scanny.domain.payload.toRawValue
import fr.skyle.scanny.domain.usecase.creation.RecordCreationUseCase
import fr.skyle.scanny.domain.usecase.creation.WatchCreationUseCase
import fr.skyle.scanny.domain.usecase.scan.WatchScannedCodeUseCase
import fr.skyle.scanny.platform.gallery.LogoPickResult
import fr.skyle.scanny.ui.core.code.mapper.canHold
import fr.skyle.scanny.ui.core.code.mapper.holdsCharactersOf
import fr.skyle.scanny.ui.core.code.mapper.qrErrorCorrection
import fr.skyle.scanny.ui.core.code.mapper.toBarcodeType
import fr.skyle.scanny.ui.screen.createcode.ui.mapper.errorCorrectionChoices
import fr.skyle.scanny.ui.screen.createcode.ui.mapper.format
import fr.skyle.scanny.ui.screen.createcode.ui.mapper.logoBlocker
import fr.skyle.scanny.ui.screen.createcode.ui.mapper.qrContentSize
import fr.skyle.scanny.ui.screen.createcode.ui.mapper.toBarcodeContent
import fr.skyle.scanny.ui.screen.createcode.ui.mapper.toCreationForm
import fr.skyle.scanny.ui.screen.createcode.ui.model.CodePreview
import fr.skyle.scanny.ui.screen.createcode.ui.model.CodeRefusal
import fr.skyle.scanny.ui.screen.createcode.ui.model.ContentSize
import fr.skyle.scanny.ui.screen.createcode.ui.model.CreationForm
import fr.skyle.scanny.ui.screen.createcode.ui.model.CreationKind
import fr.skyle.scanny.ui.screen.createcode.ui.model.CreationSource
import fr.skyle.scanny.ui.screen.createcode.ui.model.CreationStep
import fr.skyle.scanny.ui.screen.createcode.ui.model.ErrorCorrectionChoice
import fr.skyle.scanny.ui.screen.createcode.ui.model.LogoBlocker
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
import kotlinx.datetime.TimeZone

class CreateCodeViewModel(
    private val source: CreationSource,
    private val watchScannedCodeUseCase: WatchScannedCodeUseCase,
    private val watchCreationUseCase: WatchCreationUseCase,
    private val recordCreationUseCase: RecordCreationUseCase,
    private val timeZone: TimeZone,
) : ViewModel() {

    // A new event is in the zone of the device
    private val _uiState = MutableStateFlow(CreateCodeUIState(form = CreationForm(eventTimeZone = timeZone.id)).withDerivedState())
    val uiState: StateFlow<CreateCodeUIState> = _uiState.asStateFlow()

    private val _event = Channel<CreateCodeEvent>(Channel.BUFFERED)
    val event = _event.receiveAsFlow()

    // The customization before it was reset, until it changes again: undoing the reset brings it back
    private var styleBeforeReset: CodeStyle? = null

    init {
        // Filled once with the code it opens on, then the user's
        if (source != CreationSource.New) {
            viewModelScope.launch {
                try {
                    fillFromSource()
                } catch (exception: CancellationException) {
                    throw exception
                } catch (exception: Exception) {
                    Logger.e(throwable = exception) { "Failed to read the code the editor opens on: $source" }
                }
            }
        }
    }

    // A created code brings its style along: a QR code is changed as it was customized
    private suspend fun fillFromSource() {
        when (source) {
            CreationSource.New -> Unit

            is CreationSource.ScannedCode -> watchScannedCodeUseCase(codeId = source.codeId).first()?.let { scannedCode ->
                onFormChanged(form = scannedCode.toCreationForm().withDeviceZone())
            }

            is CreationSource.CreatedCode -> watchCreationUseCase(creationId = source.creationId).first()?.let { creation ->
                onFormChanged(form = creation.toCreationForm().withDeviceZone())
                onStyleChanged(style = creation.style)
            }
        }
    }

    // An event switched to from another content is in the zone of the device; a scanned one keeps its own
    private fun CreationForm.withDeviceZone(): CreationForm =
        if (kind == CreationKind.EVENT) this else copy(eventTimeZone = timeZone.id)

    fun onFormChanged(form: CreationForm) {
        _uiState.update { uiState ->
            uiState.copy(form = form).withDerivedState()
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

    // The correction and the logo change what the code holds
    fun onStyleChanged(style: CodeStyle) {
        styleBeforeReset = null
        _uiState.update { uiState ->
            uiState.copy(style = style).withDerivedState()
        }
    }

    // Back to the default customization: the correction, chosen with the content, stays
    fun onResetStyleClicked() {
        val style = _uiState.value.style
        val defaultStyle = style.toDefaultCustomization()
        if (style == defaultStyle) {
            return
        }

        onStyleChanged(style = defaultStyle)
        styleBeforeReset = style
        _event.trySend(CreateCodeEvent.StyleReset)
    }

    fun onUndoStyleResetClicked() {
        styleBeforeReset?.let { style ->
            onStyleChanged(style = style)
        }
    }

    fun onLogoPicked(result: LogoPickResult) {
        when (result) {
            is LogoPickResult.Picked -> onStyleChanged(style = _uiState.value.style.copy(logo = result.png))

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

    // What the form and the style make: the code, how much of it the content takes, and what can be chosen
    private fun CreateCodeUIState.withDerivedState(): CreateCodeUIState {
        val rawValue = form.toBarcodeContent()?.toRawValue()
        val isQrCode = form.format == BarcodeFormat.QR_CODE
        val errorCorrection = style.qrErrorCorrection()
        return copy(
            preview = form.toCodePreview(
                rawValue = rawValue,
                errorCorrection = errorCorrection,
            ),
            // Counted for a QR code only: a barcode has a length
            contentSize = if (isQrCode) {
                qrContentSize(
                    rawValue = rawValue.orEmpty(),
                    errorCorrection = errorCorrection,
                )
            } else {
                null
            },
            errorCorrectionChoices = errorCorrectionChoices(
                rawValue = rawValue,
                style = style,
            ),
            logoBlocker = logoBlocker(
                rawValue = rawValue,
                style = style,
            ),
            canResetStyle = style != style.toDefaultCustomization(),
        )
    }

    private fun CodeStyle.toDefaultCustomization(): CodeStyle =
        CodeStyle(errorCorrection = errorCorrection)

    // A QR code is tried with the correction it will be encoded with
    private fun CreationForm.toCodePreview(
        rawValue: String?,
        errorCorrection: QrErrorCorrection,
    ): CodePreview {
        if (rawValue == null) {
            return CodePreview.Incomplete
        }
        val isHeld = format.canHold(
            rawValue = rawValue,
            errorCorrection = errorCorrection,
        )
        if (isHeld) {
            return CodePreview.Code(
                rawValue = rawValue,
                format = format,
            )
        }

        val reason = when {
            format.toBarcodeType() != null -> CodeRefusal.VALUE_NOT_FOR_FORMAT
            !format.holdsCharactersOf(rawValue) -> CodeRefusal.CHARACTERS_NOT_HELD
            else -> CodeRefusal.TOO_LONG
        }
        return CodePreview.Refused(
            format = format,
            reason = reason,
        )
    }

    data class CreateCodeUIState(
        val form: CreationForm = CreationForm(),
        val preview: CodePreview = CodePreview.Incomplete,
        // Null for a barcode
        val contentSize: ContentSize? = null,
        val errorCorrectionChoices: List<ErrorCorrectionChoice> = emptyList(),
        // Null when a logo can be added
        val logoBlocker: LogoBlocker? = null,
        // False when the customization is the default one
        val canResetStyle: Boolean = false,
        val style: CodeStyle = CodeStyle(),
        val step: CreationStep = CreationStep.CONTENT,
        val isCreating: Boolean = false,
    )

    sealed interface CreateCodeEvent {
        data class OpenCreationDetail(val creationId: Long) : CreateCodeEvent
        data object CreationNotSaved : CreateCodeEvent
        data object LogoNotRead : CreateCodeEvent
        data object StyleReset : CreateCodeEvent
    }
}