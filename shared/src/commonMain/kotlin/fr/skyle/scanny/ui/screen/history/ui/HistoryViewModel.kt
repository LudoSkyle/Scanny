package fr.skyle.scanny.ui.screen.history.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import fr.skyle.scanny.domain.usecase.scan.DeleteAllScansUseCase
import fr.skyle.scanny.domain.usecase.scan.WatchScansUseCase
import fr.skyle.scanny.ui.screen.history.ui.mapper.toScanHistorySections
import fr.skyle.scanny.ui.screen.history.ui.model.ScanHistoryItem
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
    private val deleteAllScansUseCase: DeleteAllScansUseCase,
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
                        uiState.copy(sections = sections)
                    }
                }
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

    fun onClearConfirmed() {
        viewModelScope.launch {
            try {
                deleteAllScansUseCase()
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                Logger.e(throwable = exception) { "Failed to clear the history of the scans" }

                _event.send(HistoryEvent.ClearFailed)
            }
        }
    }

    data class HistoryUIState(
        // Null while the history is read
        val sections: List<ScanHistorySection>? = null,
    )

    sealed interface HistoryEvent {
        data object ClearFailed : HistoryEvent
        data class OpenScanDetail(val codeId: Long) : HistoryEvent
        data class OpenScanGroup(val scanId: Long) : HistoryEvent
    }
}