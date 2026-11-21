package fr.skyle.scanny.ui.screen.history.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import fr.skyle.scanny.domain.usecase.creation.DeleteAllCreationsUseCase
import fr.skyle.scanny.domain.usecase.creation.WatchCreationsUseCase
import fr.skyle.scanny.domain.usecase.scan.DeleteAllScansUseCase
import fr.skyle.scanny.domain.usecase.scan.WatchScansUseCase
import fr.skyle.scanny.ui.screen.history.ui.mapper.toCreationHistorySections
import fr.skyle.scanny.ui.screen.history.ui.mapper.toScanHistorySections
import fr.skyle.scanny.ui.screen.history.ui.model.CreationHistoryItem
import fr.skyle.scanny.ui.screen.history.ui.model.CreationHistorySection
import fr.skyle.scanny.ui.screen.history.ui.model.ScanHistoryItem
import fr.skyle.scanny.ui.screen.history.ui.model.HistoryView
import fr.skyle.scanny.ui.screen.history.ui.model.ScanHistorySection
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

class HistoryViewModel(
    private val watchScansUseCase: WatchScansUseCase,
    private val watchCreationsUseCase: WatchCreationsUseCase,
    private val deleteAllScansUseCase: DeleteAllScansUseCase,
    private val deleteAllCreationsUseCase: DeleteAllCreationsUseCase,
    private val clock: Clock,
    private val timeZone: TimeZone,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUIState())
    val uiState: StateFlow<HistoryUIState> = _uiState.asStateFlow()

    private val _event = Channel<HistoryEvent>(Channel.BUFFERED)
    val event = _event.receiveAsFlow()

    init {
        viewModelScope.launch {
            watchScansUseCase()
                .map { scans ->
                    scans.toScanHistorySections(
                        today = clock.now().toLocalDateTime(timeZone).date,
                        timeZone = timeZone,
                    )
                }
                .catch { exception ->
                    Logger.e(throwable = exception) { "Failed to read the history of the scans" }
                }
                .collect { sections ->
                    _uiState.update { uiState ->
                        uiState.copy(scanSections = sections)
                    }
                }
        }
        viewModelScope.launch {
            watchCreationsUseCase()
                .map { creations ->
                    creations.toCreationHistorySections(
                        today = clock.now().toLocalDateTime(timeZone).date,
                        timeZone = timeZone,
                    )
                }
                .catch { exception ->
                    Logger.e(throwable = exception) { "Failed to read the history of the creations" }
                }
                .collect { sections ->
                    _uiState.update { uiState ->
                        uiState.copy(creationSections = sections)
                    }
                }
        }
    }

    fun onViewSelected(view: HistoryView) {
        _uiState.update { uiState ->
            uiState.copy(selectedView = view)
        }
    }

    fun onScanClicked(item: ScanHistoryItem) {
        val event = if (item.codeCount > 1) {
            HistoryEvent.OpenScanGroup(scanId = item.scanId)
        } else {
            HistoryEvent.OpenScanDetail(codeId = item.firstCodeId)
        }
        viewModelScope.launch {
            _event.send(event)
        }
    }

    fun onCreationClicked(item: CreationHistoryItem) {
        viewModelScope.launch {
            _event.send(HistoryEvent.OpenCreationDetail(creationId = item.creationId))
        }
    }

    // Clears the view shown, the other one kept
    fun onClearConfirmed() {
        val view = _uiState.value.selectedView
        viewModelScope.launch {
            try {
                when (view) {
                    HistoryView.SCANS -> deleteAllScansUseCase()
                    HistoryView.CREATIONS -> deleteAllCreationsUseCase()
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                Logger.e(throwable = exception) { "Failed to clear the history of the ${view.name.lowercase()}" }

                _event.send(HistoryEvent.ClearFailed)
            }
        }
    }

    data class HistoryUIState(
        val selectedView: HistoryView = HistoryView.SCANS,
        // Null while the history is read
        val scanSections: List<ScanHistorySection>? = null,
        val creationSections: List<CreationHistorySection>? = null,
    )

    sealed interface HistoryEvent {
        data object ClearFailed : HistoryEvent
        data class OpenScanDetail(val codeId: Long) : HistoryEvent
        data class OpenScanGroup(val scanId: Long) : HistoryEvent
        data class OpenCreationDetail(val creationId: Long) : HistoryEvent
    }
}