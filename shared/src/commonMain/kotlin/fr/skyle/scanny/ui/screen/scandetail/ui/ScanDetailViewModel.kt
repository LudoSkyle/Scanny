package fr.skyle.scanny.ui.screen.scandetail.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import fr.skyle.scanny.domain.usecase.scan.DeleteScannedCodeUseCase
import fr.skyle.scanny.domain.usecase.scan.WatchScannedCodeUseCase
import fr.skyle.scanny.domain.usecase.settings.WatchScanSettingsUseCase
import fr.skyle.scanny.platform.share.TextSharer
import fr.skyle.scanny.ui.screen.scandetail.ui.mapper.toScanDetailItem
import fr.skyle.scanny.ui.screen.scandetail.ui.model.ScanDetailItem
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ScanDetailViewModel(
    private val codeId: Long,
    private val watchScannedCodeUseCase: WatchScannedCodeUseCase,
    private val watchScanSettingsUseCase: WatchScanSettingsUseCase,
    private val deleteScannedCodeUseCase: DeleteScannedCodeUseCase,
    private val textSharer: TextSharer,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScanDetailUIState())
    val uiState: StateFlow<ScanDetailUIState> = _uiState.asStateFlow()

    private val _event = Channel<ScanDetailEvent>(Channel.BUFFERED)
    val event = _event.receiveAsFlow()

    init {
        viewModelScope.launch {
            combine(
                watchScannedCodeUseCase(codeId = codeId),
                watchScanSettingsUseCase(),
            ) { scannedCode, settings ->
                scannedCode?.toScanDetailItem(isRawContentShown = settings.isRawContentShown)
            }
                .catch { exception ->
                    Logger.e(throwable = exception) { "Failed to read the scanned code $codeId" }
                }
                .collect { code ->
                    _uiState.update { uiState ->
                        uiState.copy(code = code)
                    }
                }
        }
    }

    // The raw value: what another app, or another scanner, can read back
    fun onShareClicked() {
        val code = _uiState.value.code ?: return
        textSharer.shareText(text = code.rawValue)
    }

    fun onDeleteConfirmed() {
        viewModelScope.launch {
            _uiState.update { uiState ->
                uiState.copy(isDeleting = true)
            }

            try {
                deleteScannedCodeUseCase(codeId = codeId)

                _event.send(ScanDetailEvent.CodeDeleted)
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                Logger.e(throwable = exception) { "Failed to delete the scanned code $codeId" }

                _event.send(ScanDetailEvent.DeleteFailed)
            } finally {
                _uiState.update { uiState ->
                    uiState.copy(isDeleting = false)
                }
            }
        }
    }

    data class ScanDetailUIState(
        val code: ScanDetailItem? = null,
        val isDeleting: Boolean = false,
    )

    sealed interface ScanDetailEvent {
        data object CodeDeleted : ScanDetailEvent
        data object DeleteFailed : ScanDetailEvent
    }
}